package com.aha.domain.notestudio.document.client.embedding;

import com.aha.domain.notestudio.document.service.embedding.EmbeddingModelProvider;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenAiEmbeddingClient implements EmbeddingClient {

    private static final String EMBEDDING_ENDPOINT = "/embeddings";

    private final RestClient openAiRestClient;
    private final EmbeddingModelProvider embeddingModelProvider;

    @Override
    public List<Double> embed(String text) {
        if (text == null || text.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<List<Double>> embeddings = embedAll(List.of(text));

        if (embeddings.isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return embeddings.get(0);
    }

    @Override
    public List<List<Double>> embedAll(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<String> normalizedTexts = texts.stream()
                .map(this::normalizeText)
                .toList();

        EmbeddingRequestDto request = new EmbeddingRequestDto(
                embeddingModelProvider.getEmbeddingModel(),
                normalizedTexts
        );

        EmbeddingResponseDto response = openAiRestClient.post()
                .uri(EMBEDDING_ENDPOINT)
                .body(request)
                .retrieve()
                .body(EmbeddingResponseDto.class);

        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        if (response.data().size() != normalizedTexts.size()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return response.data().stream()
                .sorted(Comparator.comparing(EmbeddingResponseDto.EmbeddingData::index))
                .map(EmbeddingResponseDto.EmbeddingData::embedding)
                .toList();
    }

    private String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ')
                .replaceAll("[\\t ]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}