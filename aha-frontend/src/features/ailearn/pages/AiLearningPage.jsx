import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import ConceptContentView from "../components/ConceptContentView.jsx";
import UploadProgressModal from "../components/UploadProgressModal.jsx";
import { useDocumentProcessing } from "../hooks/useDocumentProcessing.js";
import { useDocumentConceptDashboard } from "../hooks/useDocumentConceptDashboard.js";
import { useUserExams } from "../../exam/hooks/useUserExams.js";
import { useExamScopeNodes } from "../../exam/hooks/useExamScopeNodes.js";

import "./AiLearningPage.css";

const ACCEPTED_EXTENSIONS = [".pdf", ".docx"];

function AiLearningPage() {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const fileInputRef = useRef(null);
    const [isDragging, setIsDragging] = useState(false);
    const [localFiles, setLocalFiles] = useState([]);
    const [hiddenDocumentIds, setHiddenDocumentIds] = useState([]);
    const [fileMessage, setFileMessage] = useState("");

    const {
        userExams,
        selectedUserExamId,
        selectedExamVersionId,
        isExamLoading,
        examMessage,
        changeUserExam,
    } = useUserExams();

    const {
        scopeNodes,
        expandedNodeIds,
        selectedNodeId,
        isScopeLoading,
        scopeMessage,
        toggleNode,
        handleSelectNode,
        resetScopeNodes,
    } = useExamScopeNodes({ examVersionId: selectedExamVersionId });

    const {
        isUploading,
        processingStatus,
        isProgressModalOpen,
        uploadErrorMessage,
        hasProcessedDocuments,
        completedProcessingKey,
        isRetrying,
        uploadDocuments,
        retryProcessing,
        closeProgressModal,
        resetDocumentState,
    } = useDocumentProcessing({ selectedUserExamId, selectedNodeId });

    const documentRoom = useDocumentConceptDashboard({
        userExamId: selectedUserExamId,
        enabled: Boolean(selectedUserExamId) && hasProcessedDocuments,
    });

    useEffect(() => {
        if (completedProcessingKey > 0) {
            queueMicrotask(() => {
                documentRoom.refresh();
                setLocalFiles([]);
            });
        }
        // documentRoom.refresh is intentionally excluded because the hook returns
        // a new object on every render.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [completedProcessingKey]);

    const uploadedDocuments = useMemo(() => {
        const serverDocuments = documentRoom.documents
            .filter((document) => !hiddenDocumentIds.includes(document.documentId))
            .map((document) => ({
                id: `server-${document.documentId}`,
                documentId: document.documentId,
                name: document.fileName,
                status: "업로드 완료",
                meta: "AI 분석 완료",
                local: false,
            }));

        return [...localFiles, ...serverDocuments];
    }, [documentRoom.documents, hiddenDocumentIds, localFiles]);

    const completedDocuments = uploadedDocuments.filter((document) => !document.local);
    const canStartLearning = completedDocuments.length > 0 && !isUploading && !documentRoom.loading;
    const isStudyMode = searchParams.get("view") === "notes";
    const dashboardChapters = [
        ...(documentRoom.dashboard?.mapped ?? []).map((chapter) => ({ ...chapter, mapped: true })),
        ...(documentRoom.dashboard?.unmapped ?? []).map((chapter) => ({ ...chapter, mapped: false })),
    ];
    const selectedChapter = dashboardChapters.find(
        (chapter) => Number(chapter.tocId) === Number(selectedNodeId),
    ) ?? null;

    const message = examMessage || scopeMessage || fileMessage;

    const handleUserExamChange = (userExamId) => {
        changeUserExam(userExamId);
        resetScopeNodes();
        resetDocumentState();
        setLocalFiles([]);
        setHiddenDocumentIds([]);
        setFileMessage("");
    };

    const handleFiles = async (fileList) => {
        const files = Array.from(fileList || []);
        if (!files.length || isUploading) return;

        const invalidFiles = files.filter((file) => {
            const name = file.name.toLowerCase();
            return !ACCEPTED_EXTENSIONS.some((extension) => name.endsWith(extension));
        });

        if (invalidFiles.length) {
            setFileMessage("PDF 또는 DOCX 파일만 업로드할 수 있습니다.");
            return;
        }

        setFileMessage("");
        setLocalFiles((current) => [
            ...files.map((file, index) => ({
                id: `local-${file.name}-${file.lastModified}-${index}`,
                name: file.name,
                status: "업로드 중",
                meta: formatFileSize(file.size),
                local: true,
            })),
            ...current,
        ]);
        await uploadDocuments(files);
    };

    const handleFileChange = async (event) => {
        const files = event.target.files;
        event.target.value = "";
        await handleFiles(files);
    };

    const handleDrop = async (event) => {
        event.preventDefault();
        setIsDragging(false);
        await handleFiles(event.dataTransfer.files);
    };

    const handleRemoveDocument = (document) => {
        if (document.local) {
            setLocalFiles((current) => current.filter((file) => file.id !== document.id));
            return;
        }
        setHiddenDocumentIds((current) => [...current, document.documentId]);
    };

    const handleStartLearning = () => {
        if (!canStartLearning) return;
        const firstDocumentId = completedDocuments[0].documentId;
        if (firstDocumentId && documentRoom.documentId !== firstDocumentId) {
            documentRoom.selectDocument(firstDocumentId);
        }
        setSearchParams({ view: "notes" });
    };

    if (isExamLoading) {
        return (
            <main className="concept-page concept-page-loading">
                <div className="concept-loading-card">
                    <div className="concept-loading-spinner" />
                    <p>학습 정보를 불러오는 중입니다...</p>
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
                            onChange={(event) => handleUserExamChange(Number(event.target.value))}
                            disabled={userExams.length === 0}
                            aria-label="학습 시험 선택"
                        >
                            {userExams.length === 0 ? (
                                <option value="">활성 시험 없음</option>
                            ) : userExams.map((userExam) => (
                                <option key={userExam.userExamId} value={userExam.userExamId}>
                                    {userExam.examCode}
                                </option>
                            ))}
                        </select>
                        <ChevronDownIcon className="exam-select-arrow" />
                    </div>
                    <h1>개념 학습</h1>
                </div>
            </header>

            {message && <p className="concept-message">{message}</p>}

            {userExams.length === 0 ? (
                <section className="concept-empty-state">
                    <div className="concept-empty-card">
                        <DocumentIcon />
                        <h2>학습할 시험을 먼저 선택해 주세요</h2>
                        <p>마이페이지에서 학습 시험을 활성화하면 개념 학습을 시작할 수 있습니다.</p>
                        <button type="button" onClick={() => navigate("/mypage")}>
                            마이페이지로 이동
                        </button>
                    </div>
                </section>
            ) : isStudyMode ? (
                <section className="study-notes-workspace">
                    <aside className="workspace-card study-notes-toc">
                        <button
                            type="button"
                            className="study-back-button"
                            onClick={() => setSearchParams({})}
                        >
                            <ArrowLeftIcon /> 문서 관리로 돌아가기
                        </button>
                        <div className="study-section-heading">
                            <span>LEARNING NOTE</span>
                            <h2>목차별 개념 노트</h2>
                        </div>
                        <div className="toc-list">
                            <StudyTocTree
                                nodes={scopeNodes}
                                expandedNodeIds={expandedNodeIds}
                                selectedNodeId={selectedNodeId}
                                onToggle={toggleNode}
                                onSelect={handleSelectNode}
                            />
                        </div>
                    </aside>

                    <section className="study-notes-content">
                        <ConceptContentView
                            chapter={selectedChapter}
                            loading={documentRoom.loading}
                            generating={documentRoom.generatingIds.includes(selectedChapter?.tocId)}
                        />
                    </section>

                    <aside className="workspace-card study-source-panel">
                        <div className="study-section-heading">
                            <span>SOURCE DOCUMENT</span>
                            <h2>학습 자료</h2>
                        </div>
                        <div className="study-source-list">
                            {completedDocuments.map((document) => (
                                <button
                                    type="button"
                                    key={document.id}
                                    className={document.documentId === documentRoom.documentId ? "selected" : ""}
                                    onClick={() => documentRoom.selectDocument(document.documentId)}
                                >
                                    <span className={`file-type ${getFileType(document.name).toLowerCase()}`}>
                                        {getFileType(document.name)}
                                    </span>
                                    <strong>{document.name}</strong>
                                    <CheckCircleIcon />
                                </button>
                            ))}
                        </div>
                        <p className="study-source-caption">
                            선택한 문서를 바탕으로 목차별 핵심 개념을 정리했어요.
                        </p>
                    </aside>
                </section>
            ) : (
                <section className="concept-workspace">
                    <aside className="workspace-card toc-panel">
                        <div className="toc-list">
                            {isScopeLoading ? (
                                <PanelLoading label="목차를 불러오는 중입니다..." />
                            ) : scopeNodes.length === 0 ? (
                                <div className="panel-empty">등록된 목차가 없습니다.</div>
                            ) : (
                                <StudyTocTree
                                    nodes={scopeNodes}
                                    expandedNodeIds={expandedNodeIds}
                                    onToggle={toggleNode}
                                />
                            )}
                        </div>
                    </aside>

                    <section className="workspace-card upload-panel">
                        <div
                            className={`upload-dropzone ${isDragging ? "is-dragging" : ""} ${isUploading ? "is-uploading" : ""}`}
                            onDragEnter={(event) => {
                                event.preventDefault();
                                setIsDragging(true);
                            }}
                            onDragOver={(event) => event.preventDefault()}
                            onDragLeave={(event) => {
                                if (!event.currentTarget.contains(event.relatedTarget)) setIsDragging(false);
                            }}
                            onDrop={handleDrop}
                            onClick={() => !isUploading && fileInputRef.current?.click()}
                            role="button"
                            tabIndex={0}
                            onKeyDown={(event) => {
                                if (event.key === "Enter" || event.key === " ") {
                                    fileInputRef.current?.click();
                                }
                            }}
                            aria-label="학습 문서 업로드"
                        >
                            <div className="upload-visual" aria-hidden="true"><UploadCloudIcon /></div>
                            <h3>
                                {isUploading
                                    ? "문서를 안전하게 업로드하고 있어요"
                                    : isDragging
                                        ? "여기에 문서를 놓아주세요"
                                        : "학습 자료 추가"}
                            </h3>
                            <p>
                                {isUploading ? (
                                    <span className="upload-accent">업로드와 분석을 진행하고 있습니다</span>
                                ) : (
                                    <>파일을 드롭하거나 <span className="upload-accent">클릭하여 선택</span></>
                                )}
                            </p>
                            <span className="upload-hint">PDF, DOCX · 최대 10개 파일</span>
                        </div>

                        <input
                            ref={fileInputRef}
                            type="file"
                            accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            multiple
                            className="upload-file-input"
                            onChange={handleFileChange}
                        />
                    </section>

                    <aside className="workspace-card documents-panel">
                        <div className="documents-micro-header">
                            <span>MY DOCUMENTS</span>
                            <i />
                            <strong>{uploadedDocuments.length}개</strong>
                        </div>

                        <div className="document-list">
                            {documentRoom.loading && uploadedDocuments.length === 0 ? (
                                <PanelLoading label="자료를 불러오는 중입니다..." />
                            ) : uploadedDocuments.length === 0 ? (
                                <div className="documents-empty">
                                    <span><FileIcon /></span>
                                    <strong>아직 업로드된 자료가 없어요</strong>
                                    <p>가운데 영역에서 학습 문서를 추가해 주세요.</p>
                                </div>
                            ) : uploadedDocuments.map((document) => (
                                <article className="document-card" key={document.id}>
                                    <span className={`file-type ${getFileType(document.name).toLowerCase()}`}>
                                        {getFileType(document.name)}
                                    </span>
                                    <div className="document-info">
                                        <strong title={document.name}>{document.name}</strong>
                                        <span>
                                            {document.meta}
                                            <i className={document.local ? "processing" : ""} />
                                            {document.status}
                                        </span>
                                    </div>
                                    <button
                                        type="button"
                                        className="trash-button"
                                        onClick={() => handleRemoveDocument(document)}
                                        aria-label={`${document.name} 목록에서 삭제`}
                                    >
                                        <TrashIcon />
                                    </button>
                                </article>
                            ))}
                        </div>

                        <div className="documents-action-area">
                            <div className="learning-start-caption">
                                <span aria-hidden="true">💡</span>
                                <p>업로드한 문서를 바탕으로 목차별 맞춤 설명이 생성돼요.</p>
                            </div>
                            <button
                                type="button"
                                className={`learning-start-button ${canStartLearning ? "is-active" : "is-disabled"}`}
                                disabled={!canStartLearning}
                                onClick={handleStartLearning}
                            >
                                <span className="learning-start-sparkle">✦</span>
                                <span>{isUploading ? "문서 분석 중..." : "개념 학습 시작하기"}</span>
                                <ArrowRightIcon />
                            </button>
                        </div>
                    </aside>
                </section>
            )}

            <UploadProgressModal
                open={isProgressModalOpen}
                currentStatusText={processingStatus?.stepMessage}
                status={processingStatus}
                errorMessage={uploadErrorMessage}
                onClose={closeProgressModal}
                onRetry={retryProcessing}
                isRetrying={isRetrying}
            />
        </main>
    );
}

function StudyTocTree({ nodes, expandedNodeIds, selectedNodeId, onToggle, onSelect }) {
    return nodes.map((node, index) => (
        <StudyTocTreeNode
            key={node.id}
            node={node}
            numberPrefix={`${index + 1}`}
            level={1}
            expandedNodeIds={expandedNodeIds}
            selectedNodeId={selectedNodeId}
            onToggle={onToggle}
            onSelect={onSelect}
        />
    ));
}

function StudyTocTreeNode({
    node,
    numberPrefix,
    level,
    expandedNodeIds,
    selectedNodeId,
    onToggle,
    onSelect,
}) {
    const hasChildren = Boolean(node.children?.length);
    const isExpanded = expandedNodeIds.includes(node.id);

    return (
        <div className={`toc-node level-${level}`}>
            <button
                type="button"
                className={`toc-node-row ${level === 1 ? "chapter-row" : "topic-row"} ${Number(selectedNodeId) === Number(node.id) ? "selected" : ""}`}
                onClick={() => {
                    if (onSelect) {
                        onSelect(node);
                    } else if (hasChildren) {
                        onToggle(node.id);
                    }
                }}
                style={{ "--toc-depth": Math.min(level - 1, 3) }}
                aria-expanded={hasChildren ? isExpanded : undefined}
            >
                {level === 1 ? (
                    <span className="toc-chapter-number">{numberPrefix.padStart(2, "0")}</span>
                ) : (
                    <span className="toc-dot" />
                )}
                <span className="toc-node-title">
                    {level === 1 ? node.title : `${numberPrefix}. ${node.title}`}
                </span>
                {hasChildren && (
                    <span className={`toc-arrow ${isExpanded ? "open" : ""}`}>
                        <ChevronDownIcon />
                    </span>
                )}
            </button>
            {hasChildren && isExpanded && (
                <div className="toc-children">
                    {node.children.map((child, index) => (
                        <StudyTocTreeNode
                            key={child.id}
                            node={child}
                            numberPrefix={`${numberPrefix}.${index + 1}`}
                            level={level + 1}
                            expandedNodeIds={expandedNodeIds}
                            selectedNodeId={selectedNodeId}
                            onToggle={onToggle}
                            onSelect={onSelect}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}

function PanelLoading({ label }) {
    return <div className="panel-loading"><span className="concept-loading-spinner" />{label}</div>;
}

function formatFileSize(bytes) {
    if (!Number.isFinite(bytes)) return "";
    if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function getFileType(fileName = "") {
    return fileName.toLowerCase().endsWith(".docx") ? "DOCX" : "PDF";
}

function Icon({ children, className, size = 20, viewBox = "0 0 24 24" }) {
    return <svg className={className} width={size} height={size} viewBox={viewBox} fill="none" aria-hidden="true">{children}</svg>;
}

const ChevronDownIcon = (props) => <Icon {...props} size={16}><path d="m6 9 6 6 6-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const UploadCloudIcon = () => <Icon size={30}><path d="M16 16l-4-4-4 4M12 12v9M20.4 17.5A5 5 0 0 0 18 8.2 7 7 0 0 0 4.3 10.5 4.5 4.5 0 0 0 5.5 19H7" stroke="currentColor" strokeWidth="1.65" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const TrashIcon = () => <Icon size={18}><path d="M4 7h16M9 7V4h6v3m3 0-1 13H7L6 7m4 4v5m4-5v5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const FileIcon = () => <Icon size={25}><path d="M6 3h8l4 4v14H6V3Zm8 0v5h4M9 13h6M9 17h4" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const CheckCircleIcon = () => <Icon size={17}><circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.8" /><path d="m8.5 12 2.2 2.2 4.8-5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const ArrowRightIcon = () => <Icon size={18}><path d="M5 12h14m-5-5 5 5-5 5" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const ArrowLeftIcon = () => <Icon size={17}><path d="M19 12H5m5 5-5-5 5-5" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const DocumentIcon = () => <Icon size={86}><path d="M7 2.5h7l5 5v14H7v-19Z" fill="#fff" stroke="#E8B995" strokeWidth="1.3" transform="scale(3.25) translate(-4.7 -1.2)" /><path d="M29 34h27M29 44h27M29 54h18" stroke="#C9A58B" strokeWidth="2.6" strokeLinecap="round" /></Icon>;

export default AiLearningPage;
