import "./UploadProgressModal.css";

const processingSteps = [
    {
        key: "FILE_UPLOADED",
        label: "문서 업로드",
        description: "학습 문서를 안전하게 업로드했어요.",
    },
    {
        key: "TEXT_EXTRACTING",
        label: "내용 추출",
        description: "문서에서 학습할 내용을 읽고 있어요.",
    },
    {
        key: "SCOPE_MAPPING",
        label: "목차 연결",
        description: "문서 내용을 시험 목차와 연결하고 있어요.",
    },
    {
        key: "LEARNING_CONTENT_GENERATING",
        label: "개념 설명 생성",
        description: "목차별 개념 설명을 정리하고 있어요.",
    },
];

const stepOrder = {
    UPLOAD_PENDING: 0,
    FILE_UPLOADED: 1,
    TEXT_EXTRACTING: 2,
    TEXT_EXTRACTED: 2,
    CONTENT_ANALYZING: 2,
    CONTENT_ANALYZED: 2,
    SCOPE_MAPPING: 3,
    SCOPE_MAPPED: 3,
    LEARNING_CONTENT_GENERATING: 4,
    COMPLETED: 5,
};

function UploadProgressModal({
                                 open,
                                 status,
                                 errorMessage,
                                 onClose,
                             }) {
    if (!open || !status) {
        return null;
    }

    const isFailed =
        status.status === "FAILED" ||
        status.status === "PARTIAL_FAILED";

    const isCompleted = status.status === "COMPLETED";

    const progressRate = Math.min(
        100,
        Math.max(0, Number(status.progressRate ?? 0))
    );

    const currentOrder =
        stepOrder[status.currentStep] ?? 0;

    const currentStep =
        processingSteps.find(
            (step) =>
                stepOrder[step.key] === currentOrder
        ) ?? processingSteps[0];

    const modalTitle = isFailed
        ? "문서 처리를 완료하지 못했어요"
        : isCompleted
            ? "개념 설명이 준비됐어요"
            : "개념 설명을 만들고 있어요";

    const modalDescription = isFailed
        ? "처리 중 문제가 발생했습니다. 내용을 확인한 뒤 다시 시도해 주세요."
        : isCompleted
            ? "업로드한 문서를 바탕으로 목차별 설명을 모두 생성했습니다."
            : currentStep.description;

    return (
        <div
            className="upload-progress-backdrop"
            role="presentation"
        >
            <section
                className={`upload-progress-modal ${
                    isFailed ? "failed" : ""
                }`}
                role="dialog"
                aria-modal="true"
                aria-labelledby="upload-progress-title"
            >
                <div className="upload-progress-glow" />

                <div className="upload-progress-header">
                    <div className="upload-progress-heading">
                        <div
                            className={`upload-progress-symbol ${
                                isFailed ? "failed" : ""
                            }`}
                            aria-hidden="true"
                        >
                            {isFailed ? (
                                <span>!</span>
                            ) : (
                                <>
                                    <span className="progress-document">
                                        ▤
                                    </span>
                                    <span className="progress-spark">
                                        ✦
                                    </span>
                                </>
                            )}
                        </div>

                        <div>
                            <span className="upload-progress-eyebrow">
                                AI 개념 학습
                            </span>

                            <h2 id="upload-progress-title">
                                {modalTitle}
                            </h2>

                            <p>{modalDescription}</p>
                        </div>
                    </div>

                    {isFailed && (
                        <button
                            type="button"
                            className="upload-progress-close"
                            aria-label="닫기"
                            onClick={onClose}
                        >
                            ×
                        </button>
                    )}
                </div>

                {!isFailed && (
                    <div className="upload-progress-steps">
                        {processingSteps.map((step, index) => {
                            const order = stepOrder[step.key];
                            const isDone =
                                currentOrder > order ||
                                isCompleted;
                            const isCurrent =
                                currentOrder === order &&
                                !isCompleted;

                            return (
                                <div
                                    className="upload-progress-step-wrap"
                                    key={step.key}
                                >
                                    <div
                                        className={[
                                            "upload-progress-step",
                                            isDone ? "done" : "",
                                            isCurrent ? "current" : "",
                                        ]
                                            .filter(Boolean)
                                            .join(" ")}
                                    >
                                        <span className="step-circle">
                                            {isDone ? "✓" : index + 1}
                                        </span>

                                        <span className="step-label">
                                            {step.label}
                                        </span>
                                    </div>

                                    {index <
                                        processingSteps.length - 1 && (
                                            <span
                                                className={`step-line ${
                                                    isDone ? "done" : ""
                                                }`}
                                            />
                                        )}
                                </div>
                            );
                        })}
                    </div>
                )}

                <div className="upload-progress-summary">
                    <div className="upload-progress-summary-row">
                        <div>
                            <span>현재 진행률</span>
                            <strong>{progressRate}%</strong>
                        </div>

                        <span className="upload-progress-file-count">
                            {status.completedFileCount ?? 0}
                            <em>/</em>
                            {status.totalFileCount ?? 0}개 문서
                        </span>
                    </div>

                    <div
                        className="upload-progress-bar-wrap"
                        aria-label={`진행률 ${progressRate}%`}
                    >
                        <div
                            className={`upload-progress-bar ${
                                isFailed ? "failed" : ""
                            }`}
                            style={{
                                width: `${isFailed ? 100 : progressRate}%`,
                            }}
                        />
                    </div>
                </div>

                {isFailed ? (
                    <>
                        <div className="upload-progress-error">
                            <span aria-hidden="true">!</span>

                            <p>
                                {errorMessage ||
                                    status.errorMessage ||
                                    "문서 처리 중 오류가 발생했습니다."}
                            </p>
                        </div>

                        <button
                            type="button"
                            className="upload-progress-confirm"
                            onClick={onClose}
                        >
                            확인
                        </button>
                    </>
                ) : (
                    <div className="upload-progress-guide">
                        <span className="upload-progress-pulse" />
                        <p>
                            창을 닫지 않아도 처리는 계속 진행돼요.
                            완료되면 개념 설명이 자동으로 표시됩니다.
                        </p>
                    </div>
                )}
            </section>
        </div>
    );
}

export default UploadProgressModal;