import "./UploadProgressModal.css";

const stepLabels = {
    FILE_UPLOADED: "파일 업로드 완료",
    TEXT_EXTRACTING: "텍스트 추출 중",
    TEXT_EXTRACTED: "텍스트 추출 완료",
    CONTENT_ANALYZING: "내용 분석 중",
    CONTENT_ANALYZED: "내용 분석 완료",
    SCOPE_MAPPING: "시험 목차 매핑 중",
    SCOPE_MAPPED: "시험 목차 매핑 완료",
    LEARNING_CONTENT_GENERATING: "개념 정리 생성 중",
    COMPLETED: "처리 완료",
    FAILED: "처리 실패",
};

function UploadProgressModal({ open, status, errorMessage, onClose }) {
    if (!open || !status) {
        return null;
    }

    const isFailed = status.status === "FAILED" || status.status === "PARTIAL_FAILED";
    const currentStep = stepLabels[status.currentStep] || "문서 처리 중";
    const progressRate = Number(status.progressRate || 0);

    return (
        <div className="upload-progress-backdrop" role="presentation">
            <section
                className="upload-progress-modal"
                role="dialog"
                aria-modal="true"
                aria-labelledby="upload-progress-title"
            >
                <div className="upload-progress-header">
                    <div>
                        <h2 id="upload-progress-title">문서 처리 중</h2>
                        <p>{isFailed ? "처리를 완료하지 못했습니다." : currentStep}</p>
                    </div>

                    {isFailed && (
                        <button
                            type="button"
                            className="upload-progress-close"
                            aria-label="닫기"
                            onClick={onClose}
                        >
                            x
                        </button>
                    )}
                </div>

                <div className="upload-progress-bar-wrap" aria-label={`진행률 ${progressRate}%`}>
                    <div
                        className={isFailed ? "upload-progress-bar failed" : "upload-progress-bar"}
                        style={{ width: `${progressRate}%` }}
                    />
                </div>

                <div className="upload-progress-meta">
                    <strong>{progressRate}%</strong>
                    <span>
                        {status.completedFileCount || 0}/{status.totalFileCount || 0}개 완료
                    </span>
                </div>

                {isFailed && (
                    <p className="upload-progress-error">
                        {errorMessage || status.errorMessage || "문서 처리 중 오류가 발생했습니다."}
                    </p>
                )}
            </section>
        </div>
    );
}

export default UploadProgressModal;
