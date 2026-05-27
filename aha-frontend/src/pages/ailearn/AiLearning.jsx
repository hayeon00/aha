import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance.js";
import SyllabusTree from "../../components/exam/SyllabusTree.jsx";
import "./AiLearning.css";

function AiLearning() {
    const navigate = useNavigate();
    const isLoggedIn = !!localStorage.getItem("accessToken");

    const [examVersionId] = useState(1);
    const [syllabus, setSyllabus] = useState([]);
    const [selectedNode, setSelectedNode] = useState(null);
    const [learningContent, setLearningContent] = useState(null);
    const [learningSessionId, setLearningSessionId] = useState(null);
    const [conceptProblems, setConceptProblems] = useState(null);
    const [conceptProblemResult, setConceptProblemResult] = useState(null);
    const [selectedAnswers, setSelectedAnswers] = useState({});

    const [assistantMessages, setAssistantMessages] = useState([
        {
            role: "ASSISTANT",
            text: "안녕하세요! 현재 선택한 개념에 대해 쉽게 설명하거나, 시험 포인트를 정리해드릴게요.",
        },
    ]);

    const [assistantQuestionType, setAssistantQuestionType] =
        useState("EASY_EXPLANATION");
    const [assistantInput, setAssistantInput] = useState("");
    const [assistantLoading, setAssistantLoading] = useState(false);

    const [assistantWidth, setAssistantWidth] = useState(360);
    const [isResizingAssistant, setIsResizingAssistant] = useState(false);
    const contentLayoutRef = useRef(null);

    const [progressSummary, setProgressSummary] = useState(null);
    const [progressLoading, setProgressLoading] = useState(false);

    const [syllabusLoading, setSyllabusLoading] = useState(false);
    const [contentLoading, setContentLoading] = useState(false);
    const [problemLoading, setProblemLoading] = useState(false);
    const [submitLoading, setSubmitLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    const assistantQuestionTypes = [
        { value: "EASY_EXPLANATION", label: "쉽게 설명" },
        { value: "COMPARISON", label: "비교 설명" },
        { value: "EXAM_POINT", label: "시험 포인트" },
        { value: "PROBLEM_HELP", label: "문제 풀이 도움" },
        { value: "SUMMARY", label: "요약" },
        { value: "FREE_QNA", label: "자유 질문" },
    ];

    const findFirstLeafNode = (nodes = []) => {
        for (const node of nodes) {
            if (node.isLeaf) {
                return node;
            }

            const children = node.children || node.childNodes || [];
            const leafNode = findFirstLeafNode(children);

            if (leafNode) {
                return leafNode;
            }
        }

        return null;
    };

    const fetchProgressSummary = async () => {
        if (!localStorage.getItem("accessToken")) {
            setProgressSummary(null);
            return;
        }

        try {
            setProgressLoading(true);

            const response = await axiosInstance.get(
                `/api/v1/learning/progress/summary?examVersionId=${examVersionId}`
            );

            setProgressSummary(response.data.data);
        } catch (error) {
            console.error("학습 진도 조회 실패:", error);
        } finally {
            setProgressLoading(false);
        }
    };

    const handleSelectNode = async (node) => {
        setSelectedNode(node);
        setLearningContent(null);
        setLearningSessionId(null);
        setConceptProblems(null);
        setConceptProblemResult(null);
        setSelectedAnswers({});
        setErrorMessage("");

        setAssistantMessages([
            {
                role: "ASSISTANT",
                text: "안녕하세요! 현재 선택한 개념에 대해 쉽게 설명하거나, 시험 포인트를 정리해드릴게요.",
            },
        ]);
        setAssistantInput("");
        setAssistantQuestionType("EASY_EXPLANATION");

        if (!node.isLeaf) {
            return;
        }

        if (!localStorage.getItem("accessToken")) {
            return;
        }

        try {
            setContentLoading(true);

            const contentResponse = await axiosInstance.get(
                `/api/v1/learning/contents/${node.id}`
            );

            const contentData = contentResponse.data.data;
            setLearningContent(contentData);

            try {
                const sessionResponse = await axiosInstance.post(
                    "/api/v1/learning/sessions",
                    {
                        examScopeNodeId: node.id,
                    }
                );

                const sessionData = sessionResponse.data.data;

                setLearningSessionId(
                    sessionData.learningSessionId ?? sessionData.id
                );
            } catch (sessionError) {
                console.error("학습 세션 생성 실패:", sessionError);
                console.error("응답 상태:", sessionError.response?.status);
                console.error("응답 데이터:", sessionError.response?.data);

                setLearningSessionId(null);
                setErrorMessage(
                    sessionError.response?.data?.message ||
                    "개념 설명은 조회되었지만 학습 세션 생성에 실패했습니다."
                );
            }
        } catch (error) {
            console.error("개념 설명 조회 실패:", error);
            console.error("응답 상태:", error.response?.status);
            console.error("응답 데이터:", error.response?.data);

            setErrorMessage(
                error.response?.data?.message ||
                "개념 설명 조회에 실패했습니다. 해당 소목차 데이터를 확인해주세요."
            );
        } finally {
            setContentLoading(false);
        }
    };

    const fetchSyllabus = async () => {
        try {
            setSyllabusLoading(true);
            setErrorMessage("");
            setSelectedNode(null);
            setLearningContent(null);
            setLearningSessionId(null);
            setConceptProblems(null);
            setConceptProblemResult(null);
            setSelectedAnswers({});

            const response = await axiosInstance.get(
                `/api/v1/exam-versions/${examVersionId}/syllabus`
            );

            const syllabusData = response.data.data || [];
            setSyllabus(syllabusData);

            if (localStorage.getItem("accessToken")) {
                await fetchProgressSummary();
            } else {
                setProgressSummary(null);
            }

            const firstLeafNode = findFirstLeafNode(syllabusData);

            if (firstLeafNode) {
                await handleSelectNode(firstLeafNode);
            }
        } catch (error) {
            console.error(error);
            setErrorMessage("목차 조회에 실패했습니다.");
        } finally {
            setSyllabusLoading(false);
        }
    };

    const handleLoadConceptProblems = async () => {
        if (!localStorage.getItem("accessToken")) {
            alert("문제풀이를 하려면 로그인이 필요합니다.");
            navigate("/login");
            return;
        }

        if (!learningSessionId) {
            setErrorMessage("학습 세션 정보가 없습니다. 소목차를 다시 선택해주세요.");
            return;
        }

        try {
            setProblemLoading(true);
            setErrorMessage("");
            setConceptProblemResult(null);
            setSelectedAnswers({});

            const problemResponse = await axiosInstance.get(
                `/api/v1/learning/sessions/${learningSessionId}/concept-problems`
            );

            setConceptProblems(problemResponse.data.data);

            try {
                const resultResponse = await axiosInstance.get(
                    `/api/v1/learning/sessions/${learningSessionId}/concept-problems/result`
                );

                const resultData = resultResponse.data.data;
                setConceptProblemResult(resultData);

                const restoredAnswers = {};

                resultData.results.forEach((result) => {
                    restoredAnswers[result.problemId] = result.selectedChoiceNo;
                });

                setSelectedAnswers(restoredAnswers);
            } catch (resultError) {
                console.log("기존 풀이 기록이 없습니다.");
            }
        } catch (error) {
            console.error(error);
            setErrorMessage(
                error.response?.data?.message ||
                "개념확인 문제 조회에 실패했습니다."
            );
        } finally {
            setProblemLoading(false);
        }
    };

    const handleSelectAnswer = (problemId, choiceNo) => {
        if (conceptProblemResult) {
            return;
        }

        setSelectedAnswers((prev) => ({
            ...prev,
            [problemId]: choiceNo,
        }));
    };

    const handleSubmitConceptProblems = async () => {
        if (!localStorage.getItem("accessToken")) {
            alert("답안을 제출하려면 로그인이 필요합니다.");
            navigate("/login");
            return;
        }

        if (!learningSessionId) {
            setErrorMessage("학습 세션 정보가 없습니다.");
            return;
        }

        if (!conceptProblems || conceptProblems.problems.length === 0) {
            setErrorMessage("제출할 문제가 없습니다.");
            return;
        }

        const totalCount = conceptProblems.problems.length;
        const answeredCount = Object.keys(selectedAnswers).length;

        if (answeredCount < totalCount) {
            alert("모든 문제의 답을 선택해주세요.");
            return;
        }

        const answers = Object.entries(selectedAnswers).map(
            ([problemId, selectedChoiceNo]) => ({
                problemId: Number(problemId),
                selectedChoiceNo: Number(selectedChoiceNo),
            })
        );

        try {
            setSubmitLoading(true);
            setErrorMessage("");

            const response = await axiosInstance.post(
                `/api/v1/learning/sessions/${learningSessionId}/concept-problems/submit`,
                {
                    answers,
                }
            );

            setConceptProblemResult(response.data.data);
            await fetchProgressSummary();
        } catch (error) {
            console.error("답안 제출 실패:", error);
            console.error("응답 상태:", error.response?.status);
            console.error("응답 데이터:", error.response?.data);

            setErrorMessage(
                error.response?.data?.message || "답안 제출에 실패했습니다."
            );
        } finally {
            setSubmitLoading(false);
        }
    };

    const handleSendAssistantMessage = async () => {
        if (!localStorage.getItem("accessToken")) {
            alert("AI 학습 도우미를 사용하려면 로그인이 필요합니다.");
            navigate("/login");
            return;
        }

        if (!selectedNode || !selectedNode.isLeaf) {
            alert("먼저 학습할 소목차를 선택해주세요.");
            return;
        }

        if (!learningSessionId) {
            alert("학습 세션 정보가 없습니다. 소목차를 다시 선택해주세요.");
            return;
        }

        if (!assistantInput.trim()) {
            alert("질문 내용을 입력해주세요.");
            return;
        }

        const userText = assistantInput;

        setAssistantMessages((prev) => [
            ...prev,
            {
                role: "USER",
                text: userText,
            },
        ]);

        setAssistantInput("");
        setAssistantLoading(true);

        try {
            const response = await axiosInstance.post(
                `/api/v1/learning/sessions/${learningSessionId}/ai-messages`,
                {
                    questionType: assistantQuestionType,
                    message: userText,
                }
            );

            const data = response.data.data;

            setAssistantMessages((prev) => [
                ...prev,
                {
                    role: "ASSISTANT",
                    text: data.assistantMessage,
                },
            ]);
        } catch (error) {
            console.error("AI 도우미 요청 실패:", error);
            console.error("응답 상태:", error.response?.status);
            console.error("응답 데이터:", error.response?.data);

            setAssistantMessages((prev) => [
                ...prev,
                {
                    role: "ASSISTANT",
                    text:
                        error.response?.data?.message ||
                        "AI 도우미 응답 생성에 실패했습니다.",
                },
            ]);
        } finally {
            setAssistantLoading(false);
        }
    };

    const handleAssistantResizeStart = () => {
        setIsResizingAssistant(true);
    };

    useEffect(() => {
        if (!isResizingAssistant) {
            return;
        }

        const handleMouseMove = (event) => {
            if (!contentLayoutRef.current) {
                return;
            }

            const layoutRect = contentLayoutRef.current.getBoundingClientRect();
            const newAssistantWidth = layoutRect.right - event.clientX;

            const minWidth = 280;
            const maxWidth = 620;

            const limitedWidth = Math.min(
                Math.max(newAssistantWidth, minWidth),
                maxWidth
            );

            setAssistantWidth(limitedWidth);
        };

        const handleMouseUp = () => {
            setIsResizingAssistant(false);
        };

        window.addEventListener("mousemove", handleMouseMove);
        window.addEventListener("mouseup", handleMouseUp);

        document.body.style.cursor = "col-resize";
        document.body.style.userSelect = "none";

        return () => {
            window.removeEventListener("mousemove", handleMouseMove);
            window.removeEventListener("mouseup", handleMouseUp);

            document.body.style.cursor = "";
            document.body.style.userSelect = "";
        };
    }, [isResizingAssistant]);

    const getProblemResult = (problemId) => {
        if (!conceptProblemResult) {
            return null;
        }

        return conceptProblemResult.results.find(
            (result) => result.problemId === problemId
        );
    };

    const getSelectedNodeProgressStatus = () => {
        if (!selectedNode || !progressSummary?.topics) {
            return null;
        }

        const topicProgress = progressSummary.topics.find(
            (topic) => topic.examScopeNodeId === selectedNode.id
        );

        return topicProgress?.status || "NOT_STARTED";
    };

    const handleLoginClick = () => {
        navigate("/login");
    };

    useEffect(() => {
        fetchSyllabus();
    }, []);

    return (
        <main className="ai-learning-page">
            {syllabusLoading && (
                <p className="learning-info-text">
                    목차를 불러오는 중입니다...
                </p>
            )}

            {errorMessage && (
                <p className="learning-error-text">{errorMessage}</p>
            )}

            <section
                ref={contentLayoutRef}
                className="learning-content-layout"
                style={{
                    gridTemplateColumns: `300px minmax(420px, 1fr) 8px ${assistantWidth}px`,
                }}
            >
                <aside className="learning-syllabus-panel">
                    <div className="learning-panel-header">
                        <div>
                            <p className="learning-panel-kicker">SYLLABUS</p>
                            <h2>시험 목차</h2>
                        </div>
                        <span>{syllabus.length}개</span>
                    </div>

                    {isLoggedIn && progressLoading && (
                        <p className="learning-progress-loading-text">
                            학습 진도를 불러오는 중입니다...
                        </p>
                    )}

                    {isLoggedIn && progressSummary && (
                        <div className="learning-progress-box">
                            <div className="progress-header">
                                <div>
                                    <strong>전체 학습 진도</strong>
                                    <p>
                                        {progressSummary.completedTopicCount} /{" "}
                                        {progressSummary.totalTopicCount}개
                                        소목차 완료
                                    </p>
                                </div>

                                <span>{progressSummary.progressRate}%</span>
                            </div>

                            <div className="progress-bar">
                                <div
                                    className="progress-fill"
                                    style={{
                                        width: `${progressSummary.progressRate}%`,
                                    }}
                                />
                            </div>
                        </div>
                    )}

                    <SyllabusTree
                        nodes={syllabus}
                        selectedNodeId={selectedNode?.id}
                        onSelectNode={handleSelectNode}
                        topicProgresses={
                            isLoggedIn ? progressSummary?.topics || [] : []
                        }
                    />
                </aside>

                <section className="learning-detail-panel">
                    {!selectedNode && (
                        <div className="learning-empty-state">
                            <span>📘</span>
                            <h3>학습할 소목차를 불러오는 중입니다</h3>
                            <p>
                                첫 번째 소목차가 자동으로 선택되면 개념 설명이
                                표시됩니다.
                            </p>
                        </div>
                    )}

                    {selectedNode && (
                        <>
                            {!selectedNode.isLeaf && (
                                <div className="learning-empty-state small">
                                    <h3>상위 목차입니다</h3>
                                    <p>하위 소목차를 선택해주세요.</p>
                                </div>
                            )}

                            {selectedNode.isLeaf && !isLoggedIn && (
                                <div className="login-required-box">
                                    <h3>로그인 후 학습할 수 있습니다</h3>
                                    <p>
                                        목차는 누구나 볼 수 있습니다. 로그인하면
                                        전체 개념 설명, 개념확인 문제풀이, 학습
                                        진도 저장, AI 학습 도우미를 사용할 수
                                        있습니다.
                                    </p>
                                    <button
                                        type="button"
                                        onClick={handleLoginClick}
                                    >
                                        로그인하고 학습 시작하기
                                    </button>
                                </div>
                            )}

                            {contentLoading && (
                                <p className="learning-info-text">
                                    개념 설명을 불러오는 중입니다...
                                </p>
                            )}

                            {learningContent && (
                                <div className="learning-content">
                                    <div className="learning-content-header">
                                        <p className="learning-eyebrow">
                                            CONCEPT CONTENT
                                        </p>
                                        <h3>{learningContent.title}</h3>
                                        <p className="summary">
                                            {learningContent.summary}
                                        </p>
                                    </div>

                                    <div className="body-list">
                                        {learningContent.bodies.map((body) => (
                                            <article
                                                key={body.id}
                                                className="body-card"
                                            >
                                                <span
                                                    className={`body-type ${body.bodyType.toLowerCase()}`}
                                                >
                                                    {convertBodyType(
                                                        body.bodyType
                                                    )}
                                                </span>
                                                <h4>{body.title}</h4>
                                                <p>{body.bodyText}</p>
                                            </article>
                                        ))}
                                    </div>

                                    {getSelectedNodeProgressStatus() ===
                                        "COMPLETED" && (
                                            <p className="completed-topic-notice">
                                                이미 개념확인 문제를 완료한
                                                소목차입니다. 풀이 결과를 다시 확인할
                                                수 있습니다.
                                            </p>
                                        )}

                                    <div className="concept-problem-button-area">
                                        <button
                                            type="button"
                                            className="start-concept-problem-button"
                                            onClick={handleLoadConceptProblems}
                                            disabled={
                                                !isLoggedIn ||
                                                !learningSessionId ||
                                                problemLoading
                                            }
                                        >
                                            {problemLoading
                                                ? "문제를 불러오는 중..."
                                                : getSelectedNodeProgressStatus() ===
                                                "COMPLETED"
                                                    ? "풀이 결과 보기"
                                                    : "개념확인 문제풀이"}
                                        </button>
                                    </div>
                                </div>
                            )}

                            {conceptProblems && (
                                <section className="concept-problem-section">
                                    <div className="concept-problem-header">
                                        <p className="learning-eyebrow">
                                            CONCEPT CHECK
                                        </p>
                                        <h3>
                                            {
                                                conceptProblems.examScopeNodeTitle
                                            }
                                        </h3>
                                        <span>
                                            총 {conceptProblems.totalCount}문제
                                        </span>
                                    </div>

                                    {conceptProblemResult && (
                                        <div className="concept-result-summary">
                                            <div>
                                                <strong>
                                                    {
                                                        conceptProblemResult.correctCount
                                                    }
                                                </strong>
                                                <span>정답</span>
                                            </div>
                                            <div>
                                                <strong>
                                                    {
                                                        conceptProblemResult.wrongCount
                                                    }
                                                </strong>
                                                <span>오답</span>
                                            </div>
                                            <div>
                                                <strong>
                                                    {
                                                        conceptProblemResult.correctRate
                                                    }
                                                    %
                                                </strong>
                                                <span>정답률</span>
                                            </div>
                                        </div>
                                    )}

                                    <div className="concept-problem-list">
                                        {conceptProblems.problems.map(
                                            (problem, index) => {
                                                const result = getProblemResult(
                                                    problem.problemId
                                                );

                                                return (
                                                    <article
                                                        key={
                                                            problem.problemId
                                                        }
                                                        className={
                                                            result
                                                                ? result.correct
                                                                    ? "concept-problem-card correct"
                                                                    : "concept-problem-card wrong"
                                                                : "concept-problem-card"
                                                        }
                                                    >
                                                        <div className="problem-title">
                                                            <span>
                                                                문제 {index + 1}
                                                            </span>
                                                            <h4>
                                                                {
                                                                    problem.questionText
                                                                }
                                                            </h4>
                                                        </div>

                                                        <div className="choice-list">
                                                            {problem.choices.map(
                                                                (choice) => {
                                                                    const isSelected =
                                                                        selectedAnswers[
                                                                            problem
                                                                                .problemId
                                                                            ] ===
                                                                        choice.choiceNo;

                                                                    const isCorrectChoice =
                                                                        result?.correctChoiceNo ===
                                                                        choice.choiceNo;

                                                                    return (
                                                                        <label
                                                                            key={
                                                                                choice.choiceId
                                                                            }
                                                                            className={[
                                                                                "choice-item",
                                                                                isSelected
                                                                                    ? "selected"
                                                                                    : "",
                                                                                result &&
                                                                                isCorrectChoice
                                                                                    ? "correct-choice"
                                                                                    : "",
                                                                                result &&
                                                                                isSelected &&
                                                                                !result.correct
                                                                                    ? "wrong-choice"
                                                                                    : "",
                                                                            ]
                                                                                .filter(
                                                                                    Boolean
                                                                                )
                                                                                .join(
                                                                                    " "
                                                                                )}
                                                                        >
                                                                            <input
                                                                                type="radio"
                                                                                name={`problem-${problem.problemId}`}
                                                                                value={
                                                                                    choice.choiceNo
                                                                                }
                                                                                checked={
                                                                                    isSelected
                                                                                }
                                                                                disabled={
                                                                                    !!conceptProblemResult
                                                                                }
                                                                                onChange={() =>
                                                                                    handleSelectAnswer(
                                                                                        problem.problemId,
                                                                                        choice.choiceNo
                                                                                    )
                                                                                }
                                                                            />
                                                                            <span>
                                                                                {
                                                                                    choice.choiceNo
                                                                                }
                                                                                .{" "}
                                                                                {
                                                                                    choice.choiceText
                                                                                }
                                                                            </span>
                                                                        </label>
                                                                    );
                                                                }
                                                            )}
                                                        </div>

                                                        {result && (
                                                            <div
                                                                className={
                                                                    result.correct
                                                                        ? "problem-result-box correct"
                                                                        : "problem-result-box wrong"
                                                                }
                                                            >
                                                                <div className="result-label">
                                                                    {result.correct
                                                                        ? "정답입니다"
                                                                        : "오답입니다"}
                                                                </div>
                                                                <p>
                                                                    선택한 답:{" "}
                                                                    {
                                                                        result.selectedChoiceNo
                                                                    }
                                                                    번 / 정답:{" "}
                                                                    {
                                                                        result.correctChoiceNo
                                                                    }
                                                                    번
                                                                </p>
                                                                <div className="explanation-box">
                                                                    <strong>
                                                                        해설
                                                                    </strong>
                                                                    <p>
                                                                        {
                                                                            result.explanationText
                                                                        }
                                                                    </p>
                                                                </div>
                                                            </div>
                                                        )}
                                                    </article>
                                                );
                                            }
                                        )}
                                    </div>

                                    {!conceptProblemResult && (
                                        <button
                                            type="button"
                                            className="submit-problem-button"
                                            onClick={
                                                handleSubmitConceptProblems
                                            }
                                            disabled={submitLoading}
                                        >
                                            {submitLoading
                                                ? "채점 중..."
                                                : "답안 제출"}
                                        </button>
                                    )}
                                </section>
                            )}
                        </>
                    )}
                </section>

                <div
                    className={`assistant-resizer ${
                        isResizingAssistant ? "active" : ""
                    }`}
                    onMouseDown={handleAssistantResizeStart}
                    role="separator"
                    aria-orientation="vertical"
                />

                <aside className="learning-assistant-panel dark-assistant">
                    <div className="assistant-header">
                        <div>
                            <p className="assistant-kicker">AI HELPER</p>
                            <h2>AI 학습 도우미</h2>
                        </div>
                        <span className="assistant-status">
                            {!isLoggedIn
                                ? "로그인 필요"
                                : selectedNode?.isLeaf
                                    ? "사용 가능"
                                    : "소목차 선택 필요"}
                        </span>
                    </div>

                    <div className="assistant-type-list">
                        {assistantQuestionTypes.map((type) => (
                            <button
                                key={type.value}
                                type="button"
                                className={
                                    assistantQuestionType === type.value
                                        ? "assistant-type-button active"
                                        : "assistant-type-button"
                                }
                                onClick={() =>
                                    setAssistantQuestionType(type.value)
                                }
                            >
                                {type.label}
                            </button>
                        ))}
                    </div>

                    <div className="assistant-message-list">
                        {assistantMessages.map((message, index) => (
                            <div
                                key={index}
                                className={
                                    message.role === "USER"
                                        ? "assistant-message user"
                                        : "assistant-message assistant"
                                }
                            >
                                <span>
                                    {message.role === "USER" ? "나" : "AI"}
                                </span>
                                <p>{message.text}</p>
                            </div>
                        ))}
                    </div>

                    <div className="assistant-input-area">
                        <textarea
                            value={assistantInput}
                            onChange={(event) =>
                                setAssistantInput(event.target.value)
                            }
                            placeholder={
                                !isLoggedIn
                                    ? "로그인 후 AI 학습 도우미를 사용할 수 있습니다."
                                    : selectedNode?.isLeaf
                                        ? "현재 개념에 대해 궁금한 점을 입력하세요."
                                        : "소목차를 먼저 선택해주세요."
                            }
                            disabled={
                                !isLoggedIn ||
                                !selectedNode?.isLeaf ||
                                !learningSessionId ||
                                assistantLoading
                            }
                        />

                        <button
                            type="button"
                            onClick={handleSendAssistantMessage}
                            disabled={
                                !isLoggedIn ||
                                !selectedNode?.isLeaf ||
                                !learningSessionId ||
                                assistantLoading
                            }
                        >
                            {assistantLoading ? "답변 생성 중..." : "질문하기"}
                        </button>
                    </div>
                </aside>
            </section>
        </main>
    );
}

function convertBodyType(bodyType) {
    const labels = {
        BASE_EXPLANATION: "기본 설명",
        CORE_POINT: "핵심 포인트",
        EXAMPLE: "예시",
        CONFUSION_NOTE: "헷갈리는 개념",
        EXAM_POINT: "출제 포인트",
    };

    return labels[bodyType] || bodyType;
}

export default AiLearning;