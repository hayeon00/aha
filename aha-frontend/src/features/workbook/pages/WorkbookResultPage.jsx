import { useCallback, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { getWorkbookAttemptResult } from "../api/workbookApi.js";
import "./WorkbookResultPage.css";

const getResultErrorMessage = (errorCode) => {
    switch (errorCode) {
        case "WORKBOOK_012":
            return "아직 채점이 완료되지 않았습니다.";
        case "WORKBOOK_004":
            return "결과를 찾을 수 없거나 접근할 수 없습니다.";
        default:
            return "결과를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.";
    }
};

const formatElapsedTime = (elapsedTime) => {
    const totalSeconds = Math.max(Number(elapsedTime) || 0, 0);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    if (hours > 0) {
        return `${hours}시간 ${minutes}분 ${String(seconds).padStart(2, "0")}초`;
    }
    return `${minutes}분 ${String(seconds).padStart(2, "0")}초`;
};

function WorkbookResultPage() {
    const { workbookId, attemptId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const [result, setResult] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState(null);

    const workbookTitle =
        location.state?.workbookTitle ||
        sessionStorage.getItem(`workbook-title-${workbookId}`) ||
        `문제집 #${workbookId}`;

    const loadResult = useCallback(async () => {
        setIsLoading(true);
        setLoadError(null);
        try {
            const response = await getWorkbookAttemptResult(attemptId);
            setResult(response.data);
        } catch (error) {
            console.error("문제집 결과 조회 실패:", error);
            setLoadError(getResultErrorMessage(error.errorCode));
        } finally {
            setIsLoading(false);
        }
    }, [attemptId]);

    useEffect(() => {
        queueMicrotask(loadResult);
    }, [loadResult]);

    const partResults = useMemo(
        () => [...(result?.partResults || [])].sort(
            (first, second) => first.displayOrder - second.displayOrder
        ),
        [result]
    );

    if (isLoading) {
        return (
            <div className="result-state-card" role="status">
                <span className="result-loader" />
                <strong>채점 결과를 불러오고 있습니다.</strong>
            </div>
        );
    }

    if (loadError || !result) {
        return (
            <div className="result-state-card error">
                <strong>{loadError || "결과를 표시할 수 없습니다."}</strong>
                <div className="result-state-actions">
                    <button type="button" onClick={loadResult}>다시 시도</button>
                    <button type="button" className="secondary" onClick={() => navigate("/problems")}>목록으로</button>
                </div>
            </div>
        );
    }

    const scoreRate = result.totalScore > 0
        ? Math.round((result.userScore / result.totalScore) * 100)
        : 0;

    return (
        <div className="workbook-result-page">
            <header className="result-page-header">
                <div>
                    <span>채점 결과</span>
                    <h1>{workbookTitle}</h1>
                </div>
                <button type="button" onClick={() => navigate("/problems")}>문제집 목록</button>
            </header>

            <main className="result-content">
                <section className={`result-summary-card ${result.passed ? "passed" : "failed"}`}>
                    <div className="result-verdict">
                        <span>{result.passed ? "PASS" : "RESULT"}</span>
                        <strong>{result.passed ? "합격입니다" : "아쉽지만 불합격입니다"}</strong>
                        <p>{result.passed ? "꾸준히 쌓아온 실력이 좋은 결과로 이어졌어요." : "과목별 결과를 확인하고 부족한 부분을 다시 학습해 보세요."}</p>
                    </div>
                    <div className="result-score-ring" style={{ "--score-rate": `${scoreRate * 3.6}deg` }}>
                        <div>
                            <strong>{result.userScore}</strong>
                            <span>/ {result.totalScore}점</span>
                        </div>
                    </div>
                    <dl className="result-summary-meta">
                        <div><dt>득점률</dt><dd>{scoreRate}%</dd></div>
                        <div><dt>풀이 시간</dt><dd>{formatElapsedTime(result.elapsedTime)}</dd></div>
                    </dl>
                </section>

                <section className="result-parts-section">
                    <div className="result-section-heading">
                        <div><span>상세 분석</span><h2>과목별 결과</h2></div>
                        <p>과목과 세부 영역별 점수를 확인해 보세요.</p>
                    </div>
                    <div className="result-part-list">
                        {partResults.map((part) => {
                            const sections = [...(part.sectionResults || [])].sort(
                                (first, second) => first.displayOrder - second.displayOrder
                            );
                            const partRate = part.totalScore > 0
                                ? Math.round((part.userScore / part.totalScore) * 100)
                                : 0;

                            return (
                                <article className="result-part-card" key={part.id}>
                                    <header>
                                        <div><span>과목</span><h3>{part.name}</h3></div>
                                        <strong>{part.userScore}<small> / {part.totalScore}점</small></strong>
                                    </header>
                                    <div className="result-progress" aria-label={`${partRate}% 득점`}>
                                        <span style={{ width: `${Math.min(partRate, 100)}%` }} />
                                    </div>
                                    <div className="result-section-list">
                                        {sections.map((section) => (
                                            <div className="result-section-row" key={section.id}>
                                                <div><strong>{section.title}</strong><span>{section.correctQuestionCount}/{section.totalQuestionCount}문항 정답</span></div>
                                                <span>{section.userScore} / {section.totalScore}점</span>
                                            </div>
                                        ))}
                                    </div>
                                </article>
                            );
                        })}
                    </div>
                </section>
            </main>
        </div>
    );
}

export default WorkbookResultPage;
