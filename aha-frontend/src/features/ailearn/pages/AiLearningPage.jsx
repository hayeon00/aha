import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import ConceptContentView from "../components/ConceptContentView.jsx";
import ConceptEditor from "../components/ConceptEditor.jsx";
import UnmappedTopicEmptyState from "../components/UnmappedTopicEmptyState.jsx";
import NoteHeader from "../components/NoteHeader.jsx";
import UploadProgressModal from "../components/UploadProgressModal.jsx";
import { useDocumentProcessing } from "../hooks/useDocumentProcessing.js";
import { useDocumentConceptDashboard } from "../hooks/useDocumentConceptDashboard.js";
import { deleteLearningDocument } from "../api/learningDocumentApi.js";
import { useUserExams } from "../../exam/hooks/useUserExams.js";
import { useExamScopeNodes } from "../../exam/hooks/useExamScopeNodes.js";
import {
    getLearningNotes,
    removeLearningNoteByDocumentId,
    saveLearningNote,
} from "../utils/learningNoteStorage.js";

import "./AiLearningPage.css";

const ACCEPTED_EXTENSIONS = [".pdf", ".docx"];

function AiLearningPage() {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const fileInputRef = useRef(null);
    const examDropdownRef = useRef(null);
    const wasStudyModeRef = useRef(false);
    const [isDragging, setIsDragging] = useState(false);
    const [isExamDropdownOpen, setIsExamDropdownOpen] = useState(false);
    const [localFiles, setLocalFiles] = useState([]);
    const [hiddenDocumentIds, setHiddenDocumentIds] = useState([]);
    const [fileMessage, setFileMessage] = useState("");
    const [isEditMode, setIsEditMode] = useState(false);
    const [draftNote, setDraftNote] = useState("");
    const [aiPrompt, setAiPrompt] = useState("");
    const [saveFeedback, setSaveFeedback] = useState("");
    const [noteOverrides, setNoteOverrides] = useState(() => Object.fromEntries(
        getLearningNotes().flatMap((note) => {
            if (note.topicContents) return Object.entries(note.topicContents);
            return note.tocId ? [[note.tocId, note.content]] : [];
        }),
    ));
    const [noteTitlesByDocument, setNoteTitlesByDocument] = useState(() => Object.fromEntries(
        getLearningNotes().map((note) => [note.documentId, note.title]),
    ));

    const {
        userExams,
        selectedUserExamId,
        selectedUserExam,
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
        selectNodeById,
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

    useEffect(() => {
        if (!isExamDropdownOpen) return undefined;

        const closeOnOutsideClick = (event) => {
            if (!examDropdownRef.current?.contains(event.target)) {
                setIsExamDropdownOpen(false);
            }
        };
        const closeOnEscape = (event) => {
            if (event.key === "Escape") setIsExamDropdownOpen(false);
        };

        document.addEventListener("mousedown", closeOnOutsideClick);
        document.addEventListener("keydown", closeOnEscape);
        return () => {
            document.removeEventListener("mousedown", closeOnOutsideClick);
            document.removeEventListener("keydown", closeOnEscape);
        };
    }, [isExamDropdownOpen]);

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
    const canStartLearning = completedDocuments.length > 0
        && !isUploading
        && !documentRoom.loading
        && !documentRoom.batchGenerating;
    const isStudyMode = searchParams.get("view") === "notes";
    const dashboardChapters = useMemo(() => [
        ...(documentRoom.dashboard?.mapped ?? []).map((chapter) => ({ ...chapter, mapped: true })),
        ...(documentRoom.dashboard?.unmapped ?? []).map((chapter) => ({ ...chapter, mapped: false })),
    ], [documentRoom.dashboard]);
    const selectedChapter = dashboardChapters.find(
        (chapter) => Number(chapter.tocId) === Number(selectedNodeId),
    ) ?? null;
    const mappedChapters = dashboardChapters.filter((chapter) => chapter.mapped);
    const mappedTocIds = new Set(mappedChapters.map((chapter) => Number(chapter.tocId)));
    const knownTocIds = new Set(dashboardChapters.map((chapter) => Number(chapter.tocId)));
    const selectedChapterWithOverride = selectedChapter
        ? {
            ...selectedChapter,
            content: noteOverrides[selectedChapter.tocId] ?? selectedChapter.content,
        }
        : null;
    const noteTitle = noteTitlesByDocument[documentRoom.documentId]
        ?? `${selectedUserExam?.examCode ?? "나의"} 개념 학습`;

    useEffect(() => {
        const wasStudyMode = wasStudyModeRef.current;
        wasStudyModeRef.current = isStudyMode;

        if (wasStudyMode && !isStudyMode) {
            setLocalFiles([]);
            setHiddenDocumentIds([]);
            setFileMessage("");
            documentRoom.refresh();
        }
    }, [documentRoom, isStudyMode]);

    useEffect(() => {
        const requestedDocumentId = Number(searchParams.get("documentId"));
        if (
            isStudyMode
            && requestedDocumentId
            && documentRoom.documentId !== requestedDocumentId
            && documentRoom.documents.some((document) => document.documentId === requestedDocumentId)
        ) {
            documentRoom.selectDocument(requestedDocumentId);
        }
    }, [documentRoom, isStudyMode, searchParams]);

    useEffect(() => {
        const requestedTocId = Number(searchParams.get("tocId"));
        if (
            isStudyMode
            && requestedTocId
            && selectedNodeId !== requestedTocId
            && dashboardChapters.some((chapter) => Number(chapter.tocId) === requestedTocId)
        ) {
            selectNodeById(requestedTocId);
        }
    }, [dashboardChapters, isStudyMode, searchParams, selectNodeById, selectedNodeId]);

    const message = examMessage || scopeMessage || fileMessage;

    const handleUserExamChange = (userExamId) => {
        setIsExamDropdownOpen(false);
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
        const files = Array.from(event.target.files || []);
        event.target.value = "";
        await handleFiles(files);
    };

    const handleDrop = async (event) => {
        event.preventDefault();
        setIsDragging(false);
        await handleFiles(event.dataTransfer.files);
    };

    const handleRemoveDocument = async (document) => {
        if (document.local) {
            setLocalFiles((current) => current.filter((file) => file.id !== document.id));
            return;
        }

        try {
            setFileMessage("");
            await deleteLearningDocument(document.documentId);
            removeLearningNoteByDocumentId(document.documentId);
            setHiddenDocumentIds((current) => [...current, document.documentId]);
            await documentRoom.refresh();
        } catch (error) {
            setFileMessage(
                error.response?.data?.message || "문서를 삭제하지 못했습니다.",
            );
        }
    };

    const handleStartLearning = async () => {
        if (!canStartLearning) return;
        const firstDocumentId = completedDocuments[0].documentId;
        if (firstDocumentId && documentRoom.documentId !== firstDocumentId) {
            documentRoom.selectDocument(firstDocumentId);
        }
        const hasCompleteNote = documentRoom.documentId === firstDocumentId
            && mappedChapters.length > 0
            && mappedChapters.every((chapter) => chapter.content);
        if (!hasCompleteNote) {
            const created = await documentRoom.generateAll(firstDocumentId);
            if (!created) return;
        }
        setSearchParams({ view: "notes", documentId: String(firstDocumentId) });
    };

    const handleChapterSelect = (chapter) => {
        selectNodeById(chapter.tocId);
        setIsEditMode(false);
        setAiPrompt("");
        setSaveFeedback("");
        setDraftNote(noteOverrides[chapter.tocId] ?? chapter.content ?? "");
    };

    const handleEditToggle = () => {
        if (!selectedChapterWithOverride) return;
        setDraftNote(selectedChapterWithOverride.content ?? "");
        setIsEditMode((current) => !current);
    };

    const handleEditComplete = async () => {
        if (!selectedChapterWithOverride) return;
        if (!selectedChapterWithOverride.mapped) {
            const saved = await documentRoom.saveOne(
                selectedChapterWithOverride.tocId,
                draftNote,
            );
            if (!saved) return;
        }
        setNoteOverrides((current) => ({
            ...current,
            [selectedChapterWithOverride.tocId]: draftNote,
        }));
        saveLearningNote({
            documentId: documentRoom.documentId,
            tocId: selectedChapterWithOverride.tocId,
            title: noteTitle,
            content: draftNote,
        });
        setIsEditMode(false);
    };

    const handleTitleSave = async (title) => {
        if (!selectedChapterWithOverride) return;
        setNoteTitlesByDocument((current) => ({
            ...current,
            [documentRoom.documentId]: title,
        }));
        saveLearningNote({
            documentId: documentRoom.documentId,
            tocId: selectedChapterWithOverride.tocId,
            title,
            content: selectedChapterWithOverride.content ?? "",
        });
    };

    const handleAiGenerate = async (prompt = aiPrompt) => {
        if (!selectedChapterWithOverride || selectedChapterWithOverride.mapped || !prompt.trim()) return;
        setSaveFeedback("");
        const generated = await documentRoom.generateOne(
            selectedChapterWithOverride.tocId,
            prompt.trim(),
        );
        if (generated?.content) {
            setNoteOverrides((current) => ({
                ...current,
                [selectedChapterWithOverride.tocId]: generated.content,
            }));
            setDraftNote(generated.content);
            setIsEditMode(true);
        }
    };

    const handleEmptySummarySave = (content) => {
        if (!selectedChapterWithOverride) return;
        setNoteOverrides((current) => ({
            ...current,
            [selectedChapterWithOverride.tocId]: content,
        }));
        saveLearningNote({
            documentId: documentRoom.documentId,
            tocId: selectedChapterWithOverride.tocId,
            title: noteTitle,
            content,
        });
    };

    const handleSaveConcept = async () => {
        if (!selectedChapterWithOverride) return;
        const content = isEditMode
            ? draftNote
            : selectedChapterWithOverride.content ?? "";
        const saved = await documentRoom.saveOne(
            selectedChapterWithOverride.tocId,
            content,
        );
        if (!saved) return;
        setNoteOverrides((current) => ({
            ...current,
            [selectedChapterWithOverride.tocId]: content,
        }));
        saveLearningNote({
            documentId: documentRoom.documentId,
            tocId: selectedChapterWithOverride.tocId,
            title: noteTitle,
            content,
        });
        setIsEditMode(false);
        setSaveFeedback("저장됨");
    };

    const handleStudyTreeSelect = (node) => {
        if (node.children?.length) {
            toggleNode(node.id);
            return;
        }

        const chapter = dashboardChapters.find(
            (item) => Number(item.tocId) === Number(node.id),
        ) ?? {
            tocId: node.id,
            tocTitle: node.title,
            content: "",
            mapped: false,
        };
        handleChapterSelect(chapter);
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
                    <div className="exam-dropdown-wrap" ref={examDropdownRef}>
                        <button
                            type="button"
                            className={`exam-select-trigger ${isExamDropdownOpen ? "is-open" : ""}`}
                            onClick={() => setIsExamDropdownOpen((current) => !current)}
                            disabled={userExams.length === 0}
                            aria-expanded={isExamDropdownOpen}
                            aria-haspopup="listbox"
                            aria-label="학습 시험 선택"
                        >
                            <span>{selectedUserExam?.examCode ?? "활성 시험 없음"}</span>
                            <ChevronDownIcon className="exam-select-arrow" />
                        </button>
                        {isExamDropdownOpen && (
                            <div className="exam-select-menu" role="listbox" aria-label="학습 시험 목록">
                                {userExams.map((userExam) => {
                                    const isSelected = userExam.userExamId === selectedUserExamId;
                                    return (
                                        <button
                                            type="button"
                                            role="option"
                                            aria-selected={isSelected}
                                            className={isSelected ? "is-selected" : ""}
                                            key={userExam.userExamId}
                                            onClick={() => handleUserExamChange(userExam.userExamId)}
                                        >
                                            <span>{userExam.examCode}</span>
                                            {isSelected && <span className="exam-option-check">✓</span>}
                                        </button>
                                    );
                                })}
                            </div>
                        )}
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
                            <h2>학습 목차</h2>
                        </div>
                        <div className="toc-list study-tree-list">
                            <StudyTocTree
                                nodes={scopeNodes}
                                expandedNodeIds={expandedNodeIds}
                                selectedNodeId={selectedNodeId}
                                mappedTocIds={mappedTocIds}
                                knownTocIds={knownTocIds}
                                onToggle={toggleNode}
                                onSelect={handleStudyTreeSelect}
                            />
                        </div>
                    </aside>

                    <section className="study-notes-content">
                        <NoteHeader
                            title={noteTitle}
                            onTitleSave={handleTitleSave}
                            documents={completedDocuments}
                            selectedDocumentId={documentRoom.documentId}
                            onDocumentSelect={documentRoom.selectDocument}
                            editing={isEditMode}
                            onEdit={isEditMode ? handleEditComplete : handleEditToggle}
                        />
                        {selectedChapterWithOverride
                            && !selectedChapterWithOverride.mapped
                            && selectedChapterWithOverride.content && (
                            <div className="ai-result-header">
                                <span className="external-knowledge-badge">
                                    💡 표준 개념 지식 기반 생성됨 (업로드 문서 내용 없음)
                                </span>
                                {!isEditMode && (
                                    <div className="ai-result-actions">
                                        <span>{saveFeedback}</span>
                                        <button
                                            type="button"
                                            onClick={() => handleAiGenerate(
                                                aiPrompt || "핵심 개념을 다른 구성과 예시로 다시 설명해줘",
                                            )}
                                            disabled={documentRoom.generatingIds.includes(selectedChapterWithOverride.tocId)}
                                        >
                                            다시 생성 🔄
                                        </button>
                                        <button type="button" onClick={handleEditToggle}>편집 ✏️</button>
                                        <button
                                            type="button"
                                            className="save"
                                            onClick={handleSaveConcept}
                                            disabled={documentRoom.savingIds.includes(selectedChapterWithOverride.tocId)}
                                        >
                                            저장 💾
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}
                        {selectedChapterWithOverride
                            && !selectedChapterWithOverride.mapped
                            && !selectedChapterWithOverride.content
                            && !isEditMode ? (
                            <UnmappedTopicEmptyState
                                topicTitle={selectedChapterWithOverride.tocTitle}
                                onAddFiles={handleFiles}
                                onSaveSummary={handleEmptySummarySave}
                            />
                        ) : isEditMode ? (
                            <ConceptEditor
                                value={draftNote}
                                onChange={setDraftNote}
                                onRegenerate={() => handleAiGenerate(
                                    aiPrompt || "핵심 개념을 다른 구성과 예시로 다시 설명해줘",
                                )}
                                onCancel={handleEditToggle}
                                onSave={selectedChapterWithOverride?.mapped
                                    ? handleEditComplete
                                    : handleSaveConcept}
                                generating={documentRoom.generatingIds.includes(selectedChapterWithOverride?.tocId)}
                                saving={documentRoom.savingIds.includes(selectedChapterWithOverride?.tocId)}
                                isExternalKnowledge={!selectedChapterWithOverride?.mapped}
                            />
                        ) : (
                            <ConceptContentView
                                chapter={selectedChapterWithOverride}
                                loading={documentRoom.loading}
                                generating={documentRoom.generatingIds.includes(selectedChapter?.tocId)}
                            />
                        )}
                    </section>
                </section>
            ) : (
                <section className="concept-workspace">
                    <aside className="workspace-card toc-panel">
                        <header className="panel-section-header">
                            <h2>시험 목차</h2>
                        </header>
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
                        <header className="panel-section-header">
                            <h2>학습 자료 업로드</h2>
                        </header>
                        <label
                            htmlFor="learning-document-upload"
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
                            tabIndex={0}
                            onKeyDown={(event) => {
                                if (!isUploading && (event.key === "Enter" || event.key === " ")) {
                                    event.preventDefault();
                                    fileInputRef.current?.click();
                                }
                            }}
                            aria-label="학습 문서 업로드"
                            aria-disabled={isUploading}
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
                        </label>

                        <input
                            id="learning-document-upload"
                            ref={fileInputRef}
                            type="file"
                            accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            multiple
                            className="upload-file-input"
                            onChange={handleFileChange}
                            disabled={isUploading}
                        />
                    </section>

                    <aside className="workspace-card documents-panel">
                        <header className="panel-section-header documents-section-header">
                            <h2>MY DOCUMENTS</h2>
                            <strong>{uploadedDocuments.length}개</strong>
                        </header>

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
                                        <div className="document-title-row">
                                            <strong title={document.name}>{document.name}</strong>
                                            {document.local && (
                                                <i
                                                    className="document-loading-indicator"
                                                    role="status"
                                                    aria-label={`${document.name} 처리 중`}
                                                />
                                            )}
                                        </div>
                                        <span>
                                            {document.meta}
                                            {document.status}
                                        </span>
                                    </div>
                                    <button
                                        type="button"
                                        className="trash-button"
                                        onClick={() => handleRemoveDocument(document)}
                                        aria-label={`${document.name} 삭제`}
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
                                <span>
                                    {isUploading
                                        ? "문서 분석 중..."
                                        : documentRoom.batchGenerating
                                            ? "학습노트 만드는 중..."
                                            : "학습노트 만들기"}
                                </span>
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

function StudyTocTree({
    nodes,
    expandedNodeIds,
    selectedNodeId,
    mappedTocIds,
    knownTocIds,
    onToggle,
    onSelect,
}) {
    return nodes.map((node, index) => (
        <StudyTocTreeNode
            key={node.id}
            node={node}
            numberPrefix={`${index + 1}`}
            level={1}
            expandedNodeIds={expandedNodeIds}
            selectedNodeId={selectedNodeId}
            mappedTocIds={mappedTocIds}
            knownTocIds={knownTocIds}
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
    mappedTocIds,
    knownTocIds,
    onToggle,
    onSelect,
}) {
    const hasChildren = Boolean(node.children?.length);
    const isExpanded = expandedNodeIds.includes(node.id);
    const isMapped = mappedTocIds?.has(Number(node.id));
    const isKnownUnmapped = knownTocIds?.has(Number(node.id)) && !isMapped;

    return (
        <div className={`toc-node level-${level}`}>
            <button
                type="button"
                className={[
                    "toc-node-row",
                    level === 1 ? "chapter-row" : "topic-row",
                    Number(selectedNodeId) === Number(node.id) ? "selected" : "",
                    isMapped ? "mapped" : "",
                    isKnownUnmapped ? "unmapped" : "",
                ].join(" ")}
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
                            mappedTocIds={mappedTocIds}
                            knownTocIds={knownTocIds}
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
const ArrowRightIcon = () => <Icon size={18}><path d="M5 12h14m-5-5 5 5-5 5" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const ArrowLeftIcon = () => <Icon size={17}><path d="M19 12H5m5 5-5-5 5-5" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const DocumentIcon = () => <Icon size={86}><path d="M7 2.5h7l5 5v14H7v-19Z" fill="#fff" stroke="#E8B995" strokeWidth="1.3" transform="scale(3.25) translate(-4.7 -1.2)" /><path d="M29 34h27M29 44h27M29 54h18" stroke="#C9A58B" strokeWidth="2.6" strokeLinecap="round" /></Icon>;

export default AiLearningPage;
