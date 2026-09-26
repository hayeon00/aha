import { useCallback } from "react";
import { Link } from "react-router-dom";
import { getConcepts } from "../../concept/api/conceptApi.js";
import { useConceptQuery } from "../../concept/hooks/useConceptQuery.js";
import { contentText, isRecord } from "../../concept/utils/conceptBody.js";
import "./RelatedConcepts.css";

export default function RelatedConcepts({ examVersionId, examScopeNodeId, userExamId }) {
    const fetchConcepts = useCallback(
        (id, signal) => getConcepts(id, signal, examScopeNodeId),
        [examScopeNodeId],
    );
    const { data, loading, error, retry } = useConceptQuery(fetchConcepts, examVersionId);

    if (loading) return <p className="related-concepts-feedback" role="status">관련 개념을 확인하고 있어요.</p>;
    if (error || !Array.isArray(data)) return <div className="related-concepts-feedback" role="status">
        관련 개념을 불러오지 못했어요. <button type="button" onClick={retry}>다시 시도</button>
    </div>;
    const concepts = data.filter((item) => isRecord(item) && item.conceptId != null);
    if (!concepts.length) return null;

    return <section className="related-concepts" aria-label="관련 개념 학습">
        <h3>관련 개념 학습</h3>
        <ul>{concepts.map((concept) => <li key={concept.conceptId}>
            <Link to={`/concepts/${encodeURIComponent(concept.conceptId)}${userExamId ? `?userExamId=${encodeURIComponent(userExamId)}` : ""}`}>
                <strong>{contentText(concept.title) || "핵심 개념"}</strong>
                <span>개념 학습하기 <span aria-hidden="true">→</span></span>
            </Link>
        </li>)}</ul>
    </section>;
}
