import { useCallback, useEffect, useMemo, useState } from "react";
import ReactMarkdown from "react-markdown";
import { refractor } from "refractor/core";
import sql from "refractor/sql";
import rehypePrismGenerator from "rehype-prism-plus/generator";
import remarkGfm from "remark-gfm";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import {
    getPastPaperAttemptAnswers,
    getPastPaperItems,
    savePastPaperAnswer,
    submitPastPaperAttempt,
    togglePastPaperReviewMark,
} from "../api/pastPaperApi.js";
import "./PastPaperAttemptPage.css";

refractor.register(sql);
const rehypePrism = rehypePrismGenerator(refractor);

const getLoadErrorMessage = (errorCode) => {
    switch (errorCode) {
        case "PAST_PAPER_003":
            return "풀이 기록을 찾을 수 없습니다.";
        case "PAST_PAPER_004":
            return "이 풀이 기록을 찾을 수 없거나 접근할 수 없습니다.";
        case "PAST_PAPER_002":
            return "현재 공개되지 않은 기출 문제입니다.";
        case "EXAM_VERSION_001":
            return "시험 버전이 변경되어 풀이를 계속할 수 없습니다.";
        case "EXAM_001":
            return "현재 이용할 수 없는 시험입니다.";
        case "PROBLEM_001":
            return "문항 구성을 확인하는 중 문제가 발생했습니다.";
        default:
            return "풀이 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.";
    }
};

const getInteractionErrorMessage = (errorCode) => {
    switch (errorCode) {
        case "PAST_PAPER_003":
            return "풀이 기록을 찾을 수 없습니다.";
        case "PAST_PAPER_006":
            return "제한 시간이 지나 답안을 변경할 수 없습니다.";
        case "PAST_PAPER_005":
            return "이미 채점이 완료된 풀이입니다.";
        case "PROBLEM_001":
        case "PROBLEM_002":
            return "현재 문제 정보를 확인할 수 없습니다.";
        case "PAST_PAPER_002":
        case "PAST_PAPER_004":
            return "풀이 기록을 확인할 수 없습니다.";
        default:
            return "변경사항을 저장하지 못했습니다. 다시 시도해 주세요.";
    }
};

const getSubmitErrorMessage = (errorCode) => {
    switch (errorCode) {
        case "PAST_PAPER_003":
            return "풀이 기록을 찾을 수 없습니다.";
        case "PAST_PAPER_006":
            return "제한 시간이 지나 제출할 수 없습니다.";
        case "PAST_PAPER_005":
            return "이미 채점이 완료된 풀이입니다.";
        case "PAST_PAPER_002":
        case "PAST_PAPER_004":
            return "풀이 기록을 확인할 수 없습니다.";
        default:
            return "제출하지 못했습니다. 잠시 후 다시 시도해 주세요.";
    }
};

const formatRemainingTime = (remainingSeconds) => {
    const safeSeconds = Math.max(remainingSeconds, 0);
    const hours = Math.floor(safeSeconds / 3600);
    const minutes = Math.floor((safeSeconds % 3600) / 60);
    const seconds = safeSeconds % 60;

    return [hours, minutes, seconds]
        .map((value) => String(value).padStart(2, "0"))
        .join(":");
};

const getStoredAttempt = (attemptId) => {
    try {
        return JSON.parse(
            sessionStorage.getItem(`past-paper-attempt-${attemptId}`)
        );
    } catch {
        return null;
    }
};

function PastPaperAttemptPage() {
    const { pastPaperId, attemptId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const [items, setItems] = useState([]);
    const [answers, setAnswers] = useState([]);
    const [currentIndex, setCurrentIndex] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState(null);
    const [pendingProblemId, setPendingProblemId] = useState(null);
    const [interactionError, setInteractionError] = useState(null);
    const [submitError, setSubmitError] = useState(null);
    const [isSubmitConfirmOpen, setIsSubmitConfirmOpen] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const attempt =
        location.state?.attempt ?? getStoredAttempt(attemptId);
    const [remainingSeconds, setRemainingSeconds] = useState(0);

    const loadAttempt = useCallback(async () => {
        setIsLoading(true);
        setLoadError(null);

        try {
            const [itemsResponse, answersResponse] = await Promise.all([
                getPastPaperItems(attemptId),
                getPastPaperAttemptAnswers(attemptId),
            ]);
            const sortedItems = [...itemsResponse.data]
                .sort((first, second) => first.sortOrder - second.sortOrder)
                .map((item) => ({
                    ...item,
                    problemChoiceResponses: [
                        ...(item.problemChoiceResponses || []),
                    ].sort(
                        (first, second) =>
                            first.sortOrder - second.sortOrder
                    ),
                }));

            setItems(sortedItems);
            setAnswers(answersResponse.data?.userAnswerResponses || []);
            setCurrentIndex((index) =>
                Math.min(index, Math.max(sortedItems.length - 1, 0))
            );
        } catch (error) {
            console.error("문제 풀이 정보 조회 실패:", error);
            setLoadError(getLoadErrorMessage(error.errorCode));
        } finally {
            setIsLoading(false);
        }
    }, [attemptId]);

    useEffect(() => {
        queueMicrotask(() => {
            loadAttempt();
        });
    }, [loadAttempt]);

    useEffect(() => {
        if (!attempt?.dueAt) {
            return undefined;
        }

        const updateRemainingTime = () => {
            const dueAt = new Date(attempt.dueAt).getTime();
            setRemainingSeconds(
                Math.max(Math.ceil((dueAt - Date.now()) / 1000), 0)
            );
        };

        updateRemainingTime();
        const timerId = window.setInterval(updateRemainingTime, 1000);
        return () => window.clearInterval(timerId);
    }, [attempt?.dueAt]);

    const answersByProblemId = useMemo(
        () => new Map(answers.map((answer) => [answer.problemId, answer])),
        [answers]
    );

    const answerSheetItems = useMemo(
        () =>
            items.map((item) => ({
                sortOrder: item.sortOrder,
                problemId: item.problemId,
                userAnswer:
                    answersByProblemId.get(item.problemId)?.userAnswer ?? null,
                checked:
                    answersByProblemId.get(item.problemId)?.checked ?? false,
            })),
        [answersByProblemId, items]
    );

    const answeredCount = answerSheetItems.filter((item) => item.userAnswer).length;
    const pastPaperTitle =
        location.state?.pastPaperTitle ||
        sessionStorage.getItem(`past-paper-title-${pastPaperId}`) ||
        `기출 문제 #${pastPaperId}`;

    const moveToProblem = (nextIndex) => {
        if (nextIndex < 0 || nextIndex >= items.length) {
            return;
        }
        setCurrentIndex(nextIndex);
        setInteractionError(null);
    };

    const updateAnswerState = (problemId, updater) => {
        setAnswers((currentAnswers) => {
            const existingAnswer = currentAnswers.find(
                (answer) => answer.problemId === problemId
            );
            const nextAnswer = updater(existingAnswer || {
                problemId,
                userAnswer: null,
                correct: null,
                checked: false,
            });

            if (existingAnswer) {
                return currentAnswers.map((answer) =>
                    answer.problemId === problemId ? nextAnswer : answer
                );
            }
            return [...currentAnswers, nextAnswer];
        });
    };

    const handleSelectAnswer = async (problemId, choiceOrder) => {
        if (pendingProblemId !== null) {
            return;
        }
        setPendingProblemId(problemId);
        setInteractionError(null);

        try {
            const userAnswer = String(choiceOrder);
            await savePastPaperAnswer({ attemptId, problemId, userAnswer });
            updateAnswerState(problemId, (answer) => ({
                ...answer,
                userAnswer,
            }));
        } catch (error) {
            console.error("사용자 답안 저장 실패:", error);
            setInteractionError(getInteractionErrorMessage(error.errorCode));
        } finally {
            setPendingProblemId(null);
        }
    };

    const handleToggleCheck = async (problemId) => {
        if (pendingProblemId !== null) {
            return;
        }
        setPendingProblemId(problemId);
        setInteractionError(null);

        try {
            const currentMarkedForReview =
                answersByProblemId.get(problemId)?.checked ?? false;
            const markedForReview = !currentMarkedForReview;

            await togglePastPaperReviewMark({
                attemptId,
                problemId,
                markedForReview,
            });
            updateAnswerState(problemId, (answer) => ({
                ...answer,
                checked: markedForReview,
            }));
        } catch (error) {
            console.error("문제 검토 상태 변경 실패:", error);
            setInteractionError(getInteractionErrorMessage(error.errorCode));
        } finally {
            setPendingProblemId(null);
        }
    };

    const handleRequestSubmit = () => {
        if (pendingProblemId !== null || isSubmitting) {
            return;
        }

        const firstUnansweredIndex = answerSheetItems.findIndex((item) => !item.userAnswer);
        if (firstUnansweredIndex !== -1) {
            const unansweredCount = items.length - answeredCount;
            setCurrentIndex(firstUnansweredIndex);
            setSubmitError(
                `미응답 문항이 ${unansweredCount}개 있습니다. 모든 문항에 응답해 주세요.`
            );
            setIsSubmitConfirmOpen(false);
            return;
        }

        setSubmitError(null);
        setIsSubmitConfirmOpen(true);
    };

    const handleSubmit = async () => {
        if (isSubmitting) {
            return;
        }

        setIsSubmitting(true);
        setSubmitError(null);

        try {
            const response = await submitPastPaperAttempt(attemptId);

            navigate(
                `/past-papers/${pastPaperId}/attempts/${attemptId}/result`,
                {
                    replace: true,
                    state: {
                        pastPaperTitle,
                        result: response.data.result,
                    },
                }
            );
        } catch (error) {
            console.error("기출 문제 최종 제출 실패:", error);
            setSubmitError(getSubmitErrorMessage(error.errorCode));
            setIsSubmitConfirmOpen(false);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (isLoading) {
        return (
            <div className="attempt-skeleton-page" role="status" aria-label="문제 불러오는 중">
                <div className="attempt-skeleton-header">
                    <span className="skeleton-line title" />
                    <span className="skeleton-line timer" />
                </div>
                <div className="attempt-skeleton-workspace">
                    <section className="attempt-skeleton-problem">
                        <div className="attempt-skeleton-labels">
                            <span className="skeleton-line label" />
                            <span className="skeleton-line label wide" />
                        </div>
                        <span className="skeleton-line question" />
                        <span className="skeleton-line question short" />
                        <div className="attempt-skeleton-choices">
                            {Array.from({ length: 4 }, (_, index) => (
                                <span className="skeleton-choice" key={index} />
                            ))}
                        </div>
                    </section>
                    <aside className="attempt-skeleton-sheet">
                        <span className="skeleton-line sheet-title" />
                        <div className="attempt-skeleton-tiles">
                            {Array.from({ length: 10 }, (_, index) => (
                                <span className="skeleton-tile" key={index} />
                            ))}
                        </div>
                        <span className="skeleton-submit" />
                    </aside>
                </div>
            </div>
        );
    }

    if (loadError || items.length === 0) {
        return (
            <div className="attempt-state-card error">
                <strong>{loadError || "표시할 문항이 없습니다."}</strong>
                <p>잠시 후 다시 시도하거나 기출 문제 목록으로 돌아가 주세요.</p>
                <div className="attempt-state-actions">
                    <button type="button" onClick={loadAttempt}>다시 시도</button>
                    <button type="button" className="secondary" onClick={() => navigate("/past-papers")}>목록으로</button>
                </div>
            </div>
        );
    }

    return (
        <div className="workbook-attempt-page">
            <header className="attempt-page-header">
                <h1>{pastPaperTitle}</h1>
                {attempt?.dueAt && (
                    <span className="attempt-timer">
                        남은 시간 {formatRemainingTime(remainingSeconds)}
                    </span>
                )}
            </header>
            <div className="attempt-workspace">
                <section className="problem-deck-box" aria-label="문제 카드">
                    <div className="problem-deck">
                        {items.map((item, index) => {
                            const relativeIndex = index - currentIndex;
                            if (relativeIndex !== 0) {
                                return null;
                            }
                            const savedAnswer = answersByProblemId.get(item.problemId)?.userAnswer;
                            const isChecked = answersByProblemId.get(item.problemId)?.checked ?? false;
                            return (
                                <article
                                    key={item.problemId}
                                    className="problem-card current is-active"
                                >
                                    <header className="problem-card-header">
                                            <div>
                                                <span className="problem-number">문제 {item.sortOrder}</span>
                                                <span className="problem-part">{item.examPartName}</span>
                                            </div>
                                            <div className="problem-header-actions">
                                                <span className="problem-score">{item.score}점</span>
                                                <button
                                                    type="button"
                                                    className={`problem-check-button ${isChecked ? "active" : ""}`}
                                                    disabled={pendingProblemId !== null}
                                                    onClick={() => handleToggleCheck(item.problemId)}
                                                >
                                                    검토
                                                </button>
                                            </div>
                                    </header>
                                    <div className="problem-card-scroll">
                                        <div className="problem-content markdown-content">
                                            <ReactMarkdown
                                                remarkPlugins={[remarkGfm]}
                                                rehypePlugins={[rehypePrism]}
                                            >
                                                {item.content}
                                            </ReactMarkdown>
                                        </div>
                                        <div className="problem-choices">
                                            {item.problemChoiceResponses.map((choice) => (
                                                <button
                                                    type="button"
                                                    key={choice.sortOrder}
                                                    className={`problem-choice ${String(savedAnswer) === String(choice.sortOrder) ? "is-answered" : ""}`}
                                                    disabled={pendingProblemId !== null}
                                                    onClick={() => handleSelectAnswer(item.problemId, choice.sortOrder)}
                                                >
                                                    <span className="choice-number">{choice.sortOrder}</span>
                                                    <div className="choice-content markdown-content">
                                                        <ReactMarkdown
                                                            remarkPlugins={[remarkGfm]}
                                                            rehypePlugins={[rehypePrism]}
                                                            components={{ p: "span" }}
                                                        >
                                                            {choice.content}
                                                        </ReactMarkdown>
                                                    </div>
                                                </button>
                                            ))}
                                        </div>
                                        {interactionError && (
                                            <p className="problem-save-error" role="alert">{interactionError}</p>
                                        )}
                                    </div>
                                    <div className="problem-card-controls">
                                        <button
                                            type="button"
                                            className="problem-move-button previous"
                                            disabled={currentIndex === 0}
                                            onClick={() => moveToProblem(currentIndex - 1)}
                                        >
                                            <span aria-hidden="true">←</span>
                                            이전 문제
                                        </button>
                                        <span className="problem-page-indicator">
                                            {currentIndex + 1} / {items.length}
                                        </span>
                                        <button
                                            type="button"
                                            className="problem-move-button next"
                                            disabled={currentIndex === items.length - 1}
                                            onClick={() => moveToProblem(currentIndex + 1)}
                                        >
                                            다음 문제
                                            <span aria-hidden="true">→</span>
                                        </button>
                                    </div>
                                </article>
                            );
                        })}
                    </div>
                </section>

                <aside className="omr-card" aria-label="AnswerSheet 답안지">
                    <header className="omr-header">
                        <div className="omr-title">
                            <strong>Answer Sheet</strong>
                            <span className="omr-count">
                                {answeredCount} / {items.length} 완료
                            </span>
                        </div>
                    </header>
                    <div className="omr-list">
                        {answerSheetItems.map((item, index) => (
                            <button
                                type="button"
                                key={item.problemId}
                                className={[
                                    "omr-chip",
                                    item.userAnswer ? "answered" : "unanswered",
                                    index === currentIndex ? "active" : "",
                                    item.checked ? "checked" : "",
                                ].filter(Boolean).join(" ")}
                                onClick={() => moveToProblem(index)}
                                aria-label={`${item.sortOrder}번 문제${item.userAnswer ? `, ${item.userAnswer}번 답안 선택` : ", 미응답"}${item.checked ? ", 검토 표시" : ""}`}
                            >
                                <span className="omr-chip-number">{item.sortOrder}</span>
                            </button>
                        ))}
                    </div>
                    <div className="omr-submit-area">
                        {submitError && (
                            <p className="omr-submit-message error" role="alert">
                                {submitError}
                            </p>
                        )}
                        {isSubmitConfirmOpen ? (
                            <div className="omr-submit-confirm" role="group" aria-label="최종 제출 확인">
                                <p>제출하면 답안을 변경할 수 없습니다.</p>
                                <div>
                                    <button
                                        type="button"
                                        className="omr-submit-cancel"
                                        disabled={isSubmitting}
                                        onClick={() => setIsSubmitConfirmOpen(false)}
                                    >
                                        취소
                                    </button>
                                    <button
                                        type="button"
                                        className="omr-submit-button"
                                        disabled={isSubmitting}
                                        onClick={handleSubmit}
                                    >
                                        {isSubmitting ? "제출 중..." : "제출하기"}
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <button
                                type="button"
                                className="omr-submit-button full"
                                disabled={pendingProblemId !== null || isSubmitting}
                                onClick={handleRequestSubmit}
                            >
                                최종 제출
                            </button>
                        )}
                    </div>
                </aside>
            </div>
        </div>
    );
}

export default PastPaperAttemptPage;
