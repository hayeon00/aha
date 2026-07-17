import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";

import UploadProgressModal from "../components/UploadProgressModal.jsx";
import UnassignedChunksPanel from "../components/UnassignedChunksPanel.jsx";
import LearningContentMarkdown, { LearningContentSkeleton } from "../components/LearningContentMarkdown.jsx";
import ConceptContentView from "../components/ConceptContentView.jsx";
import AICoachPanel from "../components/AICoachPanel.jsx";
import { useDocumentProcessing } from "../hooks/useDocumentProcessing.js";
import { useLearningContent } from "../hooks/useLearningContent.js";
import { useDocumentConceptDashboard } from "../hooks/useDocumentConceptDashboard.js";
import { useUserExams } from "../../exam/hooks/useUserExams.js";
import { useExamScopeNodes } from "../../exam/hooks/useExamScopeNodes.js";

import "./AiLearningPage.css";

function AiLearningPage() {
    const navigate = useNavigate();
    const fileInputRef = useRef(null);

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
        handleSelectNode,
        resetScopeNodes,
    } = useExamScopeNodes({
        examVersionId: selectedExamVersionId,
    });

    const {
        isUploading,
        processingStatus,
        isProgressModalOpen,
        uploadErrorMessage,
        hasProcessedDocuments,
        isDocumentStateLoading,
        completedProcessingKey,
        isRetrying,
        uploadDocuments,
        retryProcessing,
        closeProgressModal,
        resetDocumentState,
    } = useDocumentProcessing({
        selectedUserExamId,
        selectedNodeId,
    });

    const {
        refetchLearningContent,
        resetLearningContent,
    } = useLearningContent({
        userExamId: selectedUserExamId,
        examScopeNodeId: selectedNodeId,
        enabled: hasProcessedDocuments && !isDocumentStateLoading,
    });

    const conceptRoom = useDocumentConceptDashboard({
        userExamId: selectedUserExamId,
        enabled: hasProcessedDocuments && !isDocumentStateLoading,
    });

    useEffect(() => {
        if (completedProcessingKey > 0) {
            refetchLearningContent();
        }
    }, [completedProcessingKey, refetchLearningContent]);

    const message = examMessage || scopeMessage;
    const selectedChapter = [
        ...(conceptRoom.dashboard?.mapped ?? []).map((chapter) => ({ ...chapter, mapped: true })),
        ...(conceptRoom.dashboard?.unmapped ?? []).map((chapter) => ({ ...chapter, mapped: false })),
    ].find((chapter) => Number(chapter.tocId) === Number(selectedNodeId)) ?? null;
    const isSelectedChapterGenerating = conceptRoom.generatingIds.includes(selectedChapter?.tocId);
    const mappedTocIds = new Set(
        (conceptRoom.dashboard?.mapped ?? []).map((chapter) => Number(chapter.tocId))
    );
    const knownTocIds = new Set([
        ...(conceptRoom.dashboard?.mapped ?? []),
        ...(conceptRoom.dashboard?.unmapped ?? []),
    ].map((chapter) => Number(chapter.tocId)));
    const aiCompletedTocIds = new Set(
        (conceptRoom.dashboard?.unmapped ?? [])
            .filter((chapter) => chapter.generated || Boolean(chapter.content))
            .map((chapter) => Number(chapter.tocId))
    );

    const handleUserExamChange = (userExamId) => {
        changeUserExam(userExamId);
        resetScopeNodes();
        resetLearningContent();
        resetDocumentState();
    };

    const handleUploadClick = () => {
        fileInputRef.current?.click();
    };

    const handleFileChange = async (event) => {
        const files = Array.from(event.target.files || []);
        event.target.value = "";

        await uploadDocuments(files);
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
                                <StudyTocTree
                                    nodes={scopeNodes}
                                    selectedNodeId={selectedNodeId}
                                    expandedNodeIds={expandedNodeIds}
                                    mappedTocIds={mappedTocIds}
                                    aiCompletedTocIds={aiCompletedTocIds}
                                    knownTocIds={knownTocIds}
                                    onToggle={toggleNode}
                                    onSelect={handleSelectNode}
                                />
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
                        ) : hasProcessedDocuments ? (
                            <ConceptContentView
                                chapter={selectedChapter}
                                loading={conceptRoom.loading}
                                generating={isSelectedChapterGenerating}
                            />
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
                                        PDF, Docx 파일을 선택해 개념 학습을 시작하세요.
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
                                        <span>문서 업로드</span>
                                    </button>

                                    <span className="upload-hint">
                                        PDF, DOCX 파일을 여러 개 선택할 수 있습니다.
                                    </span>
                                </div>
                            </div>
                        )}

                        <input
                            ref={fileInputRef}
                            type="file"
                            accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            multiple
                            className="upload-file-input"
                            onChange={handleFileChange}
                        />
                    </section>

                    <aside className="coach-panel">
                        <div className="panel-header">
                            <div className="panel-title"><h2>AI 코치</h2></div>
                        </div>
                        <AICoachPanel
                            chapter={selectedChapter}
                            generating={isSelectedChapterGenerating}
                            onGenerate={() => selectedChapter && conceptRoom.generateOne(selectedChapter.tocId)}
                        />
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
            {hasProcessedDocuments && (
                <UnassignedChunksPanel
                    userExamId={selectedUserExamId}
                    scopeNodes={scopeNodes}
                    onAssigned={refetchLearningContent}
                />
            )}
        </main>
    );
}

export function DocumentMappedContentSection({
                                          learningContent,
                                          chunks,
                                          contentErrorMessage,
                                          isContentLoading,
                                          isGeneratingConcept,
                                          isUploading,
                                          selectedNodeId,
                                          selectedUserExamName,
                                          onUploadClick,
                                          onRequestAiConcept,
                                      }) {
    const sortedChunks = [...(chunks || [])].sort((a, b) => {
        const firstOrder = a.chunkOrder ?? Number.MAX_SAFE_INTEGER;
        const secondOrder = b.chunkOrder ?? Number.MAX_SAFE_INTEGER;

        if (firstOrder !== secondOrder) {
            return firstOrder - secondOrder;
        }

        return (a.documentChunkId ?? 0) - (b.documentChunkId ?? 0);
    });

    if (!selectedNodeId) {
        return (
            <DocumentContentMessage
                title="목차를 선택해 주세요"
                message="목차를 선택하면 문서 기반 개념 내용을 확인할 수 있습니다."
                onUploadClick={onUploadClick}
                isUploading={isUploading}
            />
        );
    }

    if (isContentLoading) {
        return (
            <div className="learning-content-view">
                <LearningContentSkeleton />
            </div>
        );
    }

    if (isGeneratingConcept) {
        return (
            <div className="ai-generation-loading" aria-live="polite">
                <div className="ai-generation-loading-head">
                    <span className="ai-sparkle">✦</span>
                    <div>
                        <strong>AI가 핵심 개념을 정리하고 있어요</strong>
                        <p>목차와 상위 맥락을 분석해 표준 설명을 생성합니다.</p>
                    </div>
                </div>
                <LearningContentSkeleton />
            </div>
        );
    }

    if (contentErrorMessage) {
        return (
            <DocumentContentMessage
                title="문서 내용을 불러오지 못했습니다"
                message="문서 기반 개념 내용을 불러오지 못했습니다."
                variant="error"
                onUploadClick={onUploadClick}
                isUploading={isUploading}
            />
        );
    }

    if (learningContent) {
        return (
            <GeneratedLearningContentSection
                learningContent={learningContent}
                selectedUserExamName={selectedUserExamName}
                isUploading={isUploading}
                onUploadClick={onUploadClick}
            />
        );
    }

    if (sortedChunks.length === 0) {
        return (
            <MissingConceptPrompt
                onRequest={onRequestAiConcept}
                disabled={isGeneratingConcept}
            />
        );
    }

    return (
        <article className="learning-content-view document-content-view">
            <div className="learning-content-head document-content-head">
                <div>
                    <p className="learning-content-path">
                        {selectedUserExamName || "개념 학습"}
                    </p>
                    <h3>문서 기반 개념 내용</h3>
                    <p className="document-content-description">
                        업로드한 문서에서 선택한 목차와 관련된 내용을 모아 보여줍니다.
                    </p>
                </div>

                <div className="document-content-actions">
                    <span className="document-count-badge">
                        총 {sortedChunks.length}개 문서 조각
                    </span>
                    <button
                        type="button"
                        className="secondary-upload-button"
                        onClick={onUploadClick}
                        disabled={isUploading}
                    >
                        문서 추가 업로드
                    </button>
                </div>
            </div>

            <div className="document-chunk-list">
                {sortedChunks.map((chunk) => (
                    <DocumentChunkCard
                        key={chunk.documentChunkId ?? `${chunk.chunkOrder}-${chunk.contentText}`}
                        chunk={chunk}
                    />
                ))}
            </div>
        </article>
    );
}

function GeneratedLearningContentSection({
                                             learningContent,
                                             selectedUserExamName,
                                             isUploading,
                                             onUploadClick,
                                         }) {
    const keywords = parseKeywords(learningContent.keywordsJson);
    const isAiGenerated = learningContent.sourceType === "AI_GENERATED";

    return (
        <article className={`learning-content-view ${isAiGenerated ? "ai-generated-content" : "document-generated-content"}`}>
            <div className="learning-content-head">
                <div>
                    <p className="learning-content-path">
                        {selectedUserExamName || "개념 학습"}
                    </p>
                    {isAiGenerated && (
                        <span className="ai-source-badge">
                            <span aria-hidden="true">✦</span>
                            AI 자체 생성 지식
                        </span>
                    )}
                    <h3>{learningContent.title}</h3>
                    {isAiGenerated && (
                        <p className="ai-source-notice">
                            업로드 문서가 아닌 AI의 일반 지식을 바탕으로 생성된 설명입니다.
                        </p>
                    )}
                </div>

                <button
                    type="button"
                    className="secondary-upload-button"
                    onClick={onUploadClick}
                    disabled={isUploading}
                >
                    문서 추가 업로드
                </button>
            </div>

            <div className="learning-content-markdown">
                <LearningContentMarkdown content={learningContent.content} />
            </div>

            {keywords.length > 0 && (
                <div className="learning-keywords">
                    <strong>핵심 키워드</strong>
                    <div>
                        {keywords.map((keyword) => (
                            <span key={keyword}>{keyword}</span>
                        ))}
                    </div>
                </div>
            )}
        </article>
    );
}

function MissingConceptPrompt({ onRequest, disabled }) {
    return (
        <div className="missing-concept-prompt">
            <div className="missing-concept-robot" aria-hidden="true">🤖</div>
            <span className="missing-concept-kicker">빈 목차</span>
            <h3>아직 설명할 문서 내용이 없어요</h3>
            <p>업로드한 문서에 없는 내용입니다. AI에게 핵심 개념 설명을 요청해보세요!</p>
            <button
                type="button"
                className="ai-concept-request-button"
                onClick={onRequest}
                disabled={disabled}
            >
                <span aria-hidden="true">🤖</span>
                AI에게 설명 요청하기
                <span className="button-sparkle" aria-hidden="true">✦</span>
            </button>
            <small>생성된 설명은 AI 지식으로 명확히 표시되며 이후 요청에 재사용됩니다.</small>
        </div>
    );
}

function DocumentContentMessage({
                                    title,
                                    message,
                                    variant = "empty",
                                    onUploadClick,
                                    isUploading,
                                }) {
    return (
        <div className={`content-empty-state document-content-message ${variant}`}>
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
                        d="M18 20H34M18 27H30M18 34H32"
                        stroke="#C9A995"
                        strokeWidth="2"
                        strokeLinecap="round"
                    />
                </svg>
            </div>

            <h3>{title}</h3>
            <p>{message}</p>

            <button
                type="button"
                className="empty-upload-button"
                onClick={onUploadClick}
                disabled={isUploading}
            >
                문서 추가 업로드
            </button>
        </div>
    );
}

function DocumentChunkCard({ chunk }) {
    const contentType = normalizeContentType(chunk.contentType);
    const sectionTitle = chunk.sectionTitle || getContentTypeLabel(contentType);
    const confidence = Number(chunk.mappingConfidence);
    const confidenceLevel = confidence >= 0.8 ? "high" : "mid";

    return (
        <section className={`document-chunk-card ${getContentTypeClassName(contentType)}`}>
            <div className="document-chunk-meta">
                <div className="document-chunk-title-wrap">
                    <h4>{sectionTitle}</h4>
                    {Number.isFinite(confidence) && (
                        <span className={`mapping-confidence-badge ${confidenceLevel}`}>
                            {confidenceLevel === "high" ? "높은 신뢰도" : "검토 권장"}
                        </span>
                    )}
                    {chunk.headingPath && (
                        <p>{chunk.headingPath}</p>
                    )}
                </div>

                <div className="document-chunk-badges">
                    {chunk.pageNo && (
                        <span className="document-page-badge">p.{chunk.pageNo}</span>
                    )}
                    <span className={`document-type-badge ${getContentTypeClassName(contentType)}`}>
                        {getContentTypeLabel(contentType)}
                    </span>
                    {chunk.codeLanguage && chunk.codeLanguage !== "UNKNOWN" && (
                        <span className="document-language-badge">
                            {chunk.codeLanguage}
                        </span>
                    )}
                </div>
            </div>

            {renderChunkContent(chunk)}
        </section>
    );
}

function renderChunkContent(chunk) {
    const contentType = normalizeContentType(chunk.contentType);
    const contentText = chunk.contentText || "";

    if (contentType === "CODE" || contentType === "COMMAND" || contentType === "CONFIG") {
        return (
            <pre className="document-code-block">
                <code>{contentText}</code>
            </pre>
        );
    }

    if (contentType === "TABLE") {
        return (
            <pre className="document-table-block">
                {contentText}
            </pre>
        );
    }

    if (contentType === "HEADING") {
        return (
            <div className="document-heading-block">
                {contentText}
            </div>
        );
    }

    if (contentType === "EXAMPLE" || contentType === "WARNING") {
        return (
            <div className={`document-callout-block ${getContentTypeClassName(contentType)}`}>
                {contentText}
            </div>
        );
    }

    return (
        <div className="document-text-block">
            {contentText}
        </div>
    );
}

function normalizeContentType(contentType) {
    return String(contentType || "TEXT").toUpperCase();
}

function getContentTypeLabel(contentType) {
    const labels = {
        TEXT: "TEXT",
        HEADING: "HEADING",
        CODE: "CODE",
        TABLE: "TABLE",
        EXAMPLE: "EXAMPLE",
        WARNING: "WARNING",
        COMMAND: "COMMAND",
        CONFIG: "CONFIG",
        FORMULA: "FORMULA",
        MIXED: "MIXED",
    };

    return labels[normalizeContentType(contentType)] || normalizeContentType(contentType);
}

function getContentTypeClassName(contentType) {
    return `type-${normalizeContentType(contentType).toLowerCase()}`;
}

function parseKeywords(keywordsJson) {
    if (!keywordsJson) {
        return [];
    }

    if (Array.isArray(keywordsJson)) {
        return keywordsJson;
    }

    try {
        const parsedKeywords = JSON.parse(keywordsJson);
        return Array.isArray(parsedKeywords) ? parsedKeywords : [];
    } catch {
        return [];
    }
}

function StudyTocTree({
                          nodes,
                          selectedNodeId,
                          expandedNodeIds,
                          mappedTocIds,
                          aiCompletedTocIds,
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
            selectedNodeId={selectedNodeId}
            expandedNodeIds={expandedNodeIds}
            mappedTocIds={mappedTocIds}
            aiCompletedTocIds={aiCompletedTocIds}
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
                           selectedNodeId,
                           expandedNodeIds,
                           mappedTocIds,
                           aiCompletedTocIds,
                           knownTocIds,
                           onToggle,
                           onSelect,
                       }) {
    const hasChildren = node.children && node.children.length > 0;
    const isExpanded = expandedNodeIds.includes(node.id);
    const isSelected = selectedNodeId === node.id;
    const mappingState = mappedTocIds.has(Number(node.id))
        ? "mapped"
        : aiCompletedTocIds.has(Number(node.id))
            ? "ai-completed"
            : knownTocIds.has(Number(node.id)) ? "unmapped" : "neutral";

    return (
        <div className={`toc-node toc-node-level-${level}`}>
            <button
                type="button"
                className={`toc-node-row ${mappingState} ${isSelected ? "active" : ""}`}
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
                        <StudyTocTreeNode
                            key={child.id}
                            node={child}
                            numberPrefix={`${numberPrefix}.${index + 1}`}
                            level={level + 1}
                            selectedNodeId={selectedNodeId}
                            expandedNodeIds={expandedNodeIds}
                            mappedTocIds={mappedTocIds}
                            aiCompletedTocIds={aiCompletedTocIds}
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

export default AiLearningPage;
