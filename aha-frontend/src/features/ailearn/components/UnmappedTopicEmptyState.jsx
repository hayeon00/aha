import "./UnmappedTopicEmptyState.css";

function EmptyDocumentIcon() {
    return (
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
                d="M6.5 3.5h7l4 4v13h-11v-17Z"
                stroke="currentColor"
                strokeWidth="1.35"
                strokeLinejoin="round"
            />
            <path
                d="M13.5 3.5v4h4M9.5 12h5M9.5 15h3.5"
                stroke="currentColor"
                strokeWidth="1.25"
                strokeLinecap="round"
            />
        </svg>
    );
}

export default function UnmappedTopicEmptyState({ topicTitle, onStartWriting }) {
    return (
        <article className="unmapped-note" aria-labelledby="unmapped-note-title">
            <header className="unmapped-note-header">
                <h2 id="unmapped-note-title">{topicTitle}</h2>
            </header>

            <div className="unmapped-note-divider" />

            <div className="unmapped-note-message" role="status">
                <span className="unmapped-note-icon">
                    <EmptyDocumentIcon />
                </span>
                <div className="unmapped-note-copy">
                    <strong>아직 연결된 내용이 없어요</strong>
                    <p>필요한 개념을 직접 정리해 나만의 노트로 채울 수 있습니다.</p>
                </div>
                <button type="button" onClick={onStartWriting}>
                    <span className="unmapped-note-plus" aria-hidden="true">+</span>
                    빈 노트로 시작
                    <span className="unmapped-note-arrow" aria-hidden="true">→</span>
                </button>
            </div>
        </article>
    );
}
