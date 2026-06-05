package com.aha.global.exception;

public enum ErrorCode {

    INVALID_INPUT_VALUE(400, "COMMON_001", "잘못된 입력값입니다."),
    ENTITY_NOT_FOUND(404, "COMMON_002", "대상을 찾을 수 없습니다."),
    INVALID_REQUEST_FORMAT(400, "COMMON_003", "잘못된 요청 형식입니다."),
    INTERNAL_SERVER_ERROR(500, "COMMON_999", "서버 내부 오류입니다."),

    UNAUTHORIZED(401, "AUTH_001", "로그인이 필요합니다."),
    EMAIL_ALREADY_EXISTS(409, "AUTH_002", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(409, "AUTH_003", "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS(401, "AUTH_004", "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(401, "AUTH_005", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "AUTH_006", "만료된 토큰입니다."),

    USER_NOT_FOUND(404, "USER_001", "사용자를 찾을 수 없습니다."),

    EXAM_NOT_FOUND(404, "EXAM_NOT_FOUND", "해당 시험이 존재하지 않습니다."),
    EXAM_INACTIVE(400, "EXAM_INACTIVE", "해당 시험은 비활성화 상태입니다."),

    WORKBOOK_TYPE_NOT_FOUND(404, "WORKBOOK_TYPE_NOT_FOUND", "문제집 유형을 찾을 수 없습니다."),
    WORKBOOK_NOT_FOUND(404, "WORKBOOK_NOT_FOUND", "해당 워크북이 존재하지 않습니다."),
    WORKBOOK_TYPE_UNSUPPORTED_EXAM(422,"WORKBOOK_TYPE_UNSUPPORTED_EXAM","해당 워크북 유형은 선택하신 시험을 지원하지 않습니다."),
    WORKBOOK_ATTEMPT_ALREADY_EXIST(422,"WORKBOOK_ATTEMPT_ALREADY_EXIST","이미 해당 워크북 풀이 중입니다."),
    WORKBOOK_ARCHIVED(422,"WORKBOOK_ARCHIVED" ,"해당 워크북은 서버 보관 중입니다." );

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
