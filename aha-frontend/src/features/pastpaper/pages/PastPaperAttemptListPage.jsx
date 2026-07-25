import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    getPastPaperAttemptResult,
    getPastPaperAttempts,
    startPastPaperAttempt,
} from "../api/pastPaperApi.js";
import "./PastPaperAttemptListPage.css";

const PAGE_SIZE = 10;
const ATTEMPT_FILTERS = [
    {
        value: "GRADED",
        label: "채점 완료",
        description: "완료한 풀이 결과",
    },
    {
        value: "SOLVING",
        label: "풀이 중",
        description: "이어서 풀 수 있는 문제",
    },
];

const normalizePage = (data) => {
    const pageData = data?.data ?? data ?? {};
    const content = Array.isArray(pageData)
        ? pageData
        : pageData.content ?? pageData.items ?? [];

    return {
        content,
        page: pageData.page ?? pageData.number ?? 0,
        totalPages: pageData.totalPages ?? (content.length > 0 ? 1 : 0),
        totalElements: pageData.totalElements ?? content.length,
        first: pageData.first ?? (pageData.page ?? pageData.number ?? 0) === 0,
        last:
            pageData.last ??
            (pageData.totalPages
                ? (pageData.page ?? pageData.number ?? 0) + 1 >=
                  pageData.totalPages
                : true),
    };
};

const formatDate = (dateValue) => {
    if (!dateValue) {
        return "-";
    }

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
        return "-";
    }

    return new Intl.DateTimeFormat("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
    }).format(date);
};

const formatElapsedTime = (elapsedTime) => {
    if (elapsedTime === null || elapsedTime === undefined) {
        return "-";
    }

    const totalSeconds = Math.max(0, Number(elapsedTime) || 0);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    if (hours > 0) {
        return `${hours}시간 ${minutes}분`;
    }

    if (minutes > 0) {
        return `${minutes}분 ${seconds}초`;
    }

    return `${seconds}초`;
};

function PastPaperAttemptListPage() {
    const navigate = useNavigate();
    const [attemptStatus, setAttemptStatus] = useState("GRADED");
    const [attempts, setAttempts] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState("");
    const [loadingResultId, setLoadingResultId] = useState(null);
    const [resultErrorId, setResultErrorId] = useState(null);
    const [retryingAttemptId, setRetryingAttemptId] = useState(null);
    const [retryErrorId, setRetryErrorId] = useState(null);

    const loadAttempts = useCallback(async () => {
        setIsLoading(true);
        setLoadError("");

        try {
            const response = await getPastPaperAttempts({
                attemptStatus,
                page,
                size: PAGE_SIZE,
            });
            const pageResult = normalizePage(response.data);

            setAttempts(pageResult.content);
            setTotalPages(pageResult.totalPages);
            setTotalElements(pageResult.totalElements);
        } catch (error) {
            console.error("풀이 목록 조회 실패:", error);
            setAttempts([]);
            setLoadError(
                "풀이 기록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."
            );
        } finally {
            setIsLoading(false);
        }
    }, [attemptStatus, page]);

    useEffect(() => {
        queueMicrotask(loadAttempts);
    }, [loadAttempts]);

    const handleOpenResult = async (attempt) => {
        setLoadingResultId(attempt.attemptId);
        setResultErrorId(null);

        sessionStorage.setItem(
            `past-paper-title-${attempt.pastPaperId}`,
            attempt.paperTitle
        );

        try {
            const response = await getPastPaperAttemptResult(
                attempt.attemptId
            );

            navigate(
                `/past-papers/${attempt.pastPaperId}/attempts/${attempt.attemptId}/result`,
                {
                    state: {
                        result: response.data,
                        pastPaperTitle: attempt.paperTitle,
                    },
                }
            );
        } catch (error) {
            console.error("풀이 결과 조회 실패:", error);
            setResultErrorId(attempt.attemptId);
        } finally {
            setLoadingResultId(null);
        }
    };

    const handleContinueAttempt = (attempt) => {
        const attemptData = {
            attemptId: attempt.attemptId,
            attemptStatus: "SOLVING",
            startedAt: attempt.startedAt,
        };

        sessionStorage.setItem(
            `past-paper-title-${attempt.pastPaperId}`,
            attempt.paperTitle
        );
        sessionStorage.setItem(
            `past-paper-attempt-${attempt.attemptId}`,
            JSON.stringify(attemptData)
        );

        navigate(
            `/past-papers/${attempt.pastPaperId}/attempts/${attempt.attemptId}`,
            {
                state: {
                    attempt: attemptData,
                },
            }
        );
    };

    const handleRetryAttempt = async (attempt) => {
        if (!attempt.pastPaperId) {
            setRetryErrorId(attempt.attemptId);
            return;
        }

        setRetryingAttemptId(attempt.attemptId);
        setRetryErrorId(null);

        try {
            const response = await startPastPaperAttempt(
                attempt.pastPaperId
            );
            const nextAttempt = response.data;

            if (!nextAttempt?.attemptId) {
                throw new Error("풀이 시작 응답에 attemptId가 없습니다.");
            }

            sessionStorage.setItem(
                `past-paper-title-${attempt.pastPaperId}`,
                attempt.paperTitle
            );
            sessionStorage.setItem(
                `past-paper-attempt-${nextAttempt.attemptId}`,
                JSON.stringify(nextAttempt)
            );

            navigate(
                `/past-papers/${attempt.pastPaperId}/attempts/${nextAttempt.attemptId}`,
                {
                    state: {
                        attempt: nextAttempt,
                    },
                }
            );
        } catch (error) {
            console.error("기출 문제 다시 풀기 실패:", error);
            setRetryErrorId(attempt.attemptId);
        } finally {
            setRetryingAttemptId(null);
        }
    };

    const handleFilterChange = (status) => {
        if (status === attemptStatus) {
            return;
        }

        setAttemptStatus(status);
        setPage(0);
    };

    const isGraded = attemptStatus === "GRADED";

    const pageNumbers = Array.from(
        { length: totalPages },
        (_, index) => index
    ).filter(
        (pageNumber) =>
            pageNumber === 0 ||
            pageNumber === totalPages - 1 ||
            Math.abs(pageNumber - page) <= 1
    );

    return (
        <main className="attempt-list-page">
            <header className="attempt-list-hero">
                <span className="attempt-list-eyebrow">학습 기록</span>
                <div>
                    <h1>풀이 목록</h1>
                    <p>
                        완료한 결과를 복습하거나 풀던 문제를 이어서 학습해
                        보세요.
                    </p>
                </div>
                {!isLoading && !loadError && (
                    <strong>{totalElements}개의 풀이</strong>
                )}
            </header>

            <div className="attempt-list-filters" role="tablist" aria-label="풀이 상태">
                {ATTEMPT_FILTERS.map((filter) => (
                    <button
                        type="button"
                        role="tab"
                        key={filter.value}
                        className={
                            attemptStatus === filter.value ? "active" : ""
                        }
                        aria-selected={attemptStatus === filter.value}
                        onClick={() => handleFilterChange(filter.value)}
                    >
                        <strong>{filter.label}</strong>
                        <span>{filter.description}</span>
                    </button>
                ))}
            </div>

            <section
                className="attempt-list-section"
                aria-label={isGraded ? "채점 완료된 풀이 목록" : "풀이 중인 목록"}
                aria-busy={isLoading}
            >
                {isLoading && (
                    <div className="attempt-list-skeletons" role="status">
                        <span className="sr-only">풀이 목록을 불러오는 중</span>
                        {Array.from({ length: 3 }, (_, index) => (
                            <div className="attempt-skeleton" key={index}>
                                <i />
                                <i />
                                <i />
                            </div>
                        ))}
                    </div>
                )}

                {!isLoading && loadError && (
                    <div className="attempt-list-state error">
                        <strong>풀이 목록을 불러오지 못했어요.</strong>
                        <p>{loadError}</p>
                        <button type="button" onClick={loadAttempts}>
                            다시 시도
                        </button>
                    </div>
                )}

                {!isLoading && !loadError && attempts.length === 0 && (
                    <div className="attempt-list-state">
                        <span className="attempt-empty-icon" aria-hidden="true">
                            {isGraded ? "✓" : "↻"}
                        </span>
                        <strong>
                            {isGraded
                                ? "아직 완료한 풀이가 없어요."
                                : "진행 중인 풀이가 없어요."}
                        </strong>
                        <p>
                            {isGraded
                                ? "기출 문제를 풀고 나면 이곳에서 결과를 볼 수 있어요."
                                : "새로운 기출 문제 풀이를 시작해 보세요."}
                        </p>
                        <button
                            type="button"
                            onClick={() => navigate("/past-papers")}
                        >
                            기출 문제 풀러 가기
                        </button>
                    </div>
                )}

                {!isLoading && !loadError && attempts.length > 0 && (
                    <div className="attempt-card-list">
                        {attempts.map((attempt) => (
                            <article
                                className="attempt-record-card"
                                key={attempt.attemptId}
                            >
                                <div className="attempt-record-result">
                                    <span
                                        className={`attempt-pass-badge ${
                                            isGraded
                                                ? attempt.passed
                                                    ? "passed"
                                                    : "failed"
                                                : "solving"
                                        }`}
                                    >
                                        {isGraded
                                            ? attempt.passed
                                                ? "합격"
                                                : "불합격"
                                            : "풀이 중"}
                                    </span>
                                    {isGraded ? (
                                        <div className="attempt-score">
                                            <strong>
                                                {attempt.userScore ?? 0}
                                            </strong>
                                            <span>
                                                / {attempt.maxScore ?? 0}점
                                            </span>
                                        </div>
                                    ) : (
                                        <div className="attempt-progress-label">
                                            답안을 저장하고 있어요
                                        </div>
                                    )}
                                </div>

                                <div className="attempt-record-main">
                                    <h2>
                                        {attempt.paperTitle ||
                                            `기출 문제 #${attempt.pastPaperId}`}
                                    </h2>
                                    <div className="attempt-record-meta">
                                        <span>
                                            <small>응시일</small>
                                            {formatDate(attempt.startedAt)}
                                        </span>
                                        <span>
                                            <small>
                                                {isGraded
                                                    ? "소요시간"
                                                    : "상태"}
                                            </small>
                                            {isGraded
                                                ? formatElapsedTime(
                                                      attempt.elapsedTime
                                                  )
                                                : "진행 중"}
                                        </span>
                                    </div>
                                </div>

                                <div className="attempt-record-actions">
                                    {isGraded && attempt.pastPaperId && (
                                        <button
                                            type="button"
                                            className="attempt-retry-button"
                                            disabled={
                                                loadingResultId !== null ||
                                                retryingAttemptId !== null
                                            }
                                            onClick={() =>
                                                handleRetryAttempt(attempt)
                                            }
                                        >
                                            {retryingAttemptId ===
                                            attempt.attemptId
                                                ? "준비 중"
                                                : "다시 풀기"}
                                        </button>
                                    )}
                                    <button
                                        type="button"
                                        className={`attempt-explanation-button ${
                                            loadingResultId ===
                                            attempt.attemptId
                                                ? "is-loading"
                                                : ""
                                        }`}
                                        disabled={
                                            isGraded &&
                                            (loadingResultId !== null ||
                                                retryingAttemptId !== null)
                                        }
                                        onClick={() => {
                                            if (isGraded) {
                                                handleOpenResult(attempt);
                                                return;
                                            }

                                            handleContinueAttempt(attempt);
                                        }}
                                    >
                                        {isGraded
                                            ? loadingResultId ===
                                              attempt.attemptId
                                                ? "결과 불러오는 중"
                                                : "결과 보기"
                                            : "이어서 풀기"}
                                        <span aria-hidden="true">›</span>
                                    </button>
                                </div>
                                {resultErrorId === attempt.attemptId && (
                                    <p className="attempt-result-error">
                                        결과를 불러오지 못했어요. 다시 시도해
                                        주세요.
                                    </p>
                                )}
                                {retryErrorId === attempt.attemptId && (
                                    <p className="attempt-result-error">
                                        풀이를 시작하지 못했어요. 다시 시도해
                                        주세요.
                                    </p>
                                )}
                            </article>
                        ))}
                    </div>
                )}
            </section>

            {!isLoading && !loadError && totalPages > 1 && (
                <nav className="attempt-pagination" aria-label="풀이 목록 페이지">
                    <button
                        type="button"
                        className="pagination-arrow"
                        disabled={page === 0}
                        onClick={() => setPage((current) => current - 1)}
                        aria-label="이전 페이지"
                    >
                        ‹
                    </button>

                    {pageNumbers.map((pageNumber, index) => {
                        const previousPageNumber = pageNumbers[index - 1];
                        const showEllipsis =
                            previousPageNumber !== undefined &&
                            pageNumber - previousPageNumber > 1;

                        return (
                            <span className="pagination-group" key={pageNumber}>
                                {showEllipsis && (
                                    <span className="pagination-ellipsis">
                                        …
                                    </span>
                                )}
                                <button
                                    type="button"
                                    className={
                                        pageNumber === page ? "active" : ""
                                    }
                                    onClick={() => setPage(pageNumber)}
                                    aria-current={
                                        pageNumber === page
                                            ? "page"
                                            : undefined
                                    }
                                >
                                    {pageNumber + 1}
                                </button>
                            </span>
                        );
                    })}

                    <button
                        type="button"
                        className="pagination-arrow"
                        disabled={page + 1 >= totalPages}
                        onClick={() => setPage((current) => current + 1)}
                        aria-label="다음 페이지"
                    >
                        ›
                    </button>
                </nav>
            )}
        </main>
    );
}

export default PastPaperAttemptListPage;
