package com.aha.global.exception;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

    @Test
    void 외부_에러코드는_서로_중복되지_않는다() {
        assertThat(Arrays.stream(ErrorCode.values())
                .map(ErrorCode::getCode))
                .doesNotHaveDuplicates();
    }

    @Test
    void 문서파일_크기제한은_413을_사용한다() {
        assertThat(ErrorCode.DOCUMENT_FILE_SIZE_EXCEEDED.getStatus()).isEqualTo(413);
        assertThat(ErrorCode.DOCUMENT_TOTAL_FILE_SIZE_EXCEEDED.getStatus()).isEqualTo(413);
        assertThat(ErrorCode.DOCUMENT_REQUEST_SIZE_EXCEEDED.getStatus()).isEqualTo(413);
    }

    @Test
    void 파일검증과_스토리지실패를_구분한다() {
        assertThat(ErrorCode.DOCUMENT_FILE_VALIDATION_FAILED.getStatus()).isEqualTo(422);
        assertThat(ErrorCode.DOCUMENT_STORAGE_FAILED.getStatus()).isEqualTo(500);
        assertThat(ErrorCode.DOCUMENT_FILE_VALIDATION_FAILED.getCode())
                .isNotEqualTo(ErrorCode.DOCUMENT_STORAGE_FAILED.getCode());
    }
}
