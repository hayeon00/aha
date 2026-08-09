import "./UploadProgressModal.css";

function UploadProgressModal({
                                 open,
                                 currentStatusText,
                                 status,
                                 errorMessage,
                                 onClose,
                                 onRetry,
                                 isRetrying = false,
                             }) {
    if (!open) {
        return null;
    }

    const isFailed = status?.status === "FAILED";

    const statusText =
        currentStatusText ||
        status?.stepMessage ||
        "문서를 처리하고 있어요.";

    return (
        <div className="upload-progress-backdrop">
            <section
                className={`upload-progress-modal ${isFailed ? "failed" : ""}`}
                role="dialog"
                aria-modal="true"
                aria-labelledby="upload-progress-title"
                aria-live="polite"
            >
                {isFailed ? (
                    <>
                        <div className="upload-progress-error-symbol" aria-hidden="true">!</div>
                        <h2 id="upload-progress-title">문서 처리를 완료하지 못했어요</h2>
                        <p className="upload-progress-error-message">
                            {errorMessage || status?.errorMessage || "문서 처리 중 오류가 발생했습니다."}
                        </p>
                        <div className="upload-progress-actions">
                            <button
                                type="button"
                                className="upload-progress-dismiss"
                                onClick={onClose}
                                disabled={isRetrying}
                            >
                                닫기
                            </button>
                            <button
                                type="button"
                                className="upload-progress-confirm"
                                onClick={onRetry}
                                disabled={isRetrying}
                            >
                                {isRetrying ? "재시도 중..." : "재시도"}
                            </button>
                        </div>
                    </>
                ) : (
                    <>
                        <div className="upload-progress-pulse" aria-hidden="true">
                            <span />
                            <span />
                            <span />
                        </div>
                        <h2 id="upload-progress-title">{statusText}</h2>
                    </>
                )}
            </section>
        </div>
    );
}

export default UploadProgressModal;
