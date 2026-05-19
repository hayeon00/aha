package com.aha.global.exception;

public enum ErrorCode {

    INVALID_INPUT_VALUE(400, "COMMON_001", "잘못된 입력값입니다."),
    UNAUTHORIZED(401, "COMMON_002", "로그인이 필요합니다."),
    ENTITY_NOT_FOUND(404, "COMMON_003", "대상을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "COMMON_004", "서버 내부 오류입니다.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
