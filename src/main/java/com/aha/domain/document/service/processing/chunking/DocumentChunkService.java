package com.aha.domain.document.service.processing.chunking;

import com.aha.domain.document.entity.DocumentChunk;
import com.aha.domain.document.entity.SourceDocument;
import com.aha.domain.document.enums.DocumentChunkContentType;
import com.aha.domain.document.repository.DocumentChunkRepository;
import com.aha.domain.document.repository.SourceDocumentRepository;
import com.aha.domain.document.service.processing.parsing.model.DocumentBlock;
import com.aha.domain.document.service.processing.parsing.model.ParsedDocument;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentChunkService {

    /**
     * 현재는 문자 수 기준으로 제한한다.
     *
     * Aha의 1차 목표는 "Section 의미 보존 + Topic Mapping 정확도 검증"이므로
     * 먼저 문자 수 기준으로 단순하게 유지하고,
     * 이후 실제 임베딩 모델을 확정한 뒤 token 기준으로 교체할 수 있다.
     */
    private static final int MAX_CHUNK_LENGTH = 1500;

    private final SourceDocumentRepository sourceDocumentRepository;
    private final DocumentChunkRepository documentChunkRepository;

    @Transactional
    public int createChunks(
            Long sourceDocumentId,
            ParsedDocument parsedDocument
    ) {
        SourceDocument sourceDocument = getSourceDocument(sourceDocumentId);

        validateParsedDocument(parsedDocument);

        List<DocumentChunk> chunks = createDocumentChunksFromBlocks(
                sourceDocument,
                parsedDocument.blocks()
        );

        if (chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_CHUNK_NOT_FOUND);
        }

        documentChunkRepository.deleteAllBySourceDocument_Id(sourceDocumentId);
        documentChunkRepository.saveAll(chunks);

        log.info(
                "문서 청크 생성 완료. sourceDocumentId={}, blockCount={}, chunkCount={}",
                sourceDocumentId,
                parsedDocument.blocks().size(),
                chunks.size()
        );

        return chunks.size();
    }

    private SourceDocument getSourceDocument(Long sourceDocumentId) {
        if (sourceDocumentId == null || sourceDocumentId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return sourceDocumentRepository
                .findById(sourceDocumentId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.SOURCE_DOCUMENT_NOT_FOUND)
                );
    }

    private void validateParsedDocument(ParsedDocument parsedDocument) {
        if (parsedDocument == null
                || parsedDocument.blocks() == null
                || parsedDocument.blocks().isEmpty()) {

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
                    "DocumentBlock 확인. sourceDocumentId={}, pageNo={}, headingPath={}, "
                            + "sectionTitle={}, contentType={}, textLength={}, preview={}",
                    sourceDocument.getId(),
                    block.pageNo(),
                    block.headingPath(),
                    block.sectionTitle(),
                    block.contentType(),
                    block.text() == null ? 0 : block.text().length(),
                    preview(block.text())
            );

            List<ChunkPart> chunkParts = splitSectionIntoChunks(block);

            for (ChunkPart chunkPart : chunkParts) {
                DocumentChunk chunk = createDocumentChunk(
                        sourceDocument,
                        block,
                        chunkPart,
                        chunkOrder++
                );

                chunks.add(chunk);
            }
        }

        return chunks;
    }

    /**
     * 하나의 DocumentBlock을 하나의 Section으로 보고 청킹한다.
     *
     * 핵심 원칙:
     * 1. Section이 충분히 짧으면 제목 + 본문을 그대로 하나의 Chunk로 유지한다.
     * 2. Section이 길면 본문만 문단/문장 단위로 나눈다.
     * 3. 길어서 여러 Chunk로 나뉘더라도 모든 Chunk에 sectionTitle을 다시 붙인다.
     * 4. rawText에는 해당 Chunk가 실제로 담당하는 본문만 저장한다.
     */
    private List<ChunkPart> splitSectionIntoChunks(DocumentBlock block) {
        String sectionTitle = normalizeText(block.sectionTitle());
        String bodyText = resolveBodyText(block);

        if (bodyText == null || bodyText.isBlank()) {
            String fallback = normalizeText(block.text());

            if (fallback == null) {
                return List.of();
            }

            return List.of(new ChunkPart(fallback, fallback));
        }

        int bodyLimit = calculateBodyLimit(sectionTitle);
        List<String> bodyChunks = splitBodyIntoChunks(bodyText, bodyLimit);

        List<ChunkPart> chunkParts = new ArrayList<>();

        for (String bodyChunk : bodyChunks) {
            String contentText = buildContentText(sectionTitle, bodyChunk);

            chunkParts.add(
                    new ChunkPart(
                            contentText,
                            bodyChunk
                    )
            );
        }

        return List.copyOf(chunkParts);
    }

    /**
     * Parser 수정 후 rawText에는 제목을 제외한 실제 본문이 들어가는 것을 우선 사용한다.
     * rawText가 없는 예외적인 Block은 text에서 제목 prefix를 제거해 본문을 복원한다.
     */
    private String resolveBodyText(DocumentBlock block) {
        String rawText = normalizeText(block.rawText());

        if (rawText != null) {
            return rawText;
        }

        String contentText = normalizeText(block.text());

        if (contentText == null) {
            return null;
        }

        String sectionTitle = normalizeText(block.sectionTitle());

        if (sectionTitle != null && contentText.startsWith(sectionTitle)) {
            String remainder = contentText
                    .substring(sectionTitle.length())
                    .trim();

            if (!remainder.isBlank()) {
                return remainder;
            }
        }

        return contentText;
    }

    /**
     * 제목도 최종 contentText 길이에 포함되므로,
     * 본문이 사용할 수 있는 길이를 제목 길이만큼 줄인다.
     */
    private int calculateBodyLimit(String sectionTitle) {
        if (sectionTitle == null || sectionTitle.isBlank()) {
            return MAX_CHUNK_LENGTH;
        }

        int headingContextLength = sectionTitle.length() + 2;

        return Math.max(
                MAX_CHUNK_LENGTH - headingContextLength,
                MAX_CHUNK_LENGTH / 2
        );
    }

    private String buildContentText(
            String sectionTitle,
            String bodyText
    ) {
        if (sectionTitle == null || sectionTitle.isBlank()) {
            return bodyText;
        }

        return sectionTitle + "\n\n" + bodyText;
    }

    /**
     * Section 본문을 먼저 문단 단위로 묶고,
     * 하나의 문단 자체가 너무 긴 경우에만 문장 경계를 우선하여 추가 분할한다.
     *
     * 현재는 overlap을 두지 않는다.
     * Section 제목을 모든 child chunk에 반복해 넣어 문맥을 보존하고,
     * Topic Mapping 실험 결과를 본 뒤 overlap 필요 여부를 판단한다.
     */
    private List<String> splitBodyIntoChunks(
            String text,
            int maxLength
    ) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String normalizedParagraph = normalizeText(paragraph);

            if (normalizedParagraph == null) {
                continue;
            }

            if (normalizedParagraph.length() > maxLength) {
                flushCurrentChunk(chunks, currentChunk);
                splitLongParagraph(normalizedParagraph, maxLength, chunks);
                continue;
            }

            int separatorLength = currentChunk.isEmpty() ? 0 : 2;
            int expectedLength = currentChunk.length()
                    + separatorLength
                    + normalizedParagraph.length();

            if (expectedLength > maxLength) {
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

    /**
     * 긴 문단은 가능한 한 문장 종결 지점에서 자른다.
     * 기존의 '다' 한 글자를 문장 경계로 취급하는 방식은 제거했다.
     */
    private void splitLongParagraph(
            String paragraph,
            int maxLength,
            List<String> chunks
    ) {
        int startIndex = 0;

        while (startIndex < paragraph.length()) {
            int endIndex = Math.min(
                    startIndex + maxLength,
                    paragraph.length()
            );

            if (endIndex < paragraph.length()) {
                int sentenceEndIndex = findSentenceEnd(
                        paragraph,
                        startIndex,
                        endIndex,
                        maxLength
                );

                if (sentenceEndIndex > startIndex) {
                    endIndex = sentenceEndIndex;
                }
            }

            String chunk = paragraph
                    .substring(startIndex, endIndex)
                    .trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            startIndex = endIndex;
        }
    }

    /**
     * 목표 길이의 절반 이후부터 뒤쪽으로 탐색하면서
     * 문장 종결 문자 또는 줄바꿈을 찾는다.
     */
    private int findSentenceEnd(
            String text,
            int startIndex,
            int endIndex,
            int maxLength
    ) {
        int minimumSplitIndex = startIndex + maxLength / 2;

        for (int index = endIndex - 1; index >= minimumSplitIndex; index--) {
            char currentCharacter = text.charAt(index);

            if (currentCharacter == '.'
                    || currentCharacter == '!'
                    || currentCharacter == '?'
                    || currentCharacter == '\n') {

                return index + 1;
            }
        }

        return endIndex;
    }

    private void flushCurrentChunk(
            List<String> chunks,
            StringBuilder currentChunk
    ) {
        if (currentChunk.isEmpty()) {
            return;
        }

        String chunkText = currentChunk
                .toString()
                .trim();

        if (!chunkText.isBlank()) {
            chunks.add(chunkText);
        }

        currentChunk.setLength(0);
    }

    private DocumentChunk createDocumentChunk(
            SourceDocument sourceDocument,
            DocumentBlock block,
            ChunkPart chunkPart,
            int chunkOrder
    ) {
        DocumentChunkContentType contentType = resolveContentType(block);

        return DocumentChunk.create(
                sourceDocument,
                chunkOrder,
                block.pageNo(),
                block.pageNo(),
                block.sectionTitle(),
                block.headingPath(),
                contentType,
                block.codeLanguage(),
                chunkPart.contentText(),
                chunkPart.rawText(),
                null,
                null,
                buildStructureMetadata(block),
                null
        );
    }

    private Map<String, Object> buildStructureMetadata(DocumentBlock block) {
        Map<String, Object> structure = new LinkedHashMap<>();

        structure.put(
                "extractionMethod",
                block.resolvedExtractionMethod().name()
        );

        if (block.headingLevel() != null) {
            structure.put(
                    "headingLevel",
                    block.headingLevel()
            );
        }

        return structure;
    }

    private DocumentChunkContentType resolveContentType(DocumentBlock block) {
        return block.contentType() == null
                ? DocumentChunkContentType.TEXT
                : block.contentType();
    }

    private String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String normalized = text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ')
                .replaceAll("[\\t ]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        return normalized.isBlank()
                ? null
                : normalized;
    }

    private String preview(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalizedText = text.replace("\n", " ")
                .trim();

        return normalizedText.substring(
                0,
                Math.min(200, normalizedText.length())
        );
    }

    /**
     * contentText: 검색/매핑/임베딩에 사용할 텍스트
     * rawText: 해당 Chunk가 실제로 담당하는 원문 본문
     */
    private record ChunkPart(
            String contentText,
            String rawText
    ) {
    }
}