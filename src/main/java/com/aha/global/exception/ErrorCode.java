package com.aha.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // COMMON
    INVALID_INPUT_VALUE(400, "COMMON_001", "잘못된 입력값입니다."),
    ENTITY_NOT_FOUND(404, "COMMON_002", "대상을 찾을 수 없습니다."),
    INVALID_REQUEST_FORMAT(400, "COMMON_003", "잘못된 요청 형식입니다."),
    UNSUPPORTED_MEDIA_TYPE(415, "COMMON_004", "지원하지 않는 요청 형식입니다."),
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

    DOCUMENT_UPLOAD_FAILED(400, "DOCUMENT_UPLOAD_FAILED_001","파일 업로드에 실패했습니다."),

    // DOCUMENT FILE VALIDATION
    DOCUMENT_FILE_COUNT_EXCEEDED(400, "DOCUMENT_UPLOAD_001", "업로드 가능한 문서 파일 개수를 초과했습니다."),
    DOCUMENT_FILE_REQUIRED(400, "DOCUMENT_UPLOAD_002", "업로드할 문서 파일이 필요합니다."),
    DOCUMENT_FILE_EMPTY(422, "DOCUMENT_UPLOAD_003", "빈 문서 파일은 업로드할 수 없습니다."),
    DOCUMENT_FILE_SIZE_EXCEEDED(413, "DOCUMENT_UPLOAD_004", "업로드 가능한 문서 파일 크기를 초과했습니다."),
    DOCUMENT_TOTAL_FILE_SIZE_EXCEEDED(413, "DOCUMENT_UPLOAD_005", "업로드 가능한 전체 문서 파일 크기를 초과했습니다."),
    DOCUMENT_FILE_NAME_INVALID(422, "DOCUMENT_UPLOAD_006", "문서 파일명이 올바르지 않습니다."),
    DOCUMENT_FILE_EXTENSION_INVALID(422, "DOCUMENT_UPLOAD_007", "PDF 또는 DOCX 파일만 업로드할 수 있습니다."),
    DOCUMENT_FILE_MIME_TYPE_INVALID(422, "DOCUMENT_UPLOAD_008", "파일 확장자와 실제 문서 형식이 일치하지 않습니다."),
    DOCUMENT_STORAGE_FAILED(500, "DOCUMENT_UPLOAD_009", "문서 파일을 저장하지 못했습니다."),
    DOCUMENT_FILE_UNREADABLE(422, "DOCUMENT_UPLOAD_010", "손상되었거나 읽을 수 없는 문서 파일입니다."),
    DOCUMENT_REQUEST_SIZE_EXCEEDED(413, "DOCUMENT_UPLOAD_011", "업로드 요청의 전체 크기를 초과했습니다."),
    DOCUMENT_FILE_VALIDATION_FAILED(422, "DOCUMENT_UPLOAD_012", "업로드할 수 없는 파일이 있습니다."),


    // DOCUMENT PROCESSING
    DOCUMENT_PROCESSING_FAILED(500, "DOCUMENT_002", "문서 처리에 실패했습니다."),
    DOCUMENT_TEXT_EXTRACTION_FAILED(500, "DOCUMENT_004", "문서 텍스트 추출에 실패했습니다."),
    DOCUMENT_TEXT_EMPTY(422, "DOCUMENT_005", "문서에서 추출할 수 있는 텍스트가 없습니다."),
    DOCUMENT_PROCESSING_NOT_FOUND(404, "DOCUMENT_006", "처리할 문서 정보를 찾을 수 없습니다."),
    INVALID_DOCUMENT_PROCESSING_STATUS(409, "DOCUMENT_007", "현재 문서 처리 상태에서는 요청한 작업을 수행할 수 없습니다."),
    DOCUMENT_CHUNK_NOT_FOUND(404, "DOCUMENT_008", "목차에 매핑할 문서 청크를 찾을 수 없습니다."),
    DOCUMENT_SCOPE_MAPPING_FAILED(500, "DOCUMENT_009", "문서 청크를 시험 목차에 매핑하지 못했습니다."),
    AI_RESPONSE_PARSE_FAILED(500, "DOCUMENT_010", "AI 응답을 처리하는 중 오류가 발생했습니다."),
    DOCUMENT_SCOPE_MAPPING_NOT_FOUND(404, "DOCUMENT_011", "개념 설명 생성에 사용할 목차 매핑 결과를 찾을 수 없습니다."),
    LEARNING_CONTENT_GENERATION_FAILED(500, "DOCUMENT_012", "목차별 개념 설명 생성에 실패했습니다."),
    OPENAI_CREDIT_EXHAUSTED(503, "DOCUMENT_013", "AI 분석 사용량이 소진되었습니다. API 크레딧을 충전한 뒤 다시 시도해 주세요."),


    SOURCE_DOCUMENT_NOT_FOUND(404, "DOCUMENT_018", "처리할 원본 문서를 찾을 수 없습니다."),
    LEARNING_NOTE_NOT_FOUND(404, "LEARNING_NOTE_001", "학습노트를 찾을 수 없습니다."),
    LEARNING_NOTE_CONTENT_NOT_FOUND(404, "LEARNING_NOTE_002", "학습노트의 개념 설명을 찾을 수 없습니다."),
    LEARNING_NOTE_NOT_EDITABLE(409, "LEARNING_NOTE_003", "완료된 학습노트만 수정할 수 있습니다."),



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
    STUDY_PARTICIPATION_ALREADY_EXISTS(409, "STUDY_001", "이미 스터디룸에 참가하고 있습니다."),
    STUDY_ROOM_ALREADY_JOINED(409,"STUDY_002","스터디룸에 이미 참가했습니다."),
    STUDY_ROOM_NOT_FOUND(404,"STUDY_003","스터디룸이 존재하지 않습니다."),
    JOINED_STUDY_ROOM_NOT_FOUND(404,"STUDY_004" ,"참가하고 있는 스터디가 없습니다." ),
    STUDY_ROOM_ALREADY_CANCELED(409,"STUDY_005" ,"스터디룸이 취소되었습니다." ),
    STUDY_ROOM_ALREADY_FULL(409,"STUDY_006" ,"스터디룸 정원이 전부 찼습니다." ),
    STUDY_ROOM_ALREADY_SOLVING(409,"STUDY_007" ,"스터디룸은 풀이 중 상태입니다." ),
    REQUESTER_NOT_STUDY_ROOM_MEMBER(403, "STUDY_008", "해당 스터디룸에 참여하고 있지 않습니다."),
    STUDY_ROOM_MEMBER_ROLE_MUST_BE_MEMBER(403,"STUDY_009" ,"방장이 아닌 멤버여야 합니다" ),
    STUDY_ROOM_ALREADY_FEEDBACK(409,"STUDY_010","스터디룸은 피드백 중입니다." ),
    STUDY_ROOM_MEMBER_ROLE_MUST_BE_HOST(403,"STUDY_011" ,"방장이어야 합니다."),
    STUDY_ROOM_HOST_CANNOT_KICK_SELF(403, "STUDY_012", "방장은 자기 자신을 강퇴할 수 없습니다."),
    STUDY_ROOM_TARGET_NOT_FOUND(404, "STUDY_013", "대상자를 찾을 수 없습니다."),
    STUDY_ROOM_HOST_CANNOT_DELEGATE_SELF(403,"STUDY_014" ,"방장은 자기 자신에게 위임할 수 없습니다."),
    STUDY_ROOM_INSUFFICIENT_MEMBERS(409,"STUDY_015" ,"스터디룸 최소인원 미달입니다." ),
    STUDY_ROOM_ALL_MEMBER_NOT_READY(409,"STUDY_016" ,"스터디룸 멤버 전원이 준비상태여야 합니다." ),
    STUDY_ROOM_WAITING(409,"STUDY_017" ,"스터디룸이 대기 중이라 풀이에 접근할 수 없습니다." ),
    STUDY_ROOM_ATTEMPT_NOT_FOUND(404,"STUDY_018" ,"사용자에 대한 스터디룸 풀이를 찾을 수 없습니다." );

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
