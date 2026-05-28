package com.aha.global.exception;

public enum ErrorCode {


    INVALID_INPUT_VALUE(400, "COMMON_001", "잘못된 입력값입니다."),
    ENTITY_NOT_FOUND(404, "COMMON_002", "대상을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "COMMON_999", "서버 내부 오류입니다."),

    UNAUTHORIZED(401, "AUTH_001", "로그인이 필요합니다."),
    EMAIL_ALREADY_EXISTS(409, "AUTH_002", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(409, "AUTH_003", "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS(401, "AUTH_004", "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(401, "AUTH_005", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "AUTH_006", "만료된 토큰입니다."),

    USER_NOT_FOUND(404, "USER_001", "사용자를 찾을 수 없습니다."),

    SOURCE_DOCUMENT_NOT_FOUND(404, "DOC_001", "학습 원본문서를 찾을 수 없습니다."),
    SOURCE_DOCUMENT_SCOPE_REQUIRED(400, "DOC_002", "목차가 연결되지 않은 문서는 게시할 수 없습니다."),
    FILE_EMPTY(400, "DOC_003", "업로드할 파일이 없습니다."),
    FILE_SAVE_FAILED(500, "DOC_004", "파일 저장 중 오류가 발생했습니다."),
    UNSUPPORTED_DOCUMENT_TYPE(400, "DOC_005", "지원하지 않는 문서 형식입니다."),
    STORED_FILE_NOT_FOUND(404, "DOC_006", "저장된 파일을 찾을 수 없습니다."),

    EXTRACTED_CONTENT_NOT_FOUND(404, "DOC_101", "구조화할 추출 원문이 없습니다. 먼저 텍스트 추출을 실행해주세요."),
    GENERATED_CONTENT_NOT_FOUND(404, "DOC_102", "게시할 AI 구조화 본문이 없습니다."),
    DOCUMENT_EXTRACTION_FAILED(500, "DOC_103", "학습 원본문서 텍스트 추출에 실패했습니다."),
    DOCUMENT_STRUCTURING_FAILED(500, "DOC_104", "학습 원본문서 구조화에 실패했습니다."),
    DOCUMENT_PUBLISH_FAILED(500, "DOC_105", "학습 콘텐츠 게시에 실패했습니다.");

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