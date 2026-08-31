package com.aha.global.openai;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiApiExceptionTranslatorTest {

    @Test
    void translatesExhaustedCreditResponseToNonRetryableBusinessError() {
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                HttpHeaders.EMPTY,
                "{\"error\":{\"code\":\"credit_balance_exhausted\",\"type\":\"insufficient_quota\"}}"
                        .getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        BusinessException translated = OpenAiApiExceptionTranslator.translate(
                exception,
                ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
        );

        assertThat(translated.getErrorCode())
                .isEqualTo(ErrorCode.OPENAI_CREDIT_EXHAUSTED);
        assertThat(OpenAiApiExceptionTranslator.isCreditExhausted(translated))
                .isTrue();
    }

    @Test
    void keepsOrdinaryRateLimitAsStageSpecificError() {
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                HttpHeaders.EMPTY,
                "{\"error\":{\"code\":\"rate_limit_exceeded\"}}"
                        .getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        BusinessException translated = OpenAiApiExceptionTranslator.translate(
                exception,
                ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED
        );

        assertThat(translated.getErrorCode())
                .isEqualTo(ErrorCode.DOCUMENT_SCOPE_MAPPING_FAILED);
    }
}
