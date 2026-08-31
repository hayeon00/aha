import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import ConceptContentView from "../components/ConceptContentView.jsx";
import { useLearningNoteDetail } from "../hooks/useLearningNoteDetail.js";
import {
    deleteLearningNote,
    generateLearningNoteTopic,
    updateLearningNoteTitle,
} from "../api/learningNoteApi.js";

import "./LearningNoteDetailPage.css";

export default function LearningNoteDetailPage({ learningNoteId }) {
    const navigate = useNavigate();
    const { detail, loading, error, refresh } = useLearningNoteDetail(learningNoteId);
    const [selectedTocId, setSelectedTocId] = useState(null);
    const [tocFilter, setTocFilter] = useState("all");
    const [openSubjectOverride, setOpenSubjectOverride] = useState(undefined);
    const [generatingTocId, setGeneratingTocId] = useState(null);
    const [generationError, setGenerationError] = useState("");
    const [deleting, setDeleting] = useState(false);
    const [deleteError, setDeleteError] = useState("");
    const [editingTitle, setEditingTitle] = useState(false);
    const [titleDraft, setTitleDraft] = useState("");
    const [savingTitle, setSavingTitle] = useState(false);
    const [titleError, setTitleError] = useState("");

    useEffect(() => {
        setTitleDraft(detail?.title ?? "");
    }, [detail?.title]);

    const allContents = useMemo(() => detail?.contents ?? [], [detail?.contents]);
    const leafContents = useMemo(
        () => allContents.filter((content) => content.leaf !== false),
        [allContents],
    );
    const writtenContents = useMemo(
        () => leafContents.filter((content) => Boolean(content.content)),
        [leafContents],
    );
    const visibleContents = useMemo(
        () => {
            if (tocFilter === "all") return allContents;

            const contentById = new Map(allContents.map((content) => [content.tocId, content]));
            const visibleIds = new Set();
            writtenContents.forEach((content) => {
                let current = content;
                while (current && !visibleIds.has(current.tocId)) {
                    visibleIds.add(current.tocId);
                    current = contentById.get(current.parentTocId);
                }
            });
            return allContents.filter((content) => visibleIds.has(content.tocId));
        },
        [allContents, tocFilter, writtenContents],
    );

    const visibleLeafContents = useMemo(
        () => visibleContents.filter((content) => content.leaf !== false),
        [visibleContents],
    );

    const effectiveSelectedTocId = visibleLeafContents.some(
        (content) => content.tocId === selectedTocId,
    ) ? selectedTocId : visibleLeafContents[0]?.tocId ?? null;

    const selectedContent = useMemo(() => (
        detail?.contents?.find((content) => content.tocId === effectiveSelectedTocId) ?? null
    ), [detail, effectiveSelectedTocId]);

    const leafOrderById = useMemo(
        () => new Map(leafContents.map((content, index) => [content.tocId, index + 1])),
        [leafContents],
    );

    const tocGroups = useMemo(() => {
        const groups = new Map();
        visibleContents.forEach((content) => {
            const key = content.examPartId ?? `exam-part-${content.examPartName || "기타"}`;
            if (!groups.has(key)) {
                groups.set(key, {
                    key,
                    title: content.examPartName || "기타",
                    displayOrder: content.examPartDisplayOrder ?? Number.MAX_SAFE_INTEGER,
                    contents: [],
                });
            }
            groups.get(key).contents.push(content);
        });
        return Array.from(groups.values()).sort((a, b) => a.displayOrder - b.displayOrder);
    }, [visibleContents]);

    const activeGroup = useMemo(
        () => tocGroups.find((group) => (
            group.contents.some((content) => content.tocId === effectiveSelectedTocId)
        )),
        [effectiveSelectedTocId, tocGroups],
    );
    const openSubjectKey = openSubjectOverride === undefined
        ? activeGroup?.key
        : openSubjectOverride;

    const toggleSubject = (subjectKey) => {
        setOpenSubjectOverride((current) => {
            const currentOpenKey = current === undefined ? activeGroup?.key : current;
            return currentOpenKey === subjectKey ? null : subjectKey;
        });
    };

    const handleGenerateTopic = async () => {
        if (!selectedContent?.tocId || generatingTocId) return;
        try {
            setGeneratingTocId(selectedContent.tocId);
            setGenerationError("");
            await generateLearningNoteTopic(learningNoteId, selectedContent.tocId);
            await refresh({ silent: true });
        } catch (requestError) {
            setGenerationError(
                requestError.response?.data?.message
                || "이 단원 노트를 생성하지 못했습니다. 추가 교안을 업로드해 주세요.",
            );
        } finally {
            setGeneratingTocId(null);
        }
    };

    const handleDelete = async () => {
        if (deleting || !window.confirm(`'${detail.title}'을 삭제할까요?\n삭제된 노트는 복구할 수 없습니다.`)) return;
        try {
            setDeleting(true);
            setDeleteError("");
            await deleteLearningNote(learningNoteId);
            navigate("/learning-home", { replace: true });
        } catch (requestError) {
            setDeleteError(requestError.response?.data?.message || "학습노트를 삭제하지 못했습니다.");
            setDeleting(false);
        }
    };

    const handleTitleSave = async () => {
        const normalizedTitle = titleDraft.trim();
        if (!normalizedTitle || savingTitle) return;

        try {
            setSavingTitle(true);
            setTitleError("");
            await updateLearningNoteTitle(learningNoteId, normalizedTitle);
            await refresh({ silent: true });
            setEditingTitle(false);
        } catch (requestError) {
            setTitleError(
                requestError.response?.data?.message
                || "학습노트 제목을 수정하지 못했습니다.",
            );
        } finally {
            setSavingTitle(false);
        }
    };

    if (loading) {
        return (
            <main className="note-detail-page note-detail-state" aria-busy="true">
                <span className="studio-spinner" />
                <p>완성된 학습노트를 펼치고 있어요.</p>
            </main>
        );
    }

    if (error || !detail) {
        return (
            <main className="note-detail-page note-detail-state">
                <div className="note-detail-error-icon">!</div>
                <h2>학습노트를 불러오지 못했어요</h2>
                <p>{error || "잠시 후 다시 시도해 주세요."}</p>
                <div>
                    <button type="button" className="secondary" onClick={() => navigate("/learning-home")}>학습 홈으로</button>
                    <button type="button" className="primary" onClick={refresh}>다시 불러오기</button>
                </div>
            </main>
        );
    }

    return (
        <main className="note-detail-page">
            <header className="note-detail-header">
                <div className="note-detail-heading">
                    <div className="note-title-area">
                        {editingTitle ? (
                            <div className="note-title-editor">
                                <input
                                    value={titleDraft}
                                    maxLength={255}
                                    onChange={(event) => setTitleDraft(event.target.value)}
                                    onKeyDown={(event) => {
                                        if (event.key === "Enter") handleTitleSave();
                                        if (event.key === "Escape") {
                                            setTitleDraft(detail.title);
                                            setTitleError("");
                                            setEditingTitle(false);
                                        }
                                    }}
                                    aria-label="학습노트 제목"
                                    autoFocus
                                />
                                <button
                                    type="button"
                                    onClick={handleTitleSave}
                                    disabled={savingTitle || !titleDraft.trim()}
                                >
                                    {savingTitle ? "저장 중" : "저장"}
                                </button>
                                <button
                                    type="button"
                                    className="cancel"
                                    disabled={savingTitle}
                                    onClick={() => {
                                        setTitleDraft(detail.title);
                                        setTitleError("");
                                        setEditingTitle(false);
                                    }}
                                >
                                    취소
                                </button>
                            </div>
                        ) : (
                            <div className="note-title-display">
                                <h1>{detail.title}</h1>
                                <button type="button" onClick={() => setEditingTitle(true)}>
                                    제목 수정
                                </button>
                            </div>
                        )}
                        {titleError && <p className="note-title-error" role="alert">{titleError}</p>}
                    </div>
                    <div className="note-detail-actions">
                        <button type="button" className="note-detail-delete" disabled={deleting} onClick={handleDelete}>
                            {deleting ? "삭제 중..." : "노트 삭제"}
                        </button>
                        <button type="button" className="note-detail-new" onClick={() => navigate("/learning")}>새 노트 만들기</button>
                    </div>
                </div>
                {deleteError && <p className="note-detail-delete-error" role="alert">{deleteError}</p>}
            </header>

            <div className="note-detail-workspace">
                <aside className="note-toc-panel">
                    <div className="note-toc-title">
                        <div><span>CONTENTS</span><b>학습 목차</b></div>
                        <div className="toc-filter" aria-label="목차 필터">
                            <button type="button" className={tocFilter === "all" ? "active" : ""} onClick={() => setTocFilter("all")}>전체 <span>{leafContents.length}</span></button>
                            <button type="button" className={tocFilter === "written" ? "active" : ""} onClick={() => setTocFilter("written")}>작성됨 <span>{writtenContents.length}</span></button>
                        </div>
                    </div>
                    <nav aria-label="학습노트 목차">
                        {tocGroups.map((group, groupIndex) => {
                            const collapsed = openSubjectKey !== group.key;
                            const containsActive = group.contents.some((content) => content.tocId === effectiveSelectedTocId);
                            return (
                                <section className={`toc-subject-group ${containsActive ? "has-active" : ""}`} key={group.key}>
                                    <button
                                        type="button"
                                        className="toc-subject-header"
                                        onClick={() => toggleSubject(group.key)}
                                        aria-expanded={!collapsed}
                                    >
                                        <ChevronIcon collapsed={collapsed} />
                                        <span>SUBJECT {groupIndex + 1}.</span>
                                        <strong>{group.title}</strong>
                                    </button>
                                    {!collapsed && (
                                        <div className="toc-subject-items">
                                            {group.contents
                                                .filter((content) => content.leaf !== false)
                                                .map((content) => (
                                                    <button
                                                        type="button"
                                                        key={content.tocId}
                                                        className={`toc-item ${content.tocId === effectiveSelectedTocId ? "active" : ""} ${content.content ? "connected" : "unlinked"}`}
                                                        onClick={() => setSelectedTocId(content.tocId)}
                                                        title={content.content ? undefined : "클릭하면 AI가 해당 단원 노트를 바로 생성을 시작합니다."}
                                                    >
                                                        <span>{String(leafOrderById.get(content.tocId) ?? "").padStart(2, "0")}</span>
                                                        <strong>{content.tocTitle}</strong>
                                                    </button>
                                                ))}
                                        </div>
                                    )}
                                </section>
                            );
                        })}
                    </nav>
                    <div className="note-source-card">
                        <span>연결된 학습 자료</span>
                        {detail.documents.map((document) => (
                            <div key={document.documentId}>
                                <FileIcon />
                                <p><b>{document.fileName}</b><small>{document.fileExtension.toUpperCase()} · {formatFileSize(document.fileSize)}</small></p>
                            </div>
                        ))}
                    </div>
                </aside>

                <section className="note-concept-panel">
                    {selectedContent?.content ? (
                        <>
                            <ConceptContentView
                                chapter={selectedContent ? {
                                    ...selectedContent,
                                    tocTitle: selectedContent.title || selectedContent.tocTitle,
                                    mapped: true,
                                } : null}
                                loading={false}
                            />
                        </>
                    ) : (
                        <UnlinkedContentView
                            topicTitle={selectedContent?.tocTitle}
                            generating={generatingTocId === selectedContent?.tocId}
                            error={generationError}
                            onGenerate={handleGenerateTopic}
                        />
                    )}
                </section>
            </div>
        </main>
    );
}

function UnlinkedContentView({ topicTitle, generating, error, onGenerate }) {
    if (generating) {
        return (
            <div className="topic-generation-skeleton" aria-live="polite" aria-busy="true">
                <div className="skeleton-heading"><span /><span /></div>
                <div className="skeleton-callout"><span /><span /><span /></div>
                <div className="skeleton-lines"><span /><span /><span /><span /></div>
                <p><span>✦</span> AI가 이 단원의 핵심 개념을 정리하고 있어요</p>
            </div>
        );
    }

    return (
        <div className="unlinked-content-view">
            <div className="unlinked-content-icon" aria-hidden="true">
                <FileIcon />
                <span>✦</span>
            </div>
            <h2>아직 &apos;{topicTitle || "선택한"}&apos; 단원의 학습 노트가 없어요.</h2>
            <p>업로드하신 교안에 해당 단원 내용이 부족하거나<br />AI 분석이 완료되지 않은 목차입니다.</p>
            {error && <p className="unlinked-generation-error" role="alert">{error}</p>}
            <button type="button" onClick={onGenerate}><span aria-hidden="true">✨</span>AI로 이 단원 노트 생성하기</button>
        </div>
    );
}

function formatFileSize(bytes = 0) {
    if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))}KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)}MB`;
}

const FileIcon = () => <svg width="17" height="17" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M6 3.5h8l4 4V21H6V3.5Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" /><path d="M14 3.5v4h4" stroke="currentColor" strokeWidth="1.7" /></svg>;
const ChevronIcon = ({ collapsed }) => <svg width="13" height="13" viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d={collapsed ? "m7 4 6 6-6 6" : "m4 7 6 6 6-6"} stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" /></svg>;
