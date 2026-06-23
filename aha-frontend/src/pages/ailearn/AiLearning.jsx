import { useEffect, useMemo, useRef, useState } from "react";
import ReactMarkdown from "react-markdown";
import UploadProgressModal from "../../components/ailearn/UploadProgressModal";
import {
    getDocumentProcessingStatus,
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

    const selectedUserExam = useMemo(() => {
        return userExams.find(
            (userExam) => userExam.userExamId === selectedUserExamId
        );
    }, [userExams, selectedUserExamId]);

    const selectedExamVersionId = selectedUserExam?.examVersionId ?? null;

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
                    setMessage("문서 처리가 완료되었습니다.");

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

            setLearningContent(getApiData(response));
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
                                <option value="">표시 시험 없음</option>
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
                    </div>

                    <div className="concept-title-wrap">
                        <h1>개념 학습</h1>
                        <span>
                            {learningContent ? "개념 설명 생성 완료" : "문서 업로드 전"}
                        </span>
                    </div>
                </div>
            </header>

            {message && <p className="concept-message">{message}</p>}

            {userExams.length === 0 ? (
                <section className="concept-empty-state">
                    <h2>표시 중인 시험이 없습니다</h2>
                    <p>
                        마이페이지에서 학습할 시험의 표시 설정을 켜면
                        개념학습 화면에서 확인할 수 있습니다.
                    </p>
                </section>
            ) : (
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

                        <div className="toc-guide-card">
                            <span className="guide-file-icon">▣</span>
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
                                <span className={learningContent ? "status-pill ready" : "status-pill"}>
                                    {isContentLoading
                                        ? "불러오는 중"
                                        : learningContent
                                            ? "생성 완료"
                                            : "문서 업로드 전"}
                                </span>
                            </div>

                            <button
                                type="button"
                                className="icon-button"
                                aria-label="확대"
                            >
                                ↗
                            </button>
                        </div>

                        {isContentLoading ? (
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
                        ) : (
                            <div className="upload-dropzone">
                                <div className="upload-empty-content">
                                    <div className="upload-doc-icon" aria-hidden="true">
                                        <svg
                                            width="100"
                                            height="100"
                                            viewBox="0 0 100 100"
                                            fill="none"
                                        >
                                            <path
                                                d="M33 18H58L74 34V72C74 76.4183 70.4183 80 66 80H33C28.5817 80 25 76.4183 25 72V26C25 21.5817 28.5817 18 33 18Z"
                                                stroke="#C8D2DF"
                                                strokeWidth="2.4"
                                                fill="white"
                                            />
                                            <path
                                                d="M58 18V30C58 34.4183 61.5817 38 66 38H74"
                                                stroke="#F7B182"
                                                strokeWidth="2.4"
                                            />
                                            <path
                                                d="M38 46H60"
                                                stroke="#C8D2DF"
                                                strokeWidth="2.4"
                                                strokeLinecap="round"
                                            />
                                            <path
                                                d="M38 56H60"
                                                stroke="#C8D2DF"
                                                strokeWidth="2.4"
                                                strokeLinecap="round"
                                            />
                                        </svg>
                                    </div>

                                    <h3>
                                        개념 설명을 보려면
                                        <br />
                                        먼저 문서를 업로드해 주세요.
                                    </h3>

                                    <p className="upload-description">
                                        업로드한 문서를 AI가 분석한 뒤,
                                        <br />
                                        선택한 시험의 목차와 연결해 개념 설명으로 정리해드려요.
                                    </p>

                                    <div className="upload-process">
                                        <div className="upload-process-item">
                                            <span className="process-icon">📄</span>
                                            <strong>문서 업로드</strong>
                                        </div>
                                        <span className="process-arrow">→</span>

                                        <div className="upload-process-item">
                                            <span className="process-icon">AI</span>
                                            <strong>AI 분석</strong>
                                        </div>
                                        <span className="process-arrow">→</span>

                                        <div className="upload-process-item">
                                            <span className="process-icon">🔗</span>
                                            <strong>목차 매핑</strong>
                                        </div>
                                        <span className="process-arrow">→</span>

                                        <div className="upload-process-item">
                                            <span className="process-icon">📘</span>
                                            <strong>개념 설명 생성</strong>
                                        </div>
                                    </div>

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
                                        <span>학습 문서 업로드</span>
                                    </button>
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
