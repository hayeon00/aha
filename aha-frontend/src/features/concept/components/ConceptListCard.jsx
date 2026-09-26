import { Link } from "react-router-dom";
import { contentText } from "../utils/conceptBody.js";

const STATUS = {
    COMPLETED: { label: "학습 완료", tone: "complete", action: "복습하기", symbol: "✓" },
    IN_PROGRESS: { label: "학습 중", tone: "progress", action: "이어 학습", symbol: "◐" },
    NOT_STARTED: { label: "미시작", tone: "idle", action: "학습하기", symbol: "○" },
};

export default function ConceptListCard({ concept, userExamId }) {
    const status = Object.hasOwn(STATUS, concept.learningStatus)
        ? STATUS[concept.learningStatus]
        : { label: "기록 없음", tone: "idle", action: "학습하기", symbol: "○" };
    const minutes = typeof concept.estimatedMinutes === "number" && Number.isFinite(concept.estimatedMinutes) && concept.estimatedMinutes > 0
        ? `${Math.ceil(concept.estimatedMinutes)}분` : "—";
    const problems = Number.isInteger(concept.problemCount) && concept.problemCount >= 0
        ? `${concept.problemCount}개` : "—";

    return <Link className="concept-list-card" to={`/concepts/${concept.conceptId}?userExamId=${userExamId}`}>
        <svg className="concept-card-pattern" width="180" height="130" viewBox="0 0 180 130" fill="none" aria-hidden="true">
            <g stroke="currentColor" strokeWidth="1.5" strokeDasharray="2 4">
                <rect x="16" y="18" width="58" height="42" rx="8" /><rect x="110" y="70" width="58" height="42" rx="8" />
                <path d="M74 39h22v52h14M26 32h37M26 44h25M120 84h37M120 96h25" />
                <circle cx="96" cy="39" r="4" />
            </g>
        </svg>
        <div className="concept-card-category">
            <span className={`concept-status concept-status-${status.tone}`}><span aria-hidden="true">{status.symbol}</span>{status.label}</span>
            <span className="concept-badge">{contentText(concept.scopeTitle) || "핵심 개념"}</span>
        </div>
        <div className="concept-card-copy">
            <h2>{contentText(concept.title) || "제목 없는 개념"}</h2>
            <p>{contentText(concept.summary) || "개념의 정의와 예시를 살펴보세요."}</p>
        </div>
        <div className="concept-card-side">
            <dl className="concept-card-meta">
                <div><dt><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="8" /><path d="M12 7v5l3 2" /></svg>예상 시간</dt><dd>{minutes}</dd></div>
                <div><dt><svg viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="3" width="14" height="18" rx="3" /><path d="M9 8h6M9 12h6M9 16h3" /></svg>연결 문제</dt><dd>{problems}</dd></div>
            </dl>
            <span className="concept-card-action">{status.action} <span aria-hidden="true">→</span></span>
        </div>
    </Link>;
}
