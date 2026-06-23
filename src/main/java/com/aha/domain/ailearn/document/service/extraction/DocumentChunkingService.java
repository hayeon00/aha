package com.aha.domain.ailearn.document.service.extraction;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentChunkingService {

    private static final int MAX_CHUNK_LENGTH = 1500;
    private static final int MIN_CHUNK_LENGTH = 300;


    public List<String> split(String extractedText) {
        if (extractedText == null || extractedText.isBlank()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EMPTY
            );
        }

        String normalizedText =
                normalizeForChunking(extractedText);

        String[] paragraphs =
                normalizedText.split("\\n\\s*\\n");

        List<String> chunks =
                new ArrayList<>();

        StringBuilder currentChunk =
                new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmedParagraph =
                    paragraph.trim();

            if (trimmedParagraph.isBlank()) {
                continue;
            }

            /*
             * 하나의 문단 자체가 최대 길이를 초과하면
             * 문장 단위로 나눠서 처리한다.
             */
            if (trimmedParagraph.length()
                    > MAX_CHUNK_LENGTH) {

                flushCurrentChunk(
                        chunks,
                        currentChunk
                );

                splitLongParagraph(
                        trimmedParagraph,
                        chunks
                );

                continue;
            }

            /*
             * 현재 청크에 문단을 추가했을 때
             * 최대 길이를 넘지 않으면 이어 붙인다.
             */
            if (canAppend(
                    currentChunk,
                    trimmedParagraph
            )) {
                appendParagraph(
                        currentChunk,
                        trimmedParagraph
                );
                continue;
            }

            /*
             * 최대 길이를 넘으면 현재 청크를 저장하고
             * 새 청크를 시작한다.
             */
            flushCurrentChunk(
                    chunks,
                    currentChunk
            );

            currentChunk.append(
                    trimmedParagraph
            );
        }

        flushCurrentChunk(
                chunks,
                currentChunk
        );

        List<String> mergedChunks =
                mergeShortChunks(chunks);

        if (mergedChunks.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DOCUMENT_TEXT_EMPTY
            );
        }

        return mergedChunks;
    }

    private boolean canAppend(
            StringBuilder currentChunk,
            String paragraph
    ) {
        if (currentChunk.isEmpty()) {
            return true;
        }

        int expectedLength =
                currentChunk.length()
                        + 2
                        + paragraph.length();

        return expectedLength <= MAX_CHUNK_LENGTH;
    }

    private void appendParagraph(
            StringBuilder currentChunk,
            String paragraph
    ) {
        if (!currentChunk.isEmpty()) {
            currentChunk.append("\n\n");
        }

        currentChunk.append(paragraph);
    }

    private void flushCurrentChunk(
            List<String> chunks,
            StringBuilder currentChunk
    ) {
        if (currentChunk.isEmpty()) {
            return;
        }

        String chunk =
                currentChunk.toString().trim();

        if (!chunk.isBlank()) {
            chunks.add(chunk);
        }

        currentChunk.setLength(0);
    }

    private void splitLongParagraph(
            String paragraph,
            List<String> chunks
    ) {
        String[] sentences =
                paragraph.split(
                        "(?<=[.!?。！？])\\s+"
                );

        StringBuilder sentenceChunk =
                new StringBuilder();

        for (String sentence : sentences) {
            String trimmedSentence =
                    sentence.trim();

            if (trimmedSentence.isBlank()) {
                continue;
            }

            if (trimmedSentence.length()
                    > MAX_CHUNK_LENGTH) {

                flushCurrentChunk(
                        chunks,
                        sentenceChunk
                );

                splitByFixedLength(
                        trimmedSentence,
                        chunks
                );

                continue;
            }

            if (canAppend(
                    sentenceChunk,
                    trimmedSentence
            )) {
                if (!sentenceChunk.isEmpty()) {
                    sentenceChunk.append(" ");
                }

                sentenceChunk.append(
                        trimmedSentence
                );

                continue;
            }

            flushCurrentChunk(
                    chunks,
                    sentenceChunk
            );

            sentenceChunk.append(
                    trimmedSentence
            );
        }

        flushCurrentChunk(
                chunks,
                sentenceChunk
        );
    }

    private void splitByFixedLength(
            String text,
            List<String> chunks
    ) {
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(
                    start + MAX_CHUNK_LENGTH,
                    text.length()
            );

            String chunk =
                    text.substring(start, end).trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            start = end;
        }
    }

    private List<String> mergeShortChunks(
            List<String> chunks
    ) {
        if (chunks.size() <= 1) {
            return chunks;
        }

        List<String> mergedChunks =
                new ArrayList<>();

        for (String chunk : chunks) {
            if (mergedChunks.isEmpty()) {
                mergedChunks.add(chunk);
                continue;
            }

            if (chunk.length() >= MIN_CHUNK_LENGTH) {
                mergedChunks.add(chunk);
                continue;
            }

            int lastIndex =
                    mergedChunks.size() - 1;

            String previousChunk =
                    mergedChunks.get(lastIndex);

            int mergedLength =
                    previousChunk.length()
                            + 2
                            + chunk.length();

            if (mergedLength <= MAX_CHUNK_LENGTH) {
                mergedChunks.set(
                        lastIndex,
                        previousChunk
                                + "\n\n"
                                + chunk
                );
            } else {
                mergedChunks.add(chunk);
            }
        }

        return mergedChunks;
    }

    private String normalizeForChunking(
            String text
    ) {
        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}