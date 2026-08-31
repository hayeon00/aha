package com.aha.global.openai;

import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import org.springframework.web.client.RestClientResponseException;

import java.util.Locale;

public final class OpenAiApiExceptionTranslator {

    private static final int TOO_MANY_REQUESTS = 429;

    private OpenAiApiExceptionTranslator() {
    }

    public static BusinessException translate(
            RestClientResponseException exception,
            ErrorCode fallbackErrorCode
    ) {
        if (isCreditExhausted(exception)) {
            return new BusinessException(ErrorCode.OPENAI_CREDIT_EXHAUSTED);
        }
        return new BusinessException(fallbackErrorCode);
    }

    public static boolean isCreditExhausted(
            RestClientResponseException exception
    ) {
        if (exception == null
                || exception.getStatusCode().value() != TOO_MANY_REQUESTS) {
            return false;
        }

        String responseBody = exception.getResponseBodyAsString();
        if (responseBody == null || responseBody.isBlank()) {
            return false;
        }

        String normalized = responseBody.toLowerCase(Locale.ROOT);
        return normalized.contains("insufficient_quota")
                || normalized.contains("credit_balance_exhausted")
                || normalized.contains("no credits remaining");
    }

    public static boolean isCreditExhausted(BusinessException exception) {
        return exception != null
                && exception.getErrorCode() == ErrorCode.OPENAI_CREDIT_EXHAUSTED;
    }
}
