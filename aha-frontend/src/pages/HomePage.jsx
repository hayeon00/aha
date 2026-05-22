import { useEffect, useState } from "react";
import axiosInstance from "../api/axiosInstance";
import SyllabusTree from "../components/exam/SyllabusTree";

function HomePage() {
    const [examVersionId, setExamVersionId] = useState(1);
    const [syllabus, setSyllabus] = useState([]);
    const [selectedNode, setSelectedNode] = useState(null);
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    const fetchSyllabus = async () => {
        try {
            setLoading(true);
            setErrorMessage("");

            const response = await axiosInstance.get(
                `/api/v1/exam-versions/${examVersionId}/syllabus`
            );

            setSyllabus(response.data.data);
        } catch (error) {
            console.error(error);
            setErrorMessage("목차 조회에 실패했습니다. 백엔드 서버와 API 경로를 확인해주세요.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchSyllabus();
    }, []);

    return (
        <main className="page">
            <section className="hero-section">
                <p className="eyebrow">Aha Learning Platform</p>
                <h1>SQLD 목차 조회 테스트</h1>
                <p className="hero-description">
                    백엔드 목차 조회 API를 호출해서 React 화면에 시험 목차 트리를 표시합니다.
                </p>
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

            {loading && <p className="info-text">목차를 불러오는 중입니다...</p>}
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
                        onSelectNode={setSelectedNode}
                    />
                </aside>

                <section className="detail-panel">
                    <h2>선택한 목차 정보</h2>

                    {selectedNode ? (
                        <div className="node-detail-card">
                            <p>
                                <strong>ID</strong>
                                <span>{selectedNode.id}</span>
                            </p>
                            <p>
                                <strong>코드</strong>
                                <span>{selectedNode.code}</span>
                            </p>
                            <p>
                                <strong>제목</strong>
                                <span>{selectedNode.title}</span>
                            </p>
                            <p>
                                <strong>타입</strong>
                                <span>{selectedNode.nodeType}</span>
                            </p>
                            <p>
                                <strong>깊이</strong>
                                <span>{selectedNode.depth}</span>
                            </p>
                            <p>
                                <strong>Leaf 여부</strong>
                                <span>{selectedNode.isLeaf ? "예" : "아니오"}</span>
                            </p>
                        </div>
                    ) : (
                        <p className="empty-text">
                            왼쪽 목차를 클릭하면 상세 정보가 표시됩니다.
                        </p>
                    )}
                </section>
            </section>
        </main>
    );
}

export default HomePage;