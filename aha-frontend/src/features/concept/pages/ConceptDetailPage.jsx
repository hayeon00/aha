import { Link, useParams, useSearchParams } from "react-router-dom";
import { getConcept } from "../api/conceptApi.js";
import { useConceptQuery } from "../hooks/useConceptQuery.js";
import ConceptBlocks from "../components/ConceptBlocks.jsx";
import ConceptState from "../components/ConceptState.jsx";
import { contentText, isRecord } from "../utils/conceptBody.js";
import "./ConceptPage.css";

export default function ConceptDetailPage() {
    const { conceptId } = useParams();
    const [searchParams] = useSearchParams();
    const { data, loading, error, retry } = useConceptQuery(getConcept, conceptId);
    const userExamId = searchParams.get("userExamId");
    const backPath = userExamId ? `/concepts?userExamId=${encodeURIComponent(userExamId)}` : "/concepts";
    const cards = (Array.isArray(data?.cards) ? data.cards.filter(isRecord) : []).slice().sort((a, b) => (Number(a.displayOrder) || 0) - (Number(b.displayOrder) || 0));
    return <div className="concept-page"><div className="concept-container concept-detail">
        <Link className="concept-back" to={backPath}>← 개념 목록</Link>
        {loading ? <ConceptState loading /> : error || !isRecord(data) ? <ConceptState title="개념을 불러오지 못했어요" message="삭제된 개념이거나 일시적인 연결 문제일 수 있어요." onRetry={retry} /> : <>
            <header className="concept-header"><span className="concept-eyebrow">CONCEPT NOTE · {cards.length}개의 학습 카드</span><h1>{contentText(data.title) || "핵심 개념"}</h1>{contentText(data.summary) && <p>{contentText(data.summary)}</p>}</header>
            {cards.length ? <div className="concept-card-stack">{cards.map((card, index) => <article className="concept-detail-card" key={`${conceptId}-${card.cardId ?? index}`}>
                <header><span className="concept-badge">개념 {String(index + 1).padStart(2, "0")}</span><h2>{contentText(card.title) || "개념 설명"}</h2></header>
                <ConceptBlocks body={card.body} />
            </article>)}</div> : <ConceptState title="학습 카드를 준비하고 있어요" message="아직 등록된 상세 설명이 없습니다." />}
        </>}
    </div></div>;
}
