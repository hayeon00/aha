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

    // EXAM
    EXAM_NOT_FOUND(404, "EXAM_001", "시험 정보를 찾을 수 없습니다."),
    EXAM_VERSION_NOT_FOUND(404, "EXAM_002", "시험 버전 정보를 찾을 수 없습니다."),
    EXAM_PART_NOT_FOUND(404, "EXAM_003", "시험 과목 정보를 찾을 수 없습니다."),
    EXAM_SCOPE_NODE_NOT_FOUND(404, "EXAM_004", "시험 목차를 찾을 수 없습니다."),

    // LEARNING CONTENT
    LEARNING_CONTENT_UNIT_NOT_FOUND(404, "LEARN_001", "학습 콘텐츠 단위를 찾을 수 없습니다."),
    LEARNING_CONTENT_UNIT_ITEM_NOT_FOUND(404, "LEARN_002", "학습 콘텐츠 단위에 연결된 세부 개념이 없습니다."),
    LEARNING_CONTENT_NOT_FOUND(404, "LEARN_003", "학습 콘텐츠를 찾을 수 없습니다."),
    PUBLISHED_LEARNING_CONTENT_NOT_FOUND(404, "LEARN_004", "공개된 학습 콘텐츠가 없습니다."),
    INVALID_LEARNING_CONTENT_UNIT(400, "LEARN_005", "잘못된 학습 콘텐츠 단위입니다."),

    // DOCUMENT
    SOURCE_DOCUMENT_NOT_FOUND(404, "DOC_001", "학습 원본문서를 찾을 수 없습니다."),
    SOURCE_DOCUMENT_SCOPE_REQUIRED(400, "DOC_002", "목차가 연결되지 않은 문서는 게시할 수 없습니다."),
    FILE_EMPTY(400, "DOC_003", "업로드할 파일이 없습니다."),
    FILE_SAVE_FAILED(500, "DOC_004", "파일 저장 중 오류가 발생했습니다."),
    UNSUPPORTED_DOCUMENT_TYPE(400, "DOC_005", "지원하지 않는 문서 형식입니다."),
    STORED_FILE_NOT_FOUND(404, "DOC_006", "저장된 파일을 찾을 수 없습니다."),
    EXTRACTED_CONTENT_NOT_FOUND(404, "DOC_101", "구조화할 추출 원문이 없습니다. 먼저 텍스트 추출을 실행해주세요."),
    DOCUMENT_EXTRACTION_FAILED(500, "DOC_102", "학습 원본문서 텍스트 추출에 실패했습니다."),
    DOCUMENT_STRUCTURING_FAILED(500, "DOC_103", "학습 원본문서 구조화에 실패했습니다."),
    DOCUMENT_PUBLISH_FAILED(500, "DOC_104", "학습 콘텐츠 게시에 실패했습니다."),

    // AI GENERATION
    GENERATED_CONTENT_NOT_FOUND(404, "GEN_001", "생성된 AI 학습 콘텐츠 초안이 없습니다."),
    UNSUPPORTED_GENERATION_TYPE(400, "GEN_002", "지원하지 않는 콘텐츠 생성 방식입니다."),
    INVALID_GENERATION_REQUEST(400, "GEN_003", "잘못된 콘텐츠 생성 요청입니다."),
    AI_RESPONSE_EMPTY(502, "GEN_004", "AI 응답이 비어 있습니다."),
    AI_CONTENT_GENERATION_FAILED(500, "GEN_005", "AI 학습 콘텐츠 생성에 실패했습니다."),
    AI_RESPONSE_PARSE_FAILED(500, "GEN_006", "AI 응답을 해석하는 중 오류가 발생했습니다."),
    GENERATED_CONTENT_ALREADY_PUBLISHED(409, "GEN_007", "이미 게시된 AI 학습 콘텐츠입니다."),
    GENERATED_CONTENT_REJECTED(400, "GEN_008", "반려된 AI 학습 콘텐츠는 게시할 수 없습니다."),
    AI_GENERATED_CONTENT_TOO_SHORT(500, "GEN_009", "AI가 생성한 학습 콘텐츠가 너무 짧습니다."),
    INVALID_GENERATION_TARGET_CONTENT_UNIT(400, "GEN_010", "AI 초안 생성 대상 학습 콘텐츠 단위가 올바르지 않습니다.");

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