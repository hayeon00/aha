import { useState } from "react";

function ConceptProblemList({ problemData }) {
    const [selectedAnswers, setSelectedAnswers] = useState({});

    if (!problemData) {
        return null;
    }

    const handleSelect = (problemId, choiceNo) => {
        setSelectedAnswers((prev) => ({
            ...prev,
            [problemId]: choiceNo,
        }));
    };

    return (
        <div className="concept-problem-section">
            <div className="concept-problem-header">
                <p className="section-label">개념확인 문제</p>
                <h2>{problemData.examScopeNodeTitle}</h2>
                <p>총 {problemData.totalCount}문제</p>
            </div>

            <div className="problem-list">
                {problemData.problems.map((problem, index) => (
                    <div className="problem-card" key={problem.problemId}>
                        <h3>
                            문제 {index + 1}. {problem.questionText}
                        </h3>

                        <div className="choice-list">
                            {problem.choices.map((choice) => (
                                <label
                                    key={choice.choiceId}
                                    className={
                                        selectedAnswers[problem.problemId] === choice.choiceNo
                                            ? "choice-item selected"
                                            : "choice-item"
                                    }
                                >
                                    <input
                                        type="radio"
                                        name={`problem-${problem.problemId}`}
                                        value={choice.choiceNo}
                                        checked={selectedAnswers[problem.problemId] === choice.choiceNo}
                                        onChange={() =>
                                            handleSelect(problem.problemId, choice.choiceNo)
                                        }
                                    />
                                    <span>
                    {choice.choiceNo}. {choice.choiceText}
                  </span>
                                </label>
                            ))}
                        </div>
                    </div>
                ))}
            </div>

            <button className="submit-problem-button" type="button">
                답안 제출
            </button>
        </div>
    );
}

export default ConceptProblemList;