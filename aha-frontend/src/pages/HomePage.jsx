import { useEffect, useState } from "react";
import axiosInstance from "../api/axiosInstance";
import SyllabusTree from "../components/exam/SyllabusTree";

function HomePage({ onLogout }) {
    const [examVersionId, setExamVersionId] = useState(1);
    const [syllabus, setSyllabus] = useState([]);
    const [selectedNode, setSelectedNode] = useState(null);
    const [learningContent, setLearningContent] = useState(null);
    const [learningSessionId, setLearningSessionId] = useState(null);
    const [conceptProblems, setConceptProblems] = useState(null);
    const [conceptProblemResult, setConceptProblemResult] = useState(null);
    const [selectedAnswers, setSelectedAnswers] = useState({});

    const [progressSummary, setProgressSummary] = useState(null);
    const [progressLoading, setProgressLoading] = useState(false);

    const [syllabusLoading, setSyllabusLoading] = useState(false);
    const [contentLoading, setContentLoading] = useState(false);
    const [problemLoading, setProblemLoading] = useState(false);
    const [submitLoading, setSubmitLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    const fetchProgressSummary = async () => {
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

            setSyllabus(response.data.data || []);
            await fetchProgressSummary();
        } catch (error) {
            console.error(error);
            setErrorMessage("목차 조회에 실패했습니다.");
        } finally {
            setSyllabusLoading(false);
        }
    };

    const handleSelectNode = async (node) => {
        console.log("선택한 node:", node);

        setSelectedNode(node);
        setLearningContent(null);
        setLearningSessionId(null);
        setConceptProblems(null);
        setConceptProblemResult(null);
        setSelectedAnswers({});
        setErrorMessage("");

        if (!node.isLeaf) {
            return;
        }

        try {
            setContentLoading(true);

            const contentResponse = await axiosInstance.get(
                `/api/v1/learning/contents/${node.id}`
            );

            const contentData = contentResponse.data.data;
            setLearningContent(contentData);

            const sessionRequestBody = {
                examScopeNodeId: node.id,
            };

            console.log("학습세션 생성 요청 body:", sessionRequestBody);

            const sessionResponse = await axiosInstance.post(
                "/api/v1/learning/sessions",
                sessionRequestBody
            );

            const sessionData = sessionResponse.data.data;

            console.log("학습세션 생성 응답:", sessionData);

            setLearningSessionId(
                sessionData.learningSessionId ?? sessionData.id
            );
        } catch (error) {
            console.error("소목차 선택 처리 실패:", error);
            console.error("응답 상태:", error.response?.status);
            console.error("응답 데이터:", error.response?.data);

            setErrorMessage(
                error.response?.data?.message ||
                "개념 설명 또는 학습 세션 생성에 실패했습니다. 해당 소목차 데이터를 확인해주세요."
            );
        } finally {
            setContentLoading(false);
        }
    };

    const handleLoadConceptProblems = async () => {
        if (!learningSessionId) {
            setErrorMessage("학습 세션 정보가 없습니다. 소목차를 다시 선택해주세요.");
            return;
        }

        try {
            setProblemLoading(true);
            setErrorMessage("");
            setConceptProblemResult(null);
            setSelectedAnswers({});

            // 1. 문제 조회
            const problemResponse = await axiosInstance.get(
                `/api/v1/learning/sessions/${learningSessionId}/concept-problems`
            );

            setConceptProblems(problemResponse.data.data);

            // 2. 기존 풀이 결과 조회
            // 기록이 없으면 400/404 등이 날 수 있으므로 내부 try-catch로 별도 처리
            try {
                const resultResponse = await axiosInstance.get(
                    `/api/v1/learning/sessions/${learningSessionId}/concept-problems/result`
                );

                const resultData = resultResponse.data.data;

                console.log("기존 개념문제 풀이 결과:", resultData);

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
                error.response?.data?.message || "개념확인 문제 조회에 실패했습니다."
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

            console.log("답안 제출 요청 body:", { answers });

            const response = await axiosInstance.post(
                `/api/v1/learning/sessions/${learningSessionId}/concept-problems/submit`,
                {
                    answers,
                }
            );

            console.log("채점 결과:", response.data.data);

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

    const getProblemResult = (problemId) => {
        if (!conceptProblemResult) {
            return null;
        }

        return conceptProblemResult.results.find(
            (result) => result.problemId === problemId
        );
    };

    useEffect(() => {
        fetchSyllabus();
    }, []);

    return (
        <main className="page">
            <section className="hero-section">
                <p className="eyebrow">Aha Learning Platform</p>
                <h1>SQLD 개념학습</h1>
                <p className="hero-description">
                    목차를 클릭하면 해당 소목차의 개념 설명을 확인할 수 있습니다.
                </p>

                <button className="logout-button" type="button" onClick={onLogout}>
                    로그아웃
                </button>
            </section>

            <section className="control-section">
                <label htmlFor="examVersionId">시험 버전 ID</label>
                <input
                    id="examVersionId"
                    type="number"
                    value={examVersionId}
                    onChange={(event) => setExamVersionId(event.target.value)}
                />
                <button type="button" onClick={fetchSyllabus}>
                    목차 조회
                </button>
            </section>

            {syllabusLoading && (
                <p className="info-text">목차를 불러오는 중입니다...</p>
            )}

            {errorMessage && <p className="error-text">{errorMessage}</p>}

            <section className="content-layout">
                <aside className="syllabus-panel">
                    <div className="panel-header">
                        <h2>시험 목차</h2>
                        <span>{syllabus.length}개 상위 목차</span>
                    </div>

                    {progressLoading && (
                        <p className="progress-loading-text">
                            학습 진도를 불러오는 중입니다...
                        </p>
                    )}

                    {progressSummary && (
                        <div className="learning-progress-box">
                            <div className="progress-header">
                                <div>
                                    <strong>전체 학습 진도</strong>
                                    <p>
                                        {progressSummary.completedTopicCount} /{" "}
                                        {progressSummary.totalTopicCount}개 소목차 완료
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
                    />
                </aside>

                <section className="detail-panel">
                    {!selectedNode && (
                        <p className="empty-text">
                            왼쪽 목차를 클릭하면 개념 설명이 표시됩니다.
                        </p>
                    )}

                    {selectedNode && (
                        <>
                            <div className="selected-node-header">
                                <p className="eyebrow">{selectedNode.nodeType}</p>
                                <h2>{selectedNode.title}</h2>
                                <span>{selectedNode.code}</span>
                            </div>

                            {!selectedNode.isLeaf && (
                                <p className="empty-text">
                                    상위 목차입니다. 하위 소목차를 선택해주세요.
                                </p>
                            )}

                            {contentLoading && (
                                <p className="info-text">
                                    개념 설명을 불러오는 중입니다...
                                </p>
                            )}

                            {learningContent && (
                                <div className="learning-content">
                                    <h3>{learningContent.title}</h3>
                                    <p className="summary">{learningContent.summary}</p>

                                    <div className="body-list">
                                        {learningContent.bodies.map((body) => (
                                            <article key={body.id} className="body-card">
                                                <span
                                                    className={`body-type ${body.bodyType.toLowerCase()}`}
                                                >
                                                    {convertBodyType(body.bodyType)}
                                                </span>
                                                <h4>{body.title}</h4>
                                                <p>{body.bodyText}</p>
                                            </article>
                                        ))}
                                    </div>

                                    <div className="concept-problem-button-area">
                                        <button
                                            type="button"
                                            className="start-concept-problem-button"
                                            onClick={handleLoadConceptProblems}
                                            disabled={!learningSessionId || problemLoading}
                                        >
                                            {problemLoading
                                                ? "문제를 불러오는 중..."
                                                : "개념확인 문제풀이"}
                                        </button>
                                    </div>
                                </div>
                            )}

                            {conceptProblems && (
                                <section className="concept-problem-section">
                                    <div className="concept-problem-header">
                                        <p className="eyebrow">CONCEPT CHECK</p>
                                        <h3>{conceptProblems.examScopeNodeTitle}</h3>
                                        <span>총 {conceptProblems.totalCount}문제</span>
                                    </div>

                                    {conceptProblemResult && (
                                        <div className="concept-result-summary">
                                            <div>
                                                <strong>
                                                    {conceptProblemResult.correctCount}
                                                </strong>
                                                <span>정답</span>
                                            </div>
                                            <div>
                                                <strong>
                                                    {conceptProblemResult.wrongCount}
                                                </strong>
                                                <span>오답</span>
                                            </div>
                                            <div>
                                                <strong>
                                                    {conceptProblemResult.correctRate}%
                                                </strong>
                                                <span>정답률</span>
                                            </div>
                                        </div>
                                    )}

                                    <div className="problem-list">
                                        {conceptProblems.problems.map((problem, index) => {
                                            const result = getProblemResult(problem.problemId);

                                            return (
                                                <article
                                                    key={problem.problemId}
                                                    className={
                                                        result
                                                            ? result.correct
                                                                ? "problem-card correct"
                                                                : "problem-card wrong"
                                                            : "problem-card"
                                                    }
                                                >
                                                    <div className="problem-title">
                                                        <span>문제 {index + 1}</span>
                                                        <h4>{problem.questionText}</h4>
                                                    </div>

                                                    <div className="choice-list">
                                                        {problem.choices.map((choice) => {
                                                            const isSelected =
                                                                selectedAnswers[
                                                                    problem.problemId
                                                                    ] === choice.choiceNo;

                                                            const isCorrectChoice =
                                                                result?.correctChoiceNo ===
                                                                choice.choiceNo;

                                                            return (
                                                                <label
                                                                    key={choice.choiceId}
                                                                    className={[
                                                                        "choice-item",
                                                                        isSelected ? "selected" : "",
                                                                        result && isCorrectChoice
                                                                            ? "correct-choice"
                                                                            : "",
                                                                        result &&
                                                                        isSelected &&
                                                                        !result.correct
                                                                            ? "wrong-choice"
                                                                            : "",
                                                                    ]
                                                                        .filter(Boolean)
                                                                        .join(" ")}
                                                                >
                                                                    <input
                                                                        type="radio"
                                                                        name={`problem-${problem.problemId}`}
                                                                        value={choice.choiceNo}
                                                                        checked={isSelected}
                                                                        disabled={!!conceptProblemResult}
                                                                        onChange={() =>
                                                                            handleSelectAnswer(
                                                                                problem.problemId,
                                                                                choice.choiceNo
                                                                            )
                                                                        }
                                                                    />
                                                                    <span>
                                                                        {choice.choiceNo}.{" "}
                                                                        {choice.choiceText}
                                                                    </span>
                                                                </label>
                                                            );
                                                        })}
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
                                                                {result.selectedChoiceNo}번 / 정답:{" "}
                                                                {result.correctChoiceNo}번
                                                            </p>
                                                            <div className="explanation-box">
                                                                <strong>해설</strong>
                                                                <p>{result.explanationText}</p>
                                                            </div>
                                                        </div>
                                                    )}
                                                </article>
                                            );
                                        })}
                                    </div>

                                    {!conceptProblemResult && (
                                        <button
                                            type="button"
                                            className="submit-problem-button"
                                            onClick={handleSubmitConceptProblems}
                                            disabled={submitLoading}
                                        >
                                            {submitLoading ? "채점 중..." : "답안 제출"}
                                        </button>
                                    )}
                                </section>
                            )}
                        </>
                    )}
                </section>
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

export default HomePage;