package com.aha.domain.ailearn.document.service.extraction;

import com.aha.domain.ailearn.document.entity.DocumentChunk;
import com.aha.domain.ailearn.document.entity.SourceDocument;
import com.aha.domain.ailearn.document.enums.DocumentChunkContentType;
import com.aha.domain.ailearn.document.repository.DocumentChunkRepository;
import com.aha.domain.ailearn.document.repository.SourceDocumentRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;


import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 그룹에 속한 문서별로 다음 작업을 수행한다.
 *
 1. processingGroupId에 속한 SourceDocument 조회
 2. 각 SourceDocument의 storageKey로 실제 파일 경로 확인
 3. PDF/DOCX/TXT 텍스트 추출
 4. 텍스트 정제
 5. 청크 분할
 6. DocumentChunk 저장
 */

@Slf4j
@Service
public class DocumentExtractionService {

    private static final int MAX_CHUNK_LENGTH = 1500;

    private final SourceDocumentRepository  sourceDocumentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final Tika tika;
    private final Path baseUploadDirectory;

    public DocumentExtractionService(SourceDocumentRepository sourceDocumentRepository, DocumentChunkRepository documentChunkRepository,
                                     @Value("${file.document-upload-dir:uploads}") String documentUploadDir) {
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.tika = new Tika();
        this.baseUploadDirectory = Paths.get(documentUploadDir)
                                        .toAbsolutePath()
                                        .normalize();
    }

    @Transactional
    public void extractDocuments(Long processingGroupId){

        if(processingGroupId == null){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<SourceDocument> sourceDocuments = sourceDocumentRepository.findAllByProcessingGroup_IdOrderByIdAsc(processingGroupId);

        if(sourceDocuments.isEmpty()){
            throw new BusinessException(ErrorCode.SOURCE_DOCUMENT_NOT_FOUND);
        }

        for(SourceDocument sourceDocument : sourceDocuments){
            extractAndSaveChunks(sourceDocument);
        }
    }

    private void extractAndSaveChunks(SourceDocument sourceDocument) {

        log.info(
                "문서 텍스트 추출 시작. sourceDocumentId={}, fileName={}",
                sourceDocument.getId(),
                sourceDocument.getOriginalFileName()
        );

        Path documentPath = resolveDocumentPath(sourceDocument);

        String extractedText = extractText(sourceDocument, documentPath);

        String normalizedText = normalizeText(extractedText);

        List<String> chunkTexts = splitIntoChunks(normalizedText);

        if(chunkTexts.isEmpty()){
            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
        }

        documentChunkRepository.deleteAllBySourceDocument_Id(sourceDocument.getId());

        List<DocumentChunk> chunks = createDocumentChunks(sourceDocument, chunkTexts);

        documentChunkRepository.saveAll(chunks);

        log.info(
                "문서 텍스트 추출 및 청크 저장 완료. sourceDocumentId={}, chunkCount={}",
                sourceDocument.getId(),
                chunks.size()
        );

    }

    private Path resolveDocumentPath(SourceDocument sourceDocument){

        String storageKey = sourceDocument.getStorageKey();

        if(storageKey == null || storageKey.isBlank()){
            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
        }

        Path documentPath = baseUploadDirectory.resolve(storageKey)
                                                .toAbsolutePath()
                                                .normalize();

        if(!documentPath.startsWith(baseUploadDirectory)){

            log.warn(
                    "허용되지 않은 문서 경로입니다. sourceDocumentId={}, path={}",
                    sourceDocument.getId(),
                    documentPath
            );

            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);

        }

        if(!Files.isRegularFile(documentPath)){

            log.warn(
                    "저장된 문서 파일을 찾을 수 없습니다. sourceDocumentId={}, path={}",
                    sourceDocument.getId(),
                    documentPath
            );

            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);

        }

        return documentPath;
    }

    private String extractText(SourceDocument sourceDocument, Path documentPath) {

        try{
            String extractedText = tika.parseToString(documentPath);

            if(extractedText == null || extractedText.isBlank()){

                log.warn(
                        "문서에서 텍스트를 추출하지 못했습니다. sourceDocumentId={}, fileName={}",
                        sourceDocument.getId(),
                        sourceDocument.getOriginalFileName()
                );

                throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EMPTY);
            }

            return extractedText;
        } catch (BusinessException exception){
            throw exception;

        } catch (Exception exception) {
            log.error(
                    "문서 분석 중 예상하지 못한 오류가 발생했습니다. sourceDocumentId={}, fileName={}",
                    sourceDocument.getId(),
                    sourceDocument.getOriginalFileName(),
                    exception
            );

            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);

        }
    }

    private String normalizeText(String text){

        String normalizedText = text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ');

        normalizedText = normalizedText.replaceAll("[\\t ]+", " ");

        normalizedText = normalizedText.replaceAll("\\n{3,}", "\n\n");

        normalizedText = normalizedText.trim();

        if (normalizedText.isBlank()) {

            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EMPTY);
        }

        return normalizedText;

    }

    private List<String> splitIntoChunks(String text){

        List<String> chunks = new ArrayList<>();

        String[] paragraphs = text.split("\\n\\s*\\n");

        StringBuilder currentChunk = new StringBuilder();

        for(String paragraph : paragraphs){

            String normalizedParagraph = paragraph.trim();

            if(normalizedParagraph.isBlank()){ continue; }

            if(normalizedParagraph.length() > MAX_CHUNK_LENGTH){

                flushCurrentChunk(chunks, currentChunk);

                splitLongParagraph(normalizedParagraph, chunks);

                continue;
            }

            int separatorLength = currentChunk.isEmpty() ?0 :2;

            int expectedLength = currentChunk.length() + separatorLength + normalizedParagraph.length();

            if(expectedLength > MAX_CHUNK_LENGTH){

                flushCurrentChunk(chunks, currentChunk);
            }

            if(!currentChunk.isEmpty()){
                currentChunk.append("\n\n");
            }

            currentChunk.append(normalizedParagraph);
        }

        flushCurrentChunk(chunks, currentChunk);

        return List.copyOf(chunks);
    }

    private void splitLongParagraph(String paragraph, List<String> chunks) {

        int startIndex = 0;

        while(startIndex < paragraph.length()){

            int endIndex = Math.min(startIndex + MAX_CHUNK_LENGTH, paragraph.length());

            if(endIndex < paragraph.length()){

                int sentenceEndIndex = findSentenceEnd(paragraph, startIndex, endIndex);

                if(sentenceEndIndex > startIndex){
                    endIndex = sentenceEndIndex;
                }
            }

            String chunk = paragraph.substring(startIndex, endIndex).trim();

            if(!chunk.isBlank()){
                chunks.add(chunk);
            }

            startIndex = endIndex;
        }
    }

    private int findSentenceEnd(String text, int startIndex, int endIndex) {

        int minimumSplitIndex = startIndex + MAX_CHUNK_LENGTH / 2;

        for(int index = endIndex-1; index >= minimumSplitIndex; index--){

            char currentCharacter = text.charAt(index);

            if(currentCharacter == '.' || currentCharacter == '!' || currentCharacter == '?' || currentCharacter == '\n'){
                return index + 1;
            }
        }

        return endIndex;
    }

    private void flushCurrentChunk(List<String> chunks, StringBuilder currentChunk){

        if(currentChunk.isEmpty()) return;

        String chunkText = currentChunk.toString().trim();

        if(!chunkText.isBlank()){
            chunks.add(chunkText);
        }

        currentChunk.setLength(0);

    }

    private List<DocumentChunk> createDocumentChunks(SourceDocument sourceDocument, List<String> chunkTexts){

        List<DocumentChunk> chunks = new ArrayList<>(chunkTexts.size());

        for(int index = 0; index < chunkTexts.size(); index++){

            String chunkText = chunkTexts.get(index);

            DocumentChunk chunk = DocumentChunk.builder()
                    .sourceDocument(sourceDocument)
                    .chunkOrder(index+1)
                    .pageNo(null)
                    .sectionTitle(null)
                    .contentType(DocumentChunkContentType.TEXT)
                    .contentText(chunkText)
                    .rawText(chunkText)
                    .summary(null)
                    .keywordsJson(null)
                    .structureJson(null)
                    .tokenCount(null)
                    .build();

            chunks.add(chunk);
        }

        return chunks;

    }






}