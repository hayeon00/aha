import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

function ProblemExplanation({ answer, userAnswer, correct, explanation }) {
    return (
        <section className="problem-explanation" aria-label="문제 해설">
            <div className="problem-explanation-summary">
                <span className="correct-answer-text">
                    정답: {answer}번
                </span>
                <span className="explanation-divider" aria-hidden="true" />
                <span
                    className={
                        correct
                            ? "submitted-answer-text correct"
                            : "submitted-answer-text incorrect"
                    }
                >
                    제출한 답: {userAnswer ? `${userAnswer}번` : "미응답"}
                </span>
            </div>
            <div className="problem-explanation-body markdown-content">
                <ReactMarkdown remarkPlugins={[remarkGfm]}>
                    {explanation || "등록된 해설이 없습니다."}
                </ReactMarkdown>
            </div>
        </section>
    );
}

export default ProblemExplanation;
