package com.aha.domain.document.service.processing.chunk;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.enums.DocumentChunkContentType;
import com.aha.domain.document.repository.DocumentChunkRepository;
import com.aha.domain.document.service.processing.extraction.model.DocumentBlock;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * DocumentBlock 목록
 * 1. 제목 경로 유지
 * 2. 너무 긴 블록 분할
 * 3. 너무 짧은 블록 병합
 * 4. SQL/표/본문 단위 보존
 * 5. DocumentChunk 저장
 *
 * @author : rlagkdus
 * @filename : DocumentChunkService
 * @since : 2026. 7. 8. 수요일
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentChunkService {

    private static final int MAX_CHUNK_LENGTH = 1500;

    private final DocumentChunkRepository documentChunkRepository;

    @Transactional
    public int createChunks(SourceDocument sourceDocument, List<DocumentBlock> blocks) {
        validateSourceDocument(sourceDocument);
        validateBlocks(blocks);

        List<DocumentChunk> chunks = createDocumentChunksFromBlocks(
                sourceDocument,
                blocks
        );

        if (chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EXTRACTION_FAILED);
        }

        documentChunkRepository.deleteAllBySourceDocument_Id(sourceDocument.getId());
        documentChunkRepository.saveAll(chunks);

        log.info(
                "문서 청크 생성 완료. sourceDocumentId={}, chunkCount={}",
                sourceDocument.getId(),
                chunks.size()
        );

        return chunks.size();
    }

    private void validateSourceDocument(SourceDocument sourceDocument) {
        if (sourceDocument == null || sourceDocument.getId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateBlocks(List<DocumentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_TEXT_EMPTY);
        }
    }

    private List<DocumentChunk> createDocumentChunksFromBlocks(
            SourceDocument sourceDocument,
            List<DocumentBlock> blocks
    ) {
        List<DocumentChunk> chunks = new ArrayList<>();
        int chunkOrder = 1;

        log.info(
                "청크 생성 입력 확인. sourceDocumentId={}, blockCount={}",
                sourceDocument.getId(),
                blocks.size()
        );

        for (DocumentBlock block : blocks) {
            if (block == null || block.isBlank()) {
                continue;
            }

            log.info(
                    "DocumentBlock 확인. sourceDocumentId={}, contentType={}, textLength={}, preview={}",
                    sourceDocument.getId(),
                    block.contentType(),
                    block.text() == null ? 0 : block.text().length(),
                    preview(block.text())
            );

            List<String> chunkTexts = splitIntoChunks(block.text());

            log.info(
                    "DocumentBlock 분할 결과. sourceDocumentId={}, contentType={}, splitChunkCount={}",
                    sourceDocument.getId(),
                    block.contentType(),
                    chunkTexts.size()
            );

            for (String chunkText : chunkTexts) {
                DocumentChunk chunk = createDocumentChunk(
                        sourceDocument,
                        block,
                        chunkText,
                        chunkOrder++
                );

                chunks.add(chunk);
            }
        }

        return chunks;
    }

    private String preview(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalizedText = text.replace("\n", " ").trim();

        return normalizedText.substring(
                0,
                Math.min(200, normalizedText.length())
        );
    }


    private DocumentChunk createDocumentChunk(
            SourceDocument sourceDocument,
            DocumentBlock block,
            String chunkText,
            int chunkOrder
    ) {
        DocumentChunkContentType contentType = resolveContentType(block);

        return DocumentChunk.builder()
                .sourceDocument(sourceDocument)
                .chunkOrder(chunkOrder)
                .pageNo(block.pageNo())
                .sectionTitle(block.sectionTitle())
                .headingPath(block.headingPath())
                .contentType(contentType)
                .contentText(chunkText)
                .rawText(chunkText)
                .summary(null)
                .keywordsJson(null)
                .structureJson(null)
                .tokenCount(null)
                .build();
    }

    private DocumentChunkContentType resolveContentType(DocumentBlock block) {
        if (block.contentType() == null) {
            return DocumentChunkContentType.TEXT;
        }

        return block.contentType();
    }

    private List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        String[] paragraphs = text.split("\\n\\s*\\n");

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String normalizedParagraph = paragraph.trim();

            if (normalizedParagraph.isBlank()) {
                continue;
            }

            if (normalizedParagraph.length() > MAX_CHUNK_LENGTH) {
                flushCurrentChunk(chunks, currentChunk);
                splitLongParagraph(normalizedParagraph, chunks);
                continue;
            }

            int separatorLength = currentChunk.isEmpty() ? 0 : 2;

            int expectedLength =
                    currentChunk.length() + separatorLength + normalizedParagraph.length();

            if (expectedLength > MAX_CHUNK_LENGTH) {
                flushCurrentChunk(chunks, currentChunk);
            }

            if (!currentChunk.isEmpty()) {
                currentChunk.append("\n\n");
            }

            currentChunk.append(normalizedParagraph);
        }

        flushCurrentChunk(chunks, currentChunk);

        return List.copyOf(chunks);
    }

    private void splitLongParagraph(String paragraph, List<String> chunks) {
        int startIndex = 0;

        while (startIndex < paragraph.length()) {
            int endIndex = Math.min(startIndex + MAX_CHUNK_LENGTH, paragraph.length());

            if (endIndex < paragraph.length()) {
                int sentenceEndIndex = findSentenceEnd(paragraph, startIndex, endIndex);

                if (sentenceEndIndex > startIndex) {
                    endIndex = sentenceEndIndex;
                }
            }

            String chunk = paragraph.substring(startIndex, endIndex).trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            startIndex = endIndex;
        }
    }

    private int findSentenceEnd(String text, int startIndex, int endIndex) {
        int minimumSplitIndex = startIndex + MAX_CHUNK_LENGTH / 2;

        for (int index = endIndex - 1; index >= minimumSplitIndex; index--) {
            char currentCharacter = text.charAt(index);

            if (currentCharacter == '.'
                    || currentCharacter == '!'
                    || currentCharacter == '?'
                    || currentCharacter == '\n'
                    || currentCharacter == '다') {
                return index + 1;
            }
        }

        return endIndex;
    }

    private void flushCurrentChunk(List<String> chunks, StringBuilder currentChunk) {
        if (currentChunk.isEmpty()) {
            return;
        }

        String chunkText = currentChunk.toString().trim();

        if (!chunkText.isBlank()) {
            chunks.add(chunkText);
        }

        currentChunk.setLength(0);
    }
}
