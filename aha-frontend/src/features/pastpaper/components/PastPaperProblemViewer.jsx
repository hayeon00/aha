import { useMemo, useState } from "react";
import ReactMarkdown from "react-markdown";
import { refractor } from "refractor/core";
import sql from "refractor/sql";
import rehypePrismGenerator from "rehype-prism-plus/generator";
import remarkGfm from "remark-gfm";
import ProblemExplanation from "./ProblemExplanation.jsx";
import "./PastPaperProblemViewer.css";

refractor.register(sql);
const rehypePrism = rehypePrismGenerator(refractor);

const FILTERS = [
    { value: "all", label: "전체 보기" },
    { value: "incorrect", label: "❌ 틀린 문제만" },
    { value: "checked", label: "📌 검토 문제만" },
];

const matchesFilter = (item, filter) => {
    if (filter === "incorrect") {
        return item.correct === false;
    }
    if (filter === "checked") {
        return item.checked;
    }
    return true;
};

function PastPaperProblemViewer({
    mode = "solving",
    title,
    items,
    answers,
}) {
    const isExplanation = mode === "explanation";
    const [filter, setFilter] = useState("all");
    const [currentProblemId, setCurrentProblemId] = useState(
        items[0]?.problemId ?? null
    );

    const answersByProblemId = useMemo(
        () => new Map(answers.map((answer) => [answer.problemId, answer])),
        [answers]
    );

    const viewerItems = useMemo(
        () =>
            items.map((item) => ({
                ...item,
                userAnswer:
                    answersByProblemId.get(item.problemId)?.userAnswer ?? null,
                correct:
                    answersByProblemId.get(item.problemId)?.correct ?? null,
                checked:
                    answersByProblemId.get(item.problemId)?.checked ?? false,
            })),
        [answersByProblemId, items]
    );

    const visibleItems = useMemo(
        () => viewerItems.filter((item) => matchesFilter(item, filter)),
        [filter, viewerItems]
    );

    const effectiveCurrentProblemId = visibleItems.some(
        (item) => item.problemId === currentProblemId
    )
        ? currentProblemId
        : visibleItems[0]?.problemId;
    const currentIndex = visibleItems.findIndex(
        (item) => item.problemId === effectiveCurrentProblemId
    );
    const currentItem =
        visibleItems[currentIndex >= 0 ? currentIndex : 0] ?? null;

    const moveToProblem = (index) => {
        const nextItem = visibleItems[index];
        if (nextItem) {
            setCurrentProblemId(nextItem.problemId);
        }
    };

    return (
        <div className={`past-paper-problem-viewer ${mode}`}>
            <header className="attempt-page-header explanation-page-header">
                <div>
                    <span>문항별 해설</span>
                    <h1>{title}</h1>
                </div>
                <span className="explanation-progress">
                    정답 {viewerItems.filter((item) => item.correct).length} /{" "}
                    {viewerItems.length}
                </span>
            </header>

            <div className="attempt-workspace explanation-workspace">
                <section className="problem-deck-box" aria-label="문항별 해설">
                    {!currentItem ? (
                        <div className="explanation-empty">
                            해당 조건에 맞는 문제가 없습니다.
                        </div>
                    ) : (
                        <article className="problem-card current is-active explanation-card">
                            <header className="problem-card-header">
                                <div className="explanation-problem-labels">
                                    <span className="problem-number">
                                        문제 {currentItem.sortOrder}
                                    </span>
                                    <span
                                        className={`result-status-badge ${
                                            currentItem.correct
                                                ? "correct"
                                                : "incorrect"
                                        }`}
                                    >
                                        {currentItem.correct ? "O 정답" : "X 오답"}
                                    </span>
                                    {currentItem.checked && (
                                        <span className="reviewed-status-badge">
                                            📌 검토함
                                        </span>
                                    )}
                                    <span className="problem-part">
                                        {currentItem.examPartName}
                                    </span>
                                    {currentItem.examScopeTitle && (
                                        <span className="problem-scope">
                                            {currentItem.examScopeTitle}
                                        </span>
                                    )}
                                </div>
                                <span className="problem-score">
                                    {currentItem.score}점
                                </span>
                            </header>

                            <div className="problem-card-scroll">
                                <div className="problem-content markdown-content">
                                    <ReactMarkdown
                                        remarkPlugins={[remarkGfm]}
                                        rehypePlugins={[rehypePrism]}
                                    >
                                        {currentItem.content}
                                    </ReactMarkdown>
                                </div>

                                <div className="problem-choices explanation-choices">
                                    {currentItem.problemChoiceResponses.map(
                                        (choice) => {
                                            const choiceNumber = String(
                                                choice.sortOrder
                                            );
                                            const isCorrectChoice =
                                                String(currentItem.answer) ===
                                                choiceNumber;
                                            const isUserChoice =
                                                String(currentItem.userAnswer) ===
                                                choiceNumber;
                                            const isUserCorrect =
                                                isCorrectChoice && isUserChoice;
                                            const isUserWrong =
                                                isUserChoice && !isCorrectChoice;

                                            return (
                                                <button
                                                    type="button"
                                                    disabled={isExplanation}
                                                    key={choice.sortOrder}
                                                    className={[
                                                        "problem-choice",
                                                        "explanation-choice",
                                                        isUserCorrect
                                                            ? "user-correct"
                                                            : "",
                                                        isUserWrong
                                                            ? "user-wrong"
                                                            : "",
                                                        isCorrectChoice &&
                                                        !isUserCorrect
                                                            ? "correct-answer"
                                                            : "",
                                                        !isUserChoice &&
                                                        !isCorrectChoice
                                                            ? "neutral"
                                                            : "",
                                                    ]
                                                        .filter(Boolean)
                                                        .join(" ")}
                                                >
                                                    <span className="choice-number">
                                                        {choice.sortOrder}
                                                    </span>
                                                    <div className="choice-content markdown-content">
                                                        <ReactMarkdown
                                                            remarkPlugins={[
                                                                remarkGfm,
                                                            ]}
                                                            rehypePlugins={[
                                                                rehypePrism,
                                                            ]}
                                                            components={{
                                                                p: "span",
                                                            }}
                                                        >
                                                            {choice.content}
                                                        </ReactMarkdown>
                                                    </div>
                                                    {(isUserCorrect ||
                                                        isUserWrong ||
                                                        isCorrectChoice) && (
                                                        <span
                                                            className={`choice-result-badge ${
                                                                isUserWrong
                                                                    ? "incorrect"
                                                                    : "correct"
                                                            }`}
                                                        >
                                                            {isUserCorrect
                                                                ? "내 선택 · 정답 ⭕"
                                                                : isUserWrong
                                                                  ? "내 선택 ❌"
                                                                  : "정답 ⭕"}
                                                        </span>
                                                    )}
                                                </button>
                                            );
                                        }
                                    )}
                                </div>

                                <ProblemExplanation
                                    answer={currentItem.answer}
                                    userAnswer={currentItem.userAnswer}
                                    correct={currentItem.correct}
                                    explanation={currentItem.explanation}
                                />
                            </div>

                            <div className="problem-card-controls">
                                <button
                                    type="button"
                                    className="problem-move-button previous"
                                    disabled={currentIndex <= 0}
                                    onClick={() => moveToProblem(currentIndex - 1)}
                                >
                                    이전 문제
                                </button>
                                <span className="problem-page-indicator">
                                    {currentIndex + 1} / {visibleItems.length}
                                </span>
                                <button
                                    type="button"
                                    className="problem-move-button next"
                                    disabled={
                                        currentIndex === visibleItems.length - 1
                                    }
                                    onClick={() => moveToProblem(currentIndex + 1)}
                                >
                                    다음 문제
                                </button>
                            </div>
                        </article>
                    )}
                </section>

                <aside className="omr-card explanation-sheet" aria-label="문항 결과 답안지">
                    <header className="omr-header explanation-sheet-header">
                        <div className="omr-title">
                            <strong>Answer Sheet</strong>
                            <span className="omr-count">
                                {visibleItems.length}문항
                            </span>
                        </div>
                        <div className="answer-sheet-filters">
                            {FILTERS.map((filterItem) => (
                                <button
                                    type="button"
                                    key={filterItem.value}
                                    className={
                                        filter === filterItem.value
                                            ? "active"
                                            : ""
                                    }
                                    onClick={() => setFilter(filterItem.value)}
                                >
                                    {filterItem.label}
                                </button>
                            ))}
                        </div>
                    </header>

                    <div className="omr-list">
                        {visibleItems.map((item) => (
                            <button
                                type="button"
                                key={item.problemId}
                                className={[
                                    "omr-chip",
                                    item.correct ? "result-correct" : "result-incorrect",
                                    item.problemId === effectiveCurrentProblemId
                                        ? "active"
                                        : "",
                                    item.checked ? "checked" : "",
                                ]
                                    .filter(Boolean)
                                    .join(" ")}
                                onClick={() =>
                                    setCurrentProblemId(item.problemId)
                                }
                                aria-label={`${item.sortOrder}번 문제, ${
                                    item.correct ? "정답" : "오답"
                                }${item.checked ? ", 검토함" : ""}`}
                            >
                                <span className="omr-chip-number">
                                    {item.sortOrder}
                                </span>
                            </button>
                        ))}
                    </div>
                </aside>
            </div>
        </div>
    );
}

export default PastPaperProblemViewer;
