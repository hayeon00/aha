package com.aha.global.exception;

import com.aha.global.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/document/upload");

    @Test
    void 파일_part가_없으면_파일필수_400을_반환한다() {
        ResponseEntity<ErrorResponse> response = handler.handleMissingServletRequestPartException(
                new MissingServletRequestPartException("files"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.DOCUMENT_FILE_REQUIRED.getCode());
    }

    @Test
    void multipart_요청크기_초과는_413을_반환한다() {
        ResponseEntity<ErrorResponse> response = handler.handleMaxUploadSizeExceededException(
                new MaxUploadSizeExceededException(100L), request);

        assertThat(response.getStatusCode().value()).isEqualTo(413);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.DOCUMENT_REQUEST_SIZE_EXCEEDED.getCode());
    }

    @Test
    void 모든_에러코드는_고유하다() {
        long distinctCodeCount = Arrays.stream(ErrorCode.values())
                .map(ErrorCode::getCode)
                .distinct()
                .count();

        assertThat(distinctCodeCount).isEqualTo(ErrorCode.values().length);
    }
}
