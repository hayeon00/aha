package com.aha.global.response;

import com.aha.global.exception.ErrorCode;
import org.springframework.validation.BindingResult;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        Object data,
        List<FieldError> errors
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                null,
                List.of()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, Object data) {
        return new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage(),
            data,
            List.of()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                null,
                FieldError.of(bindingResult)
        );
    }

    public record FieldError(
            String field,
            String value,
            String reason
    ) {

        public static List<FieldError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            String.valueOf(error.getRejectedValue()),
                            error.getDefaultMessage()
                    ))
                    .toList();
        }
    }
}
