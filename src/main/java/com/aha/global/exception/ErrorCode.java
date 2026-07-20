package com.aha.global.exception;

public enum ErrorCode {

    // COMMON
    INVALID_INPUT_VALUE(400, "COMMON_001", "잘못된 입력값입니다."),
    ENTITY_NOT_FOUND(404, "COMMON_002", "대상을 찾을 수 없습니다."),
    INVALID_REQUEST_FORMAT(400, "COMMON_003", "잘못된 요청 형식입니다."),
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

    // DOCUMENT UPLOAD
    DOCUMENT_FILE_COUNT_EXCEEDED(400, "DOCUMENT_UPLOAD_001", "업로드 가능한 문서 파일 개수를 초과했습니다."),
    DOCUMENT_FILE_REQUIRED(400, "DOCUMENT_UPLOAD_002", "업로드할 문서 파일이 필요합니다."),
    DOCUMENT_FILE_EMPTY(400, "DOCUMENT_UPLOAD_003", "빈 문서 파일은 업로드할 수 없습니다."),
    DOCUMENT_FILE_SIZE_EXCEEDED(400, "DOCUMENT_UPLOAD_004", "업로드 가능한 문서 파일 크기를 초과했습니다."),
    DOCUMENT_TOTAL_FILE_SIZE_EXCEEDED(400, "DOCUMENT_UPLOAD_005", "업로드 가능한 전체 문서 파일 크기를 초과했습니다."),
    INVALID_DOCUMENT_FILE_NAME(400, "DOCUMENT_UPLOAD_06", "문서 파일명이 올바르지 않습니다."),
    INVALID_DOCUMENT_FILE_EXTENSION(400, "DOCUMENT_UPLOAD_07", "문서 파일의 확장자가 올바르지 않습니다."),
    INVALID_DOCUMENT_MIME_TYPE(400, "DOCUMENT_UPLOAD_08", "문서의 MIME 타입이 올바르지 않습니다."),


    DOCUMENT_UPLOAD_FAILED(500, "DOCUMENT_UPLOAD_001", "문서 업로드에 실패했습니다."),
    INVALID_DOCUMENT_FILE(400, "DOCUMENT_UPLOAD_002", "지원하지 않는 문서 파일입니다."),

    LEARNING_CONTENT_NOT_FOUND(404, "LEARNING_CONTENT_001", "개념설명을 찾을 수 없습니다."),

    INVALID_EXAM_SCOPE_NODE_TYPE(400, "EXAM_SCOPE_002", "개념설명을 생성할 수 없는 시험 목차 유형입니다."),





    DOCUMENT_PROCESSING_GROUP_NOT_FOUND(404, "DOCUMENT_003", "문서 처리 그룹을 찾을 수 없습니다."),
    DOCUMENT_TEXT_EXTRACTION_FAILED(500, "DOCUMENT_004", "문서 텍스트 추출에 실패했습니다."),
    DOCUMENT_TEXT_EMPTY(422, "DOCUMENT_005", "문서에서 추출할 수 있는 텍스트가 없습니다."),
    DOCUMENT_PROCESSING_NOT_FOUND(404, "DOCUMENT_006", "처리할 문서 정보를 찾을 수 없습니다."),
    INVALID_DOCUMENT_PROCESSING_STATUS(409, "DOCUMENT_007", "현재 문서 처리 상태에서는 요청한 작업을 수행할 수 없습니다."),
    DOCUMENT_CHUNK_NOT_FOUND(404, "DOCUMENT_008", "목차에 매핑할 문서 청크를 찾을 수 없습니다."),
    DOCUMENT_SCOPE_MAPPING_FAILED(500, "DOCUMENT_009", "문서 청크를 시험 목차에 매핑하지 못했습니다."),
    AI_RESPONSE_PARSE_FAILED(500, "DOCUMENT_010", "AI 응답을 처리하는 중 오류가 발생했습니다."),
    DOCUMENT_SCOPE_MAPPING_NOT_FOUND(404, "DOCUMENT_011", "개념 설명 생성에 사용할 목차 매핑 결과를 찾을 수 없습니다."),
    LEARNING_CONTENT_GENERATION_FAILED(500, "DOCUMENT_012", "목차별 개념 설명 생성에 실패했습니다."),
    USER_LEARNING_CONTENT_SAVE_FAILED(500, "DOCUMENT_013", "생성된 개념 설명을 저장하지 못했습니다."),
    USER_LEARNING_CONTENT_NOT_FOUND(404, "DOCUMENT_014", "해당 목차에 생성된 개념 설명이 없습니다."),

    SOURCE_DOCUMENT_NOT_FOUND(404, "DOCUMENT_018", "처리할 원본 문서를 찾을 수 없습니다."),
    INVALID_LEARNING_CONTENT_TARGET(400, "DOCUMENT_019", "개념 설명은 활성화된 최하위 학습 목차에만 생성할 수 있습니다."),

    //EXAM
    EXAM_VERSION_NOT_FOUND(404,"EXAM_VERSION_002","시험 버전을 찾을 수 없습니다."),
    EXAM_VERSION_NOT_ACTIVE(409,"EXAM_VERSION_001","시험 버전을 찾을 수 없습니다."),
    EXAM_NOT_ACTIVE(409,"EXAM_001","해당 시험을 찾을 수 없습니다" ),

    //WORKBOOK
    WORKBOOK_NOT_FOUND(404,"WORKBOOK_001","워크북에 접근할 수 없습니다."),
    WORKBOOK_NOT_PUBLISHED(409,"WORKBOOK_002","워크북에 접근할 수 없습니다." ),
    ATTEMPT_NOT_FOUND(422,"WORKBOOK_004", "워크북에 접근할 수 없습니다."),
    WORKBOOK_ITEM_COUNT_MISMATCH(500,"WORKBOOK_005","워크북에 문제가 발생했습니다." ),
    WORKBOOK_NOT_INCLUDE_PROBLEM(404,"WORKBOOK_006","문제를 찾을 수 없습니다." ),
    WORKBOOK_NOT_MATCH_PAST(500,"WORKBOOK_007","서버 내부 오류입니다."),
    ATTEMPT_EXCEEDED_TIME_LIMIT(422,"WORKBOOK_008","풀이를 진행할 수 없습니다."),
    ATTEMPT_ALREADY_GRADED(409,"WORKBOOK_009" ,"풀이를 진행할 수 없습니다."),
    WORKBOOK_TYPE_NOT_SUPPORTED( 404, "WORKBOOK_010","지원하지 않는 워크북 타입입니다." ),
    UNANSWERED_ITEMS_EXIST(422,"WORKBOOK_011","미응답 문항이 있습니다." ),
    ATTEMPT_NOT_GRADED(409,"WORKBOOK_012" , "채점 완료 상태가 아닙니다."),
    //PROBLEM
    PROBLEM_NOT_FOUND(404,"PROBLEM_001","문제를 찾을 수 없습니다." );

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