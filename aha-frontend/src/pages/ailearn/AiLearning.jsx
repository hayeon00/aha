import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import ReactMarkdown from "react-markdown";
import UploadProgressModal from "../../components/ailearn/UploadProgressModal";
import {
    getDocumentProcessingStatus,
    getUserExamDocumentState,
    uploadLearningDocuments,
} from "../../api/ailearn/documentApi";
import { getExamScopeNodes } from "../../api/exam/examApi";
import { getUserLearningContent } from "../../api/ailearn/learningContentApi";
import { getVisibleUserExams } from "../../api/exam/userExamApi";
import "./AiLearning.css";

const coachFeatures = [
    {
        icon: "summary",
        title: "쉬운 설명",
        description: "복잡한 개념을 쉽게 풀어드려요.",
    },
    {
        icon: "list",
        title: "핵심 요약",
        description: "중요한 내용을 빠르게 요약해요.",
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
    const navigate = useNavigate();
    const fileInputRef = useRef(null);
    const [userExams, setUserExams] = useState([]);
    const [selectedUserExamId, setSelectedUserExamId] = useState(null);

    const [scopeNodes, setScopeNodes] = useState([]);
    const [expandedNodeIds, setExpandedNodeIds] = useState([]);
    const [selectedNodeId, setSelectedNodeId] = useState(null);

    const [isExamLoading, setIsExamLoading] = useState(true);
    const [isScopeLoading, setIsScopeLoading] = useState(false);
    const [isUploading, setIsUploading] = useState(false);
    const [processingId, setProcessingId] = useState(null);
    const [processingStatus, setProcessingStatus] = useState(null);
    const [isProgressModalOpen, setIsProgressModalOpen] = useState(false);
    const [uploadErrorMessage, setUploadErrorMessage] = useState("");
    const [message, setMessage] = useState("");

    const [learningContent, setLearningContent] = useState(null);
    const [isContentLoading, setIsContentLoading] = useState(false);
    const [contentErrorMessage, setContentErrorMessage] = useState("");
    const [hasProcessedDocuments, setHasProcessedDocuments] = useState(false);
    const [isDocumentStateLoading, setIsDocumentStateLoading] = useState(false);

    const selectedUserExam = useMemo(() => {
        return userExams.find(
            (userExam) => userExam.userExamId === selectedUserExamId
        );
    }, [userExams, selectedUserExamId]);

    const selectedExamVersionId = selectedUserExam?.examVersionId ?? null;

    useEffect(() => {
        if (!selectedUserExamId) {
            setHasProcessedDocuments(false);
            return;
        }

        fetchUserExamDocumentState(selectedUserExamId);
    }, [selectedUserExamId]);

    useEffect(() => {
        fetchVisibleUserExams();
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

    useEffect(() => {
        if (!selectedUserExamId || !selectedNodeId) {
            setLearningContent(null);
            setContentErrorMessage("");
            return;
        }

        fetchLearningContent(selectedUserExamId, selectedNodeId);
    }, [selectedUserExamId, selectedNodeId]);

    useEffect(() => {
        if (!processingId || !isProgressModalOpen) {
            return undefined;
        }

        const pollProcessingStatus = async () => {
            try {
                const response = await getDocumentProcessingStatus(processingId);
                const statusData = getApiData(response);

                if (!statusData) {
                    return;
                }

                setProcessingStatus(statusData);

                if (statusData.status === "COMPLETED") {
                    setIsProgressModalOpen(false);
                    setProcessingId(null);
                    setProcessingStatus(null);
                    setMessage("");
                    setHasProcessedDocuments(true);

                    if (selectedUserExamId) {
                        await fetchUserExamDocumentState(selectedUserExamId);
                    }

                    if (selectedUserExamId && selectedNodeId) {
                        await fetchLearningContent(
                            selectedUserExamId,
                            selectedNodeId
                        );
                    }
                }

                if (
                    statusData.status === "FAILED" ||
                    statusData.status === "PARTIAL_FAILED"
                ) {
                    setUploadErrorMessage(
                        statusData.errorMessage || "문서 처리 중 오류가 발생했습니다."
                    );
                    setProcessingId(null);
                }
            } catch (error) {
                console.error("문서 처리 상태 조회 실패:", error);
                setUploadErrorMessage("문서 처리 상태를 조회하지 못했습니다.");
            }
        };

        pollProcessingStatus();
        const intervalId = window.setInterval(pollProcessingStatus, 2000);

        return () => window.clearInterval(intervalId);
    }, [processingId, isProgressModalOpen, selectedUserExamId, selectedNodeId]);

    const getApiData = (response) => {
        if (!response) return null;

        if (response.data?.data !== undefined) {
            return response.data.data;
        }

        if (response.data !== undefined) {
            return response.data;
        }

        return response;
    };

    const fetchUserExamDocumentState = async (userExamId) => {
        try {
            setIsDocumentStateLoading(true);

            const response = await getUserExamDocumentState(userExamId);
            const stateData = getApiData(response);

            setHasProcessedDocuments(
                Boolean(stateData?.hasUploadedDocuments)
            );
        } catch (error) {
            console.error("문서 업로드 상태 조회 실패:", error);
            setHasProcessedDocuments(false);
        } finally {
            setIsDocumentStateLoading(false);
        }
    };

    const fetchVisibleUserExams = async () => {
        try {
            setIsExamLoading(true);
            setMessage("");

            const response = await getVisibleUserExams();
            const visibleUserExams = getApiData(response) || [];

            setUserExams(Array.isArray(visibleUserExams) ? visibleUserExams : []);
            setSelectedUserExamId(visibleUserExams[0]?.userExamId ?? null);
        } catch (error) {
            console.error("표시 시험 조회 실패:", error);
            setUserExams([]);
            setSelectedUserExamId(null);
            setMessage("표시 중인 시험 목록을 불러오지 못했습니다.");
        } finally {
            setIsExamLoading(false);
        }
    };

    const fetchScopeNodes = async (examVersionId) => {
        try {
            setIsScopeLoading(true);
            setMessage("");

            const response = await getExamScopeNodes(examVersionId);
            const nodes = getApiData(response) || [];

            setScopeNodes(Array.isArray(nodes) ? nodes : []);
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

    const fetchLearningContent = async (
        userExamId,
        examScopeNodeId
    ) => {
        try {
            setIsContentLoading(true);
            setContentErrorMessage("");

            const response = await getUserLearningContent(
                userExamId,
                examScopeNodeId
            );

            const contentData = getApiData(response);
            setLearningContent(contentData);

            if (contentData) {
                setHasProcessedDocuments(true);
            }
        } catch (error) {
            if (error.response?.status === 404) {
                setLearningContent(null);
                setContentErrorMessage("");
                return;
            }

            console.error("개념 설명 조회 실패:", error);
            setLearningContent(null);
            setContentErrorMessage(
                error.response?.data?.message ||
                "개념 설명을 불러오지 못했습니다."
            );
        } finally {
            setIsContentLoading(false);
        }
    };

    const getDefaultExpandedNodeIds = (nodes) => {
        const ids = [];

        const traverse = (items) => {
            items.forEach((item) => {
                ids.push(item.id);

                if (item.children?.length > 0) {
                    traverse(item.children);
                }
            });
        };

        traverse(nodes || []);
        return ids;
    };

    const findFirstSelectableNodeId = (nodes) => {
        for (const node of nodes || []) {
            if (node.children?.length > 0) {
                const childNodeId = findFirstSelectableNodeId(node.children);

                if (childNodeId) {
                    return childNodeId;
                }

                continue;
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
        if (node.children?.length > 0) {
            toggleNode(node.id);
            return;
        }

        setSelectedNodeId(node.id);
    };

    const handleUserExamChange = (userExamId) => {
        setSelectedUserExamId(userExamId);
        setLearningContent(null);
        setContentErrorMessage("");
    };

    const handleUploadClick = () => {
        fileInputRef.current?.click();
    };

    const handleFileChange = async (event) => {
        const files = Array.from(event.target.files || []);
        event.target.value = "";

        if (!selectedUserExamId || files.length === 0) {
            return;
        }

        try {
            setIsUploading(true);
            setMessage("");
            setUploadErrorMessage("");

            const response = await uploadLearningDocuments(selectedUserExamId, files);
            const uploadData = getApiData(response);
            const nextProcessingId =
                uploadData?.processingId || uploadData?.processingGroupId;

            if (!nextProcessingId) {
                throw new Error("processingId not found");
            }

            setProcessingId(nextProcessingId);
            setProcessingStatus(uploadData);
            setIsProgressModalOpen(true);
        } catch (error) {
            console.error("문서 업로드 실패:", error);
            setUploadErrorMessage("문서 업로드에 실패했습니다.");
            setProcessingStatus({
                status: "FAILED",
                currentStep: "FAILED",
                progressRate: 0,
                totalFileCount: files.length,
                completedFileCount: 0,
                errorMessage: "문서 업로드에 실패했습니다.",
            });
            setIsProgressModalOpen(true);
        } finally {
            setIsUploading(false);
        }
    };

    const handleCloseProgressModal = () => {
        setIsProgressModalOpen(false);
        setProcessingId(null);
    };

    if (isExamLoading) {
        return (
            <main className="concept-page">
                <div className="concept-loading-card">
                    <div className="concept-loading-spinner" />
                    <p>표시 중인 시험을 불러오는 중입니다...</p>
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
                            value={selectedUserExamId ?? ""}
                            onChange={(event) =>
                                handleUserExamChange(Number(event.target.value))
                            }
                            disabled={userExams.length === 0}
                        >
                            {userExams.length === 0 ? (
                                <option value="">활성 시험 없음</option>
                            ) : (
                                userExams.map((userExam) => (
                                    <option
                                        key={userExam.userExamId}
                                        value={userExam.userExamId}
                                    >
                                        {userExam.examCode}
                                    </option>
                                ))
                            )}
                        </select>

                        <svg
                            className="exam-select-arrow"
                            width="16"
                            height="16"
                            viewBox="0 0 16 16"
                            fill="none"
                            aria-hidden="true"
                        >
                            <path
                                d="M4 6L8 10L12 6"
                                stroke="currentColor"
                                strokeWidth="1.8"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                            />
                        </svg>
                    </div>

                    <div className="concept-title-wrap">
                        <h1>개념 학습</h1>
                    </div>
                </div>
            </header>

            {message && <p className="concept-message">{message}</p>}

            {userExams.length === 0 ? (
                <section className="concept-empty-state">
                    <div className="concept-empty-card">
                        <div className="concept-empty-illustration" aria-hidden="true">
                            <svg
                                width="72"
                                height="72"
                                viewBox="0 0 72 72"
                                fill="none"
                            >
                                <rect
                                    x="14"
                                    y="12"
                                    width="44"
                                    height="50"
                                    rx="11"
                                    fill="#FFFFFF"
                                    stroke="#E9CDB9"
                                    strokeWidth="2"
                                />
                                <path
                                    d="M25 28H47M25 37H44M25 46H38"
                                    stroke="#C7AA95"
                                    strokeWidth="2.3"
                                    strokeLinecap="round"
                                />
                                <circle
                                    cx="55"
                                    cy="54"
                                    r="11"
                                    fill="#FF7A18"
                                />
                                <path
                                    d="M55 49V59M50 54H60"
                                    stroke="#FFFFFF"
                                    strokeWidth="2.2"
                                    strokeLinecap="round"
                                />
                            </svg>
                        </div>

                        <h2>학습할 시험을 먼저 선택해 주세요</h2>

                        <p>
                            마이페이지에서 학습 시험을 활성화하면
                            개념 학습을 바로 시작할 수 있습니다.
                        </p>

                        <button
                            type="button"
                            className="concept-empty-action"
                            onClick={() => navigate("/mypage")}
                        >
                            <span>마이페이지로 이동</span>
                            <svg
                                width="17"
                                height="17"
                                viewBox="0 0 17 17"
                                fill="none"
                                aria-hidden="true"
                            >
                                <path
                                    d="M6 3.5L11 8.5L6 13.5"
                                    stroke="currentColor"
                                    strokeWidth="1.8"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                />
                            </svg>
                        </button>

                        <span className="concept-empty-caption">
                            학습 시험 설정에서 표시할 시험을 켜주세요.
                        </span>
                    </div>
                </section>
            ) : (
                <section className="concept-workspace">
                    <aside className="toc-panel">
                        <div className="panel-header">
                            <div className="panel-title">
                                <h2>학습 목차</h2>
                            </div>
                        </div>

                        <button type="button" className="toc-select">
                            {selectedUserExam?.examName || "시험 목차"}
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

                    </aside>

                    <section className="concept-main-panel">
                        <div className="panel-header">
                            <div className="panel-title">
                                <h2>개념 설명</h2>
                            </div>
                        </div>

                        {isDocumentStateLoading ? (
                            <div className="content-loading-state">
                                <div className="concept-loading-spinner" />
                                <p>문서 상태를 확인하는 중입니다...</p>
                            </div>
                        ) : isContentLoading ? (
                            <div className="content-loading-state">
                                <div className="concept-loading-spinner" />
                                <p>개념 설명을 불러오는 중입니다...</p>
                            </div>
                        ) : learningContent ? (
                            <article className="learning-content-view">
                                <div className="learning-content-head">
                                    <div>
                                        <p className="learning-content-path">
                                            {selectedUserExam?.examName || "개념 학습"}
                                        </p>
                                        <h3>{learningContent.title}</h3>
                                    </div>

                                    <button
                                        type="button"
                                        className="secondary-upload-button"
                                        onClick={handleUploadClick}
                                        disabled={isUploading}
                                    >
                                        문서 추가 업로드
                                    </button>
                                </div>

                                {contentErrorMessage && (
                                    <p className="content-error-message">
                                        {contentErrorMessage}
                                    </p>
                                )}

                                <div className="learning-content-markdown">
                                    <ReactMarkdown>
                                        {learningContent.content || ""}
                                    </ReactMarkdown>
                                </div>

                                {Array.isArray(learningContent.keywords) &&
                                    learningContent.keywords.length > 0 && (
                                        <div className="learning-keywords">
                                            <strong>핵심 키워드</strong>
                                            <div>
                                                {learningContent.keywords.map((keyword) => (
                                                    <span key={keyword}>{keyword}</span>
                                                ))}
                                            </div>
                                        </div>
                                    )}
                            </article>
                        ) : hasProcessedDocuments ? (
                            <div className="content-empty-state">
                                <div className="content-empty-icon" aria-hidden="true">
                                    <svg
                                        width="52"
                                        height="52"
                                        viewBox="0 0 52 52"
                                        fill="none"
                                    >
                                        <rect
                                            x="11"
                                            y="7"
                                            width="30"
                                            height="38"
                                            rx="7"
                                            fill="#FFF8F2"
                                            stroke="#F6C7A8"
                                            strokeWidth="1.8"
                                        />
                                        <path
                                            d="M18 20H34M18 27H30"
                                            stroke="#C9A995"
                                            strokeWidth="2"
                                            strokeLinecap="round"
                                        />
                                    </svg>
                                </div>
                                <h3>연결된 학습 내용이 없습니다</h3>
                                <p>
                                    선택한 목차와 연결된 문서 내용이 없어
                                    개념 설명이 생성되지 않았습니다.
                                </p>
                                <button
                                    type="button"
                                    className="empty-upload-button"
                                    onClick={handleUploadClick}
                                    disabled={isUploading}
                                >
                                    문서 추가 업로드
                                </button>
                            </div>
                        ) : (
                            <div className="upload-dropzone">
                                <div className="upload-empty-content">
                                    <div className="upload-doc-icon" aria-hidden="true">
                                        <svg
                                            width="84"
                                            height="84"
                                            viewBox="0 0 84 84"
                                            fill="none"
                                        >
                                            <rect
                                                x="20"
                                                y="11"
                                                width="44"
                                                height="58"
                                                rx="10"
                                                fill="#FFFFFF"
                                                stroke="#E6C7B1"
                                                strokeWidth="2"
                                            />
                                            <path
                                                d="M31 31H53M31 40H53M31 49H46"
                                                stroke="#C6A995"
                                                strokeWidth="2.4"
                                                strokeLinecap="round"
                                            />
                                            <circle
                                                cx="61"
                                                cy="62"
                                                r="13"
                                                fill="#FF7A18"
                                            />
                                            <path
                                                d="M61 67V56M56.5 60.5L61 56L65.5 60.5"
                                                stroke="white"
                                                strokeWidth="2.2"
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                            />
                                        </svg>
                                    </div>

                                    <h3>학습 문서를 업로드해 주세요</h3>

                                    <p className="upload-description">
                                        PDF 파일을 선택해 개념 학습을 시작하세요.
                                    </p>

                                    <button
                                        type="button"
                                        className="main-upload-button"
                                        onClick={handleUploadClick}
                                        disabled={isUploading || !selectedUserExamId}
                                    >
                                        <svg
                                            width="18"
                                            height="18"
                                            viewBox="0 0 24 24"
                                            fill="none"
                                            aria-hidden="true"
                                        >
                                            <path
                                                d="M12 15.5V4.5"
                                                stroke="currentColor"
                                                strokeWidth="2.4"
                                                strokeLinecap="round"
                                            />
                                            <path
                                                d="M7.5 9L12 4.5L16.5 9"
                                                stroke="currentColor"
                                                strokeWidth="2.4"
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                            />
                                            <path
                                                d="M5 19.5H19"
                                                stroke="currentColor"
                                                strokeWidth="2.4"
                                                strokeLinecap="round"
                                            />
                                        </svg>
                                        <span>PDF 업로드</span>
                                    </button>

                                    <span className="upload-hint">
                                        여러 개의 PDF 파일을 한 번에 선택할 수 있습니다.
                                    </span>
                                </div>
                            </div>
                        )}

                        <input
                            ref={fileInputRef}
                            type="file"
                            accept=".pdf,application/pdf"
                            multiple
                            className="upload-file-input"
                            onChange={handleFileChange}
                        />

                    </section>

                    <aside className="coach-panel">
                        <div className="panel-header">
                            <div className="panel-title">
                                <h2>AI 코치</h2>
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
                                <div
                                    className="coach-feature"
                                    key={feature.title + feature.description}
                                >
                                    <span className={`coach-feature-icon ${feature.icon}`}>
                                        {feature.icon === "summary" && "▤"}
                                        {feature.icon === "list" && "☰"}
                                        {feature.icon === "target" && "◎"}
                                        {feature.icon === "compare" && "⌘"}
                                        {feature.icon === "question" && "?"}
                                    </span>

                                    <div>
                                        <strong>{feature.title}</strong>
                                        <p>{feature.description}</p>
                                    </div>
                                </div>
                            ))}
                        </div>

                    </aside>
                </section>
            )}

            <UploadProgressModal
                open={isProgressModalOpen}
                status={processingStatus}
                errorMessage={uploadErrorMessage}
                onClose={handleCloseProgressModal}
            />
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
