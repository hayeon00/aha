package com.aha.domain.ailearn.document.client.mapping;

import com.aha.domain.ailearn.document.dto.mapping.response.ScopeMappingAiResponseDto;
import com.aha.domain.ailearn.document.dto.mapping.response.ScopeMappingAiResultResponseDto;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentScopeMappingResponseParser {

    private final ObjectMapper objectMapper;

    public List<ScopeMappingAiResultResponseDto> parse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }

        try {
            ScopeMappingAiResponseDto response =
                    objectMapper.readValue(aiResponse, ScopeMappingAiResponseDto.class);

            if (response.mappings() == null) {
                return List.of();
            }

            return response.mappings();

        } catch (JacksonException exception) {
            log.error(
                    "AI 목차 매핑 응답 파싱 실패. aiResponse={}",
                    aiResponse,
                    exception
            );

            throw new BusinessException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }
}