import { useCallback, useEffect, useMemo, useState } from "react";
import ReactMarkdown from "react-markdown";
import { refractor } from "refractor/core";
import sql from "refractor/sql";
import rehypePrismGenerator from "rehype-prism-plus/generator";
import remarkGfm from "remark-gfm";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import {
    getWorkbookAttemptAnswers,
    getWorkbookItems,
    saveWorkbookAnswer,
    submitWorkbookAttempt,
    toggleWorkbookProblemCheck,
} from "../api/workbookApi.js";
import "./WorkbookAttemptPage.css";

refractor.register(sql);
const rehypePrism = rehypePrismGenerator(refractor);

const getLoadErrorMessage = (errorCode) => {
    switch (errorCode) {
        case "WORKBOOK_004":
            return "이 풀이 기록을 찾을 수 없거나 접근할 수 없습니다.";
        case "WORKBOOK_002":
            return "현재 공개되지 않은 문제집입니다.";
        case "EXAM_VERSION_001":
            return "시험 버전이 변경되어 풀이를 계속할 수 없습니다.";
        case "EXAM_001":
            return "현재 이용할 수 없는 시험입니다.";
        case "WORKBOOK_005":
            return "문항 구성을 확인하는 중 문제가 발생했습니다.";
        default:
            return "풀이 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.";
    }
};

const getInteractionErrorMessage = (errorCode) => {
    switch (errorCode) {
        case "ATTEMPT_EXCEEDED_TIME_LIMIT":
        case "WORKBOOK_008":
            return "제한 시간이 지나 답안을 변경할 수 없습니다.";
        case "ATTEMPT_ALREADY_GRADED":
        case "WORKBOOK_009":
            return "이미 채점이 완료된 풀이입니다.";
        case "WORKBOOK_006":
        case "PROBLEM_001":
            return "현재 문제 정보를 확인할 수 없습니다.";
        case "WORKBOOK_004":
            return "풀이 기록을 확인할 수 없습니다.";
        default:
            return "변경사항을 저장하지 못했습니다. 다시 시도해 주세요.";
    }
};

const getSubmitErrorMessage = (errorCode) => {
    switch (errorCode) {
        case "WORKBOOK_011":
            return "미응답 문항이 있습니다. 모든 문항에 응답해 주세요.";
        case "WORKBOOK_008":
            return "제한 시간이 지나 제출할 수 없습니다.";
        case "WORKBOOK_009":
            return "이미 채점이 완료된 풀이입니다.";
        case "WORKBOOK_004":
            return "풀이 기록을 확인할 수 없습니다.";
        default:
            return "제출하지 못했습니다. 잠시 후 다시 시도해 주세요.";
    }
};

function WorkbookAttemptPage() {
    const { workbookId, attemptId } = useParams();
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

    const loadAttempt = useCallback(async () => {
        setIsLoading(true);
        setLoadError(null);

        try {
            const [itemsResponse, answersResponse] = await Promise.all([
                getWorkbookItems(workbookId),
                getWorkbookAttemptAnswers(attemptId),
            ]);
            const sortedItems = [...itemsResponse.data]
                .sort((first, second) => first.sortOrder - second.sortOrder)
                .map((item) => ({
                    ...item,
                    choices: [...(item.choices || [])].sort(
                        (first, second) => first.sortOrder - second.sortOrder
                    ),
                }));

            setItems(sortedItems);
            setAnswers(answersResponse.data);
            setCurrentIndex((index) =>
                Math.min(index, Math.max(sortedItems.length - 1, 0))
            );
        } catch (error) {
            console.error("문제 풀이 정보 조회 실패:", error);
            setLoadError(getLoadErrorMessage(error.errorCode));
        } finally {
            setIsLoading(false);
        }
    }, [attemptId, workbookId]);

    useEffect(() => {
        queueMicrotask(() => {
            loadAttempt();
        });
    }, [loadAttempt]);

    const answersByProblemId = useMemo(
        () => new Map(answers.map((answer) => [answer.problemId, answer])),
        [answers]
    );

    const omrItems = useMemo(
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

    const answeredCount = omrItems.filter((item) => item.userAnswer).length;
    const workbookTitle =
        location.state?.workbookTitle ||
        sessionStorage.getItem(`workbook-title-${workbookId}`) ||
        `문제집 #${workbookId}`;

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
            await saveWorkbookAnswer({ attemptId, problemId, userAnswer });
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
            await toggleWorkbookProblemCheck({ attemptId, problemId });
            updateAnswerState(problemId, (answer) => ({
                ...answer,
                checked: !answer.checked,
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

        const firstUnansweredIndex = omrItems.findIndex((item) => !item.userAnswer);
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
            const response = await submitWorkbookAttempt(attemptId);
            const submittedAttemptId = response.data?.id ?? attemptId;

            navigate(
                `/problems/${workbookId}/attempts/${submittedAttemptId}/result`,
                {
                    replace: true,
                    state: { workbookTitle },
                }
            );
        } catch (error) {
            console.error("문제집 최종 제출 실패:", error);
            setSubmitError(getSubmitErrorMessage(error.errorCode));
            setIsSubmitConfirmOpen(false);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (isLoading) {
        return (
            <div className="attempt-state-card" role="status">
                <span className="attempt-loader" />
                <strong>문제와 답안을 준비하고 있습니다.</strong>
            </div>
        );
    }

    if (loadError || items.length === 0) {
        return (
            <div className="attempt-state-card error">
                <strong>{loadError || "표시할 문항이 없습니다."}</strong>
                <p>잠시 후 다시 시도하거나 문제집 목록으로 돌아가 주세요.</p>
                <div className="attempt-state-actions">
                    <button type="button" onClick={loadAttempt}>다시 시도</button>
                    <button type="button" className="secondary" onClick={() => navigate("/problems")}>목록으로</button>
                </div>
            </div>
        );
    }

    return (
        <div className="workbook-attempt-page">
            <header className="attempt-page-header">
                <h1>{workbookTitle}</h1>
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
                            const isPending = pendingProblemId === item.problemId;

                            return (
                                <article
                                    key={item.problemId}
                                    className="problem-card current is-active"
                                >
                                    <div className="problem-card-scroll">
                                        <header className="problem-card-header">
                                            <div>
                                                <span className="problem-number">문제 {item.sortOrder}</span>
                                                <span className="problem-part">{item.examPartCodeName}</span>
                                            </div>
                                            <div className="problem-header-actions">
                                                <span className="problem-score">{item.score}점</span>
                                                <button
                                                    type="button"
                                                    className={`problem-check-button ${isChecked ? "active" : ""}`}
                                                    disabled={pendingProblemId !== null}
                                                    onClick={() => handleToggleCheck(item.problemId)}
                                                >
                                                    {isChecked ? "검토 해제" : "검토 표시"}
                                                </button>
                                            </div>
                                        </header>
                                        <div className="problem-content markdown-content">
                                            <ReactMarkdown
                                                remarkPlugins={[remarkGfm]}
                                                rehypePlugins={[rehypePrism]}
                                            >
                                                {item.content}
                                            </ReactMarkdown>
                                        </div>
                                        <div className="problem-choices">
                                            {item.choices.map((choice) => (
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
                                        {isPending && (
                                            <p className="problem-save-status" role="status">저장 중...</p>
                                        )}
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

                <aside className="omr-card" aria-label="OMR 답안 현황">
                    <header className="omr-header">
                        <div className="omr-title">
                            <span>OMR</span>
                            <strong>답안 현황</strong>
                        </div>
                        <div className="omr-status-group">
                            <span className="omr-current-count">
                                현재 {currentIndex + 1}/{items.length}
                            </span>
                            <span className="omr-count">
                                응답 {answeredCount}/{items.length}
                            </span>
                        </div>
                    </header>
                    <div className="omr-list">
                        {omrItems.map((item, index) => (
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
                                {item.checked && (
                                    <svg
                                        className="omr-check-star"
                                        width="11"
                                        height="11"
                                        viewBox="0 0 24 24"
                                        fill="none"
                                        aria-hidden="true"
                                    >
                                        <path
                                            d="M12 2.75L14.82 8.46L21.12 9.38L16.56 13.82L17.64 20.1L12 17.14L6.36 20.1L7.44 13.82L2.88 9.38L9.18 8.46L12 2.75Z"
                                        />
                                    </svg>
                                )}
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

export default WorkbookAttemptPage;
