package com.aha.global.exception;

public enum ErrorCode {

    // COMMON
    INVALID_INPUT_VALUE(400, "COMMON_001", "잘못된 입력값입니다."),
    ENTITY_NOT_FOUND(404, "COMMON_002", "대상을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "COMMON_999", "서버 내부 오류입니다."),

    // AUTH
    UNAUTHORIZED(401, "AUTH_001", "로그인이 필요합니다."),
    EMAIL_ALREADY_EXISTS(409, "AUTH_002", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(409, "AUTH_003", "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS(401, "AUTH_004", "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(401, "AUTH_005", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "AUTH_006", "만료된 토큰입니다."),

    // USER
    USER_NOT_FOUND(404, "USER_001", "사용자를 찾을 수 없습니다."),
    PROFILE_IMAGE_UPLOAD_FAILED(500, "USER_002", "프로필 이미지 업로드에 실패했습니다."),
    INVALID_PROFILE_IMAGE(400, "USER_003", "지원하지 않는 프로필 이미지 형식입니다."),

    // USER_EXAM
    USER_EXAM_NOT_FOUND(404, "USER_EXAM_001", "존재하지 않는 내 시험입니다."),

    // AILEARN DOCUMENT
    DOCUMENT_UPLOAD_FAILED(500, "DOCUMENT_001", "문서 업로드에 실패했습니다."),
    INVALID_DOCUMENT_FILE(400, "DOCUMENT_002", "지원하지 않는 문서 파일입니다."),
    DOCUMENT_PROCESSING_GROUP_NOT_FOUND(404, "DOCUMENT_003", "문서 처리 그룹을 찾을 수 없습니다.");


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