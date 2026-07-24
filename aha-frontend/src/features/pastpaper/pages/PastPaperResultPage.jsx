import { useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { getStoredPastPaperResult } from "../api/pastPaperApi.js";
import "./PastPaperResultPage.css";

const getFailureDescription = (failureReason) => {
    if (failureReason.code === "TOTAL_SCORE_BELOW_MINIMUM") {
        return `획득 점수 ${failureReason.userScore}점 / 합격 기준 ${failureReason.requiredScore}점`;
    }

    if (failureReason.code === "SUBJECT_SCORE_BELOW_MINIMUM") {
        return (failureReason.failedParts || [])
            .map(
                (part) =>
                    `${part.name} ${part.userScore}점 / 과락 기준 ${part.requiredScore}점`
            )
            .join(" · ");
    }

    return failureReason.name;
};

const getFailureLabel = (failureReason) => {
    if (failureReason.code === "TOTAL_SCORE_BELOW_MINIMUM") {
        return "총점 미달";
    }

    if (failureReason.code === "SUBJECT_SCORE_BELOW_MINIMUM") {
        return "과락 발생";
    }

    return "불합격 사유";
};

const getPartDisplayName = (part) =>
    part.name?.replace(/^\s*\d+\s*과목\s*/, "") || part.name;

function PastPaperResultPage() {
    const { pastPaperId, attemptId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const [result, setResult] = useState(
        () => location.state?.result ?? getStoredPastPaperResult(attemptId)
    );

    const pastPaperTitle =
        location.state?.pastPaperTitle ||
        sessionStorage.getItem(`past-paper-title-${pastPaperId}`) ||
        `기출 문제 #${pastPaperId}`;

    const partResults = useMemo(
        () => result?.partResults || [],
        [result]
    );

    const reloadStoredResult = () => {
        setResult(getStoredPastPaperResult(attemptId));
    };

    if (!result) {
        return (
            <div className="result-state-card error">
                <strong>결과를 표시할 수 없습니다.</strong>
                <div className="result-state-actions">
                    <button type="button" onClick={reloadStoredResult}>
                        다시 시도
                    </button>
                    <button
                        type="button"
                        className="secondary"
                        onClick={() => navigate("/past-papers")}
                    >
                        목록으로
                    </button>
                </div>
            </div>
        );
    }

    const scoreRate =
        result.maxScore > 0
            ? Math.round((result.userScore / result.maxScore) * 100)
            : 0;
    const sectionResults = partResults.flatMap((part) =>
        (part.sectionResults || []).map((section) => ({
            ...section,
            partCode: part.code,
        }))
    );
    return (
        <div className="workbook-result-page">
            <header className="result-page-header">
                <div className="result-title-block">
                    <span>채점 결과</span>
                    <h1>{pastPaperTitle}</h1>
                </div>
                <section className="pill-summary-bar">
                    <div className="pill-summary-badges">
                        <span
                            className={`pill-summary-item verdict ${
                                result.passed ? "passed" : "failed"
                            }`}
                        >
                            {result.passed ? "합격" : "불합격"}
                        </span>
                        <span className="pill-summary-item score">
                            <strong>총점</strong>
                            {result.userScore}점 / {result.maxScore}점
                        </span>
                        <span className="pill-summary-item rate">
                            득점률 {scoreRate}%
                        </span>
                    </div>
                </section>
            </header>

            <main className="pill-result-dashboard">
                <section className="pill-result-card-grid">
                    {!result.passed &&
                        (result.failureReasons || []).length > 0 && (
                        <article className="pill-result-card failure">
                            <h2>불합격 분석</h2>
                            <div className="pill-result-list">
                                {result.failureReasons.map((failureReason) => (
                                    <div
                                        className={`failure-reason-block ${
                                            failureReason.code ===
                                            "TOTAL_SCORE_BELOW_MINIMUM"
                                                ? "total-score"
                                                : "subject-score"
                                        }`}
                                        key={failureReason.code}
                                    >
                                        <header>
                                            <span className="pill-row-badge failure">
                                                {getFailureLabel(failureReason)}
                                            </span>
                                            {failureReason.code ===
                                                "TOTAL_SCORE_BELOW_MINIMUM" && (
                                                <span className="failure-value">
                                                    <strong>
                                                        {
                                                            failureReason.userScore
                                                        }
                                                        점
                                                    </strong>
                                                    <small>
                                                        /{" "}
                                                        {
                                                            failureReason.requiredScore
                                                        }
                                                        점 기준
                                                    </small>
                                                </span>
                                            )}
                                        </header>
                                        {failureReason.code ===
                                        "SUBJECT_SCORE_BELOW_MINIMUM" ? (
                                            <div className="failure-key-value-list">
                                                {(failureReason.failedParts || []).map(
                                                    (part) => (
                                                        <div
                                                            className="failure-key-value-row"
                                                            key={
                                                                part.code ||
                                                                part.name
                                                            }
                                                        >
                                                            <span className="failure-key">
                                                                {part.name}
                                                            </span>
                                                            <span className="failure-value">
                                                                <strong>
                                                                    {
                                                                        part.userScore
                                                                    }
                                                                    점
                                                                </strong>
                                                                <small>
                                                                    /{" "}
                                                                    {
                                                                        part.requiredScore
                                                                    }
                                                                    점 기준
                                                                </small>
                                                            </span>
                                                        </div>
                                                    )
                                                )}
                                            </div>
                                        ) : failureReason.code ===
                                          "TOTAL_SCORE_BELOW_MINIMUM" ? null : (
                                            <p>
                                                {getFailureDescription(
                                                    failureReason
                                                )}
                                            </p>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </article>
                    )}

                    <article className="pill-result-card sections">
                        <h2>세부 영역별 결과</h2>
                        <div className="pill-result-list">
                            {sectionResults.map((section) => (
                                <div
                                    className="pill-result-row"
                                    key={`${section.partCode}-${section.code}`}
                                >
                                    <span className="section-name-badge">
                                        {section.name}
                                    </span>
                                    <strong className="result-row-score">
                                        <span className="result-earned-score">
                                            {section.userScore}
                                        </span>
                                        <span className="result-max-score">
                                            {" "}
                                            / {section.maxScore}점
                                        </span>
                                    </strong>
                                </div>
                            ))}
                        </div>
                    </article>

                    <article className="pill-result-card parts">
                        <h2>과목별 결과</h2>
                        <div className="pill-result-list">
                            {partResults.map((part, index) => (
                                <div className="pill-result-row" key={part.code}>
                                    <span className="pill-row-badge part">
                                        {index + 1}과목
                                    </span>
                                    <p>{getPartDisplayName(part)}</p>
                                    <strong className="result-row-score">
                                        <span className="result-earned-score">
                                            {part.userScore}
                                        </span>
                                        <span className="result-max-score">
                                            {" "}
                                            / {part.maxScore}점
                                        </span>
                                    </strong>
                                </div>
                            ))}
                        </div>
                    </article>
                </section>

                <section className="pill-result-actions">
                    <button
                        type="button"
                        className="secondary"
                        onClick={() =>
                            navigate(
                                `/past-papers/${pastPaperId}/attempts/${attemptId}/explanation`,
                                { state: { pastPaperTitle } }
                            )
                        }
                    >
                        문항별 해설
                    </button>
                    <button
                        type="button"
                        onClick={() => navigate("/past-papers")}
                    >
                        기출 문제 목록으로
                    </button>
                </section>
            </main>
        </div>
    );
}

export default PastPaperResultPage;
