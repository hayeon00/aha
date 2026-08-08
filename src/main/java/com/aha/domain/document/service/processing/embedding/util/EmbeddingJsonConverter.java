package com.aha.domain.document.service.processing.embedding.util;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmbeddingJsonConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Double> fromJson(String embeddingJson) {
        if (embeddingJson == null || embeddingJson.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        try {
            return objectMapper.readValue(
                    embeddingJson,
                    new TypeReference<List<Double>>() {
                    }
            );
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
