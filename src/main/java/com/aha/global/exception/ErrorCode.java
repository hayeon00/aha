package com.aha.global.exception;

import lombok.Getter;

@Getter
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
    INVALID_REFRESH_TOKEN(401, "AUTH_007", "유효하지 않은 리프레시 토큰입니다."),
    INVALID_SOCIAL_USER_INFO(401, "AUTH_008", "소셜 로그인 사용자 정보를 확인할 수 없습니다."),
    SOCIAL_ACCOUNT_LINK_REQUIRED(409, "AUTH_009", "이미 가입된 이메일입니다. 기존 계정으로 로그인한 후 소셜 계정을 연결해 주세요."),
    NICKNAME_GENERATION_FAILED(500, "AUTH_010", "사용 가능한 닉네임을 생성하지 못했습니다."),
    LOCAL_LOGIN_NOT_AVAILABLE(401, "AUTH_011", "이메일 또는 비밀번호가 일치하지 않습니다."),
    OAUTH_LOGIN_FAILED(401, "AUTH_012", "소셜 로그인에 실패했습니다."),
    UNSUPPORTED_SOCIAL_PROVIDER(400, "AUTH_013", "지원하지 않는 소셜 로그인 제공자입니다."),
    INVALID_OAUTH_AUTHORIZATION_CODE(401, "AUTH_014", "유효하지 않거나 만료된 OAuth 인증 코드입니다."),
    ACCOUNT_NOT_ACTIVE(401, "AUTH_015", "로그인할 수 없는 계정입니다."),
    SOCIAL_EMAIL_REQUIRED(401, "AUTH_016", "소셜 계정의 인증된 이메일 정보가 필요합니다."),


    // USER
    USER_NOT_FOUND(404, "USER_001", "사용자를 찾을 수 없습니다."),
    PROFILE_IMAGE_UPLOAD_FAILED(500, "USER_002", "프로필 이미지 업로드에 실패했습니다."),
    INVALID_PROFILE_IMAGE(400, "USER_003", "지원하지 않는 프로필 이미지 형식입니다."),
    USER_EXAM_ALREADY_CONFIGURED(409, "USER_EXAM_002", "이미 학습할 시험을 설정했습니다."),


    //EXAM
    EXAM_VERSION_NOT_FOUND(404, "EXAM_VERSION_002", "시험 버전을 찾을 수 없습니다."),
    EXAM_VERSION_NOT_ACTIVE(409, "EXAM_VERSION_001", "시험 버전을 찾을 수 없습니다."),
    EXAM_NOT_ACTIVE(409, "EXAM_001", "해당 시험을 찾을 수 없습니다"),
    EXAM_SCOPE_NODE_NOT_FOUND(409, "EXAM_SCOPE_NODE_001", "시험 목차를 찾을 수 없습니다."),


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


    //PROBLEM
    PROBLEM_NOT_FOUND(404, "PROBLEM_001", "문제를 찾을 수 없습니다."),
    PROBLEM_NOT_IN_PAST_PAPER(409, "PROBLEM_002", "문제가 풀고 있는 복원 기출에 포함되지 않았습니다."),

    //PAST_PAPER
    PAST_PAPER_NOT_FOUND(404, "PAST_PAPER_001", "복원 기출을 찾을 수 없습니다."),
    PAST_PAPER_NOT_PUBLISHED(409, "PAST_PAPER_002", "복원 기출이 노출 가능 상태가 아닙니다."),
    PAST_PAPER_ATTEMPT_NOT_FOUND(404, "PAST_PAPER_003", "복원 기출 풀이가 없습니다."),
    PAST_PAPER_ATTEMPT_NOT_YOURS(403, "PAST_PAPER_004", "풀이 소유자가 아닙니다."),
    PAST_PAPER_ATTEMPT_NOT_SOLVING(409, "PAST_PAPER_005", "풀이 중 상태가 아닙니다."),
    PAST_PAPER_ATTEMPT_TIME_EXPIRED(409, "PAST_PAPER_006", "복원 기출 풀이 시간이 만료되었습니다."),
    PAST_PAPER_ATTEMPT_NOT_GRADED(409, "PAST_PAPER_007", "채점 완료 상태가 아닙니다."),

    //STUDY
    STUDY_PARTICIPATION_ALREADY_EXISTS(409, "STUDY_001", "이미 스터디룸에 참가하고 있습니다.");


    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
