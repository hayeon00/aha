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

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentScopeMappingResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * AI의 JSON 응답을 목차 매핑 결과로 변환한다.
     */
    public List<ScopeMappingAiResultResponseDto> parse(
            String aiResponse
    ) {
        validateResponseText(aiResponse);

        String normalizedResponse =
                normalizeResponse(aiResponse);

        try {
            ScopeMappingAiResponseDto response =
                    objectMapper.readValue(
                            normalizedResponse,
                            ScopeMappingAiResponseDto.class
                    );

            validateResponse(response);

            return List.copyOf(response.mappings());

        } catch (BusinessException exception) {
            throw exception;

        } catch (JacksonException exception) {
            log.error(
                    "AI 목차 매핑 응답 JSON 파싱 실패. aiResponse={}",
                    aiResponse,
                    exception
            );

            throw new BusinessException(
                    ErrorCode.AI_RESPONSE_PARSE_FAILED
            );
        } catch (RuntimeException exception) {
            log.error(
                    "AI 목차 매핑 응답 처리 실패. aiResponse={}",
                    aiResponse,
                    exception
            );

            throw new BusinessException(
                    ErrorCode.AI_RESPONSE_PARSE_FAILED
            );
        }
    }

    /**
     * 모델이 실수로 마크다운 JSON 코드 블록을 붙인 경우 제거한다.
     *
     * 잘못된 숫자나 필드 값을 임의로 수정하지는 않는다.
     */
    private String normalizeResponse(
            String aiResponse
    ) {
        String normalized = aiResponse.trim();

        if (normalized.startsWith("```json")) {
            normalized = normalized.substring(
                    "```json".length()
            );
        } else if (normalized.startsWith("```")) {
            normalized = normalized.substring(
                    "```".length()
            );
        }

        if (normalized.endsWith("```")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - "```".length()
            );
        }

        return normalized.trim();
    }

    private void validateResponseText(
            String aiResponse
    ) {
        if (aiResponse == null || aiResponse.isBlank()) {
            throw new BusinessException(
                    ErrorCode.AI_RESPONSE_PARSE_FAILED
            );
        }
    }

    private void validateResponse(
            ScopeMappingAiResponseDto response
    ) {
        if (response == null || response.mappings() == null) {
            throw new BusinessException(
                    ErrorCode.AI_RESPONSE_PARSE_FAILED
            );
        }

        /*
         * 관련성 있는 매핑이 하나도 없는 경우
         * 빈 배열은 정상 응답으로 허용한다.
         */
        for (ScopeMappingAiResultResponseDto mapping
                : response.mappings()) {

            validateMapping(mapping);
        }
    }

    private void validateMapping(
            ScopeMappingAiResultResponseDto mapping
    ) {
        if (mapping == null
                || mapping.documentChunkId() == null
                || mapping.examScopeNodeId() == null
                || mapping.confidenceScore() == null) {

            throw new BusinessException(
                    ErrorCode.AI_RESPONSE_PARSE_FAILED
            );
        }

        BigDecimal confidenceScore =
                mapping.confidenceScore();

        if (confidenceScore.compareTo(BigDecimal.ZERO) < 0
                || confidenceScore.compareTo(BigDecimal.ONE) > 0) {

            throw new BusinessException(
                    ErrorCode.AI_RESPONSE_PARSE_FAILED
            );
        }
    }
}