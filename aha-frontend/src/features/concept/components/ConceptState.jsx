export default function ConceptState({ loading, title, message, onRetry }) {
    if (loading) {
        return <div className="concept-list" role="status" aria-label="개념을 불러오는 중" aria-busy="true">
            {[0, 1, 2].map((id) => <div className="concept-skeleton" key={id} aria-hidden="true"><i className="concept-skeleton-chip" /><div className="concept-skeleton-copy"><i /><i /></div><i className="concept-skeleton-action" /></div>)}
        </div>;
    }
    return <div className="concept-state" role={onRetry ? "alert" : "status"}>
        <span className="concept-state-icon" aria-hidden="true">{onRetry ? "!" : "◇"}</span>
        <h2>{title}</h2><p>{message}</p>
        {onRetry && <button type="button" className="concept-button" onClick={onRetry}>다시 시도</button>}
    </div>;
}
