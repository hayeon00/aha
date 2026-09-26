import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { useUserExams } from "../../exam/hooks/useUserExams.js";
import { getConcepts } from "../api/conceptApi.js";
import { useConceptQuery } from "../hooks/useConceptQuery.js";
import ConceptState from "../components/ConceptState.jsx";
import ConceptListCard from "../components/ConceptListCard.jsx";
import { contentText, isRecord } from "../utils/conceptBody.js";
import "./ConceptPage.css";

export default function ConceptListPage() {
    const [searchParams] = useSearchParams();
    const [query, setQuery] = useState("");
    const initialUserExamId = Number(searchParams.get("userExamId") || sessionStorage.getItem("activeUserExamId")) || undefined;
    const { selectedUserExamId, selectedExamVersionId, isExamLoading, examMessage, refetchUserExams } = useUserExams({ initialUserExamId });
    const { data, loading, error, retry } = useConceptQuery(getConcepts, isExamLoading ? null : selectedExamVersionId);
    const concepts = Array.isArray(data) ? data.filter((item) => isRecord(item) && item.conceptId != null) : [];
    const isReady = !isExamLoading && !loading && !error && !examMessage && Boolean(selectedExamVersionId) && Array.isArray(data);
    const filtered = concepts.filter((item) => [item.title, item.summary, item.scopeTitle].some((value) => contentText(value).toLowerCase().includes(query.trim().toLowerCase())));
    let content;
    if (isExamLoading || loading) content = <ConceptState loading />;
    else if (examMessage) content = <ConceptState title="시험 정보를 불러오지 못했어요" message={examMessage} onRetry={refetchUserExams} />;
    else if (!selectedExamVersionId) content = <ConceptState title="학습할 시험을 등록해 주세요" message="마이페이지에서 학습할 시험을 선택하면 핵심 개념을 확인할 수 있어요." />;
    else if (error || !Array.isArray(data)) content = <ConceptState title="개념을 불러오지 못했어요" message="잠시 후 다시 시도해 주세요." onRetry={retry} />;
    else if (!filtered.length) content = <ConceptState title={concepts.length ? "검색 결과가 없어요" : "아직 등록된 개념이 없어요"} message={concepts.length ? "다른 개념 이름이나 목차로 검색해 보세요." : "선택한 시험의 개념을 준비하고 있어요. 나중에 다시 확인해 주세요."} />;
    else content = <ul className="concept-list" aria-label="학습 개념 목록">{filtered.map((concept) => <li key={concept.conceptId}>
        <ConceptListCard concept={concept} userExamId={selectedUserExamId} />
    </li>)}</ul>;

    return <div className="concept-page"><div className="concept-container">
        <header className="concept-page-title"><h1>개념 학습</h1></header>
        <div className="concept-list-tools">
            {isReady && <p className="concept-count" role="status">전체 {concepts.length}개 개념 · 표시 {filtered.length}개</p>}
            <div className="concept-search" role="search" aria-label="개념 검색">
                <span className="concept-search-field">
                    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden="true"><circle cx="10.5" cy="10.5" r="6.5" /><path d="m16 16 4 4" strokeLinecap="round" /></svg>
                    <input type="search" aria-label="개념 이름, 요약 또는 목차 검색" placeholder="개념 또는 목차 검색" value={query} onChange={(event) => setQuery(event.target.value)} />
                </span>
            </div>
        </div>
        {content}
        {!isExamLoading && !selectedExamVersionId && !examMessage && <Link className="concept-back" to="/mypage">마이페이지에서 시험 등록 →</Link>}
    </div></div>;
}
