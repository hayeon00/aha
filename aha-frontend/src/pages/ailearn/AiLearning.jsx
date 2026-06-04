import { useEffect, useMemo, useState } from "react";
import { getExams, getExamScopeNodes } from "../../api/exam/examApi";
import "./AiLearning.css";

const coachFeatures = [
    {
        icon: "book",
        title: "쉬운 설명",
        description: "복잡한 개념을 쉽게 풀어드려요.",
    },
    {
        icon: "summary",
        title: "핵심 요약",
        description: "중요한 내용을 핵심만 요약해요.",
    },
    {
        icon: "target",
        title: "출제 포인트",
        description: "시험에 자주 나오는 포인트를 짚어드려요.",
    },
    {
        icon: "compare",
        title: "헷갈리는 개념 비교",
        description: "유사한 개념을 비교해드립니다.",
    },
    {
        icon: "question",
        title: "예상 질문 만들기",
        description: "스스로 점검할 수 있는 문제를 만들어요.",
    },
];

function AiLearning() {
    const [exams, setExams] = useState([]);
    const [selectedExamId, setSelectedExamId] = useState(null);
    const [scopeNodes, setScopeNodes] = useState([]);
    const [expandedNodeIds, setExpandedNodeIds] = useState([]);
    const [selectedNodeId, setSelectedNodeId] = useState(null);

    const [isExamLoading, setIsExamLoading] = useState(true);
    const [isScopeLoading, setIsScopeLoading] = useState(false);
    const [message, setMessage] = useState("");

    const selectedExam = useMemo(() => {
        return exams.find((exam) => exam.id === selectedExamId);
    }, [exams, selectedExamId]);

    const selectedExamVersionId = useMemo(() => {
        if (!selectedExam) return null;

        return (
            selectedExam.activeVersionId ||
            selectedExam.examVersionId ||
            selectedExam.versionId ||
            null
        );
    }, [selectedExam]);

    useEffect(() => {
        fetchExams();
    }, []);

    useEffect(() => {
        if (!selectedExamVersionId) {
            setScopeNodes([]);
            setExpandedNodeIds([]);
            setSelectedNodeId(null);
            return;
        }

        fetchScopeNodes(selectedExamVersionId);
    }, [selectedExamVersionId]);

    const fetchExams = async () => {
        try {
            setIsExamLoading(true);
            setMessage("");

            const response = await getExams();
            const examList = response.data || [];

            setExams(examList);
            setSelectedExamId(examList[0]?.id ?? null);
        } catch (error) {
            console.error("지원 시험 조회 실패:", error);
            setMessage("지원 시험 목록을 불러오지 못했습니다.");
        } finally {
            setIsExamLoading(false);
        }
    };

    const fetchScopeNodes = async (examVersionId) => {
        try {
            setIsScopeLoading(true);
            setMessage("");

            const response = await getExamScopeNodes(examVersionId);
            const nodes = response.data || [];

            setScopeNodes(nodes);
            setExpandedNodeIds(getDefaultExpandedNodeIds(nodes));
            setSelectedNodeId(findFirstSelectableNodeId(nodes));
        } catch (error) {
            console.error("시험 목차 조회 실패:", error);
            setScopeNodes([]);
            setExpandedNodeIds([]);
            setSelectedNodeId(null);
            setMessage("시험 목차를 불러오지 못했습니다.");
        } finally {
            setIsScopeLoading(false);
        }
    };

    const getDefaultExpandedNodeIds = (nodes) => {
        const ids = [];

        nodes.forEach((node) => {
            ids.push(node.id);

            if (node.children?.length > 0) {
                node.children.forEach((child) => {
                    ids.push(child.id);
                });
            }
        });

        return ids;
    };

    const findFirstSelectableNodeId = (nodes) => {
        for (const node of nodes) {
            if (node.children?.length > 0) {
                return node.children[0].id;
            }

            return node.id;
        }

        return null;
    };

    const toggleNode = (nodeId) => {
        setExpandedNodeIds((prev) => {
            if (prev.includes(nodeId)) {
                return prev.filter((id) => id !== nodeId);
            }

            return [...prev, nodeId];
        });
    };

    const handleSelectNode = (node) => {
        setSelectedNodeId(node.id);

        if (node.children?.length > 0) {
            toggleNode(node.id);
        }
    };

    const handleExamChange = (examId) => {
        setSelectedExamId(examId);
    };

    const handleUploadClick = () => {
        alert("학습 문서 업로드 기능은 다음 단계에서 연결하면 됩니다.");
    };

    if (isExamLoading) {
        return (
            <main className="concept-page">
                <div className="concept-loading-card">
                    <div className="concept-loading-spinner" />
                    <p>지원 시험을 불러오는 중입니다...</p>
                </div>
            </main>
        );
    }

    return (
        <main className="concept-page">
            <header className="concept-topbar">
                <div className="concept-topbar-left">
                    <div className="exam-dropdown-wrap">
                        <select
                            className="exam-select-control"
                            value={selectedExamId ?? ""}
                            onChange={(event) =>
                                handleExamChange(Number(event.target.value))
                            }
                        >
                            {exams.map((exam) => (
                                <option key={exam.id} value={exam.id}>
                                    {exam.code}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="concept-title-wrap">
                        <h1>개념 학습</h1>
                        <span>문서 업로드 전</span>
                    </div>
                </div>
            </header>

            {message && <p className="concept-message">{message}</p>}

            <section className="concept-workspace">
                <aside className="toc-panel">
                    <div className="panel-header">
                        <div className="panel-title">
                            <h2>학습 목차</h2>
                            <span className="info-dot">i</span>
                        </div>

                        <button
                            type="button"
                            className="icon-button"
                            aria-label="목차 설정"
                        >
                            ⚙
                        </button>
                    </div>

                    <button type="button" className="toc-select">
                        {selectedExam?.name || "시험 목차"}
                        <span>⌄</span>
                    </button>

                    <div className="toc-list">
                        {isScopeLoading ? (
                            <div className="toc-loading">
                                목차를 불러오는 중입니다...
                            </div>
                        ) : scopeNodes.length === 0 ? (
                            <div className="toc-empty">
                                등록된 목차가 없습니다.
                            </div>
                        ) : (
                            scopeNodes.map((node, index) => (
                                <ScopeTreeNode
                                    key={node.id}
                                    node={node}
                                    numberPrefix={`${index + 1}`}
                                    level={1}
                                    selectedNodeId={selectedNodeId}
                                    expandedNodeIds={expandedNodeIds}
                                    onToggle={toggleNode}
                                    onSelect={handleSelectNode}
                                />
                            ))
                        )}
                    </div>

                    <div className="toc-guide-card">
                        <span className="guide-file-icon">▤</span>
                        <p>
                            문서를 업로드하면 목차별
                            <br />
                            개념 설명이 자동으로 연결됩니다.
                        </p>
                    </div>
                </aside>

                <section className="concept-main-panel">
                    <div className="panel-header">
                        <div className="panel-title">
                            <h2>개념 설명</h2>
                            <span className="status-pill">문서 업로드 전</span>
                        </div>

                        <button
                            type="button"
                            className="icon-button"
                            aria-label="학습 자료"
                        >
                            📖
                        </button>
                    </div>

                    <div className="upload-dropzone">
                        <div className="upload-illustration">
                            <div className="paper paper-back" />
                            <div className="paper paper-main">
                                <span />
                                <span />
                            </div>

                            <button
                                type="button"
                                className="upload-circle-button"
                                onClick={handleUploadClick}
                                aria-label="학습 문서 업로드"
                            >
                                ↑
                            </button>
                        </div>

                        <h3>
                            개념 설명을 보려면
                            <br />
                            먼저 문서를 업로드해 주세요.
                        </h3>

                        <p>
                            PDF, TXT, DOCX 파일 분석 가능
                            <br />
                            다중 파일 업로드 지원
                        </p>

                        <button
                            type="button"
                            className="main-upload-button"
                            onClick={handleUploadClick}
                        >
                            <span>↥</span>
                            학습 문서 업로드
                        </button>

                        <div className="file-type-row">
                            <div className="file-type pdf">PDF</div>
                            <div className="file-type txt">TXT</div>
                            <div className="file-type docx">DOCX</div>
                        </div>
                    </div>

                    <footer className="concept-safe-box">
                        <div>
                            <span className="shield-icon">♡</span>
                            업로드한 파일은 안전하게 보호되며, 학습 분석 목적으로만 활용됩니다.
                        </div>

                        <button type="button">
                            자세히 보기
                            <span>›</span>
                        </button>
                    </footer>
                </section>

                <aside className="coach-panel">
                    <div className="panel-header">
                        <div className="panel-title">
                            <h2>AI 코치</h2>
                            <span className="disabled-pill">비활성</span>
                        </div>
                    </div>

                    <div className="coach-empty">
                        <div className="coach-robot">
                            <div className="robot-antenna" />
                            <div className="robot-head">
                                <span />
                                <span />
                            </div>
                            <div className="robot-ear left" />
                            <div className="robot-ear right" />
                            <div className="robot-bubble" />
                        </div>

                        <h3>
                            학습 문서를 업로드하면
                            <br />
                            AI 코치가 함께 학습을 도와드려요!
                        </h3>
                    </div>

                    <div className="coach-divider">
                        <span />
                        AI 코치가 도와드릴 수 있어요
                        <span />
                    </div>

                    <div className="coach-feature-list">
                        {coachFeatures.map((feature) => (
                            <div className="coach-feature" key={feature.title}>
                                <span className={`coach-feature-icon ${feature.icon}`}>
                                    {feature.icon === "book" && "▥"}
                                    {feature.icon === "summary" && "☷"}
                                    {feature.icon === "target" && "◎"}
                                    {feature.icon === "compare" && "⚖"}
                                    {feature.icon === "question" && "?"}
                                </span>

                                <div>
                                    <strong>{feature.title}</strong>
                                    <p>{feature.description}</p>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="coach-bottom-card">
                        <span>▣</span>
                        <p>
                            문서 업로드 후 모든 AI 코치 기능을
                            <br />
                            활용할 수 있습니다.
                        </p>
                    </div>
                </aside>
            </section>
        </main>
    );
}

function ScopeTreeNode({
                           node,
                           numberPrefix,
                           level,
                           selectedNodeId,
                           expandedNodeIds,
                           onToggle,
                           onSelect,
                       }) {
    const hasChildren = node.children && node.children.length > 0;
    const isExpanded = expandedNodeIds.includes(node.id);
    const isSelected = selectedNodeId === node.id;

    return (
        <div className={`toc-node toc-node-level-${level}`}>
            <button
                type="button"
                className={isSelected ? "toc-node-row active" : "toc-node-row"}
                onClick={() => onSelect(node)}
            >
                <span className="toc-dot" />
                <span className="toc-node-title">
                    {numberPrefix}. {node.title}
                </span>

                {hasChildren && (
                    <span
                        className={isExpanded ? "toc-arrow open" : "toc-arrow"}
                        onClick={(event) => {
                            event.stopPropagation();
                            onToggle(node.id);
                        }}
                    >
                        ⌄
                    </span>
                )}
            </button>

            {hasChildren && isExpanded && (
                <div className="toc-children">
                    {node.children.map((child, index) => (
                        <ScopeTreeNode
                            key={child.id}
                            node={child}
                            numberPrefix={`${numberPrefix}.${index + 1}`}
                            level={level + 1}
                            selectedNodeId={selectedNodeId}
                            expandedNodeIds={expandedNodeIds}
                            onToggle={onToggle}
                            onSelect={onSelect}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}

export default AiLearning;