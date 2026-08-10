package com.aha.global.exception;

import com.aha.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn(
                "Business exception occurred. uri={}, code={}, message={}",
                request.getRequestURI(),
                errorCode.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode,e.getData()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        log.debug(
                "Validation exception occurred. uri={}, errors={}",
                request.getRequestURI(),
                e.getBindingResult().getFieldErrors()
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getBindingResult()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException e,
        HttpServletRequest request
    ) {
      log.warn("Missing/Invalid RequestParameter. parameterName={}, uri={}", e.getParameterName(), request.getRequestURI());
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST_FORMAT));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(
            MissingServletRequestPartException e,
            HttpServletRequest request
    ) {
        log.warn("Missing multipart request part. partName={}, uri={}", e.getRequestPartName(), request.getRequestURI());

        ErrorCode errorCode = "files".equals(e.getRequestPartName())
                ? ErrorCode.DOCUMENT_FILE_REQUIRED
                : ErrorCode.INVALID_REQUEST_FORMAT;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e,
            HttpServletRequest request
    ) {
        log.warn("Multipart request size exceeded. uri={}, maxUploadSize={}",
                request.getRequestURI(), e.getMaxUploadSize());

        return ResponseEntity
                .status(ErrorCode.DOCUMENT_REQUEST_SIZE_EXCEEDED.getStatus())
                .body(ErrorResponse.of(ErrorCode.DOCUMENT_REQUEST_SIZE_EXCEEDED));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(
            MultipartException e,
            HttpServletRequest request
    ) {
        log.warn("Invalid multipart request. uri={}, message={}", request.getRequestURI(), e.getMessage());

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST_FORMAT.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST_FORMAT));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request
    ) {
        log.warn("Request argument type mismatch. parameterName={}, uri={}", e.getName(), request.getRequestURI());

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e,
            HttpServletRequest request
    ) {
        log.warn("Unsupported media type. contentType={}, uri={}", e.getContentType(), request.getRequestURI());

        return ResponseEntity
                .status(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getStatus())
                .body(ErrorResponse.of(ErrorCode.UNSUPPORTED_MEDIA_TYPE));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("Unexpected exception occurred. uri={}", request.getRequestURI(), e);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
