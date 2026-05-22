import { useEffect, useState } from "react";
import axiosInstance from "../api/axiosInstance";
import SyllabusTree from "../components/exam/SyllabusTree";

function HomePage({ onLogout }) {
    const [examVersionId, setExamVersionId] = useState(1);
    const [syllabus, setSyllabus] = useState([]);
    const [selectedNode, setSelectedNode] = useState(null);
    const [learningContent, setLearningContent] = useState(null);

    const [syllabusLoading, setSyllabusLoading] = useState(false);
    const [contentLoading, setContentLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    const fetchSyllabus = async () => {
        try {
            setSyllabusLoading(true);
            setErrorMessage("");
            setSelectedNode(null);
            setLearningContent(null);

            const response = await axiosInstance.get(
                `/api/v1/exam-versions/${examVersionId}/syllabus`
            );

            setSyllabus(response.data.data || []);
        } catch (error) {
            console.error(error);
            setErrorMessage("목차 조회에 실패했습니다.");
        } finally {
            setSyllabusLoading(false);
        }
    };

    const handleSelectNode = async (node) => {
        setSelectedNode(node);
        setLearningContent(null);
        setErrorMessage("");

        if (!node.isLeaf) {
            return;
        }

        try {
            setContentLoading(true);

            const response = await axiosInstance.get(
                `/api/v1/learning/contents/${node.id}`
            );

            setLearningContent(response.data.data);
        } catch (error) {
            console.error(error);
            setErrorMessage(
                "개념 설명 조회에 실패했습니다. 해당 소목차에 데이터가 있는지 확인해주세요."
            );
        } finally {
            setContentLoading(false);
        }
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
                                <p className="info-text">개념 설명을 불러오는 중입니다...</p>
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
                                </div>
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