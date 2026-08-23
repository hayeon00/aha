import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useOutletContext, useSearchParams } from "react-router-dom";

import { useUserExams } from "../../exam/hooks/useUserExams.js";
import { useExamScopeNodes } from "../../exam/hooks/useExamScopeNodes.js";
import SyllabusTree from "../../exam/components/SyllabusTree.jsx";
import { useLearningNoteCreation } from "../hooks/useLearningNoteCreation.js";
import LearningNoteDetailPage from "./LearningNoteDetailPage.jsx";

import "./AiLearningPage.css";

const MAX_FILE_SIZE = 20 * 1024 * 1024;
const ACCEPTED_EXTENSIONS = ["pdf", "docx"];

const PROCESS_STEPS = [
    { key: "TEXT_EXTRACTING", label: "내용 읽기", description: "문서의 텍스트와 구조를 읽고 있어요." },
    { key: "CHUNKING", label: "내용 정리", description: "학습하기 좋은 단위로 내용을 정리하고 있어요." },
    { key: "EMBEDDING", label: "핵심 분석", description: "문서의 의미와 핵심 내용을 분석하고 있어요." },
    { key: "SCOPE_MAPPING", label: "목차 연결", description: "시험 목차와 문서 내용을 연결하고 있어요." },
    { key: "CONTENT_GENERATING", label: "노트 완성", description: "학습할 개념 설명을 완성하고 있어요." },
];

function AiLearningPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const requestedNoteId = Number(searchParams.get("noteId"));
    const requestedUserExamId = Number(searchParams.get("userExamId"));

    useEffect(() => {
        if (requestedUserExamId) sessionStorage.setItem("activeUserExamId", String(requestedUserExamId));
    }, [requestedUserExamId]);
    const { userInfo } = useOutletContext() ?? {};
    const fileInputRef = useRef(null);
    const [title, setTitle] = useState("");
    const [file, setFile] = useState(null);
    const [dragging, setDragging] = useState(false);
    const [validationMessage, setValidationMessage] = useState("");

    const {
        userExams,
        selectedUserExamId,
        selectedUserExam,
        selectedExamVersionId,
        isExamLoading,
        examMessage,
    } = useUserExams({
        initialUserExamId: Number.isInteger(requestedUserExamId) && requestedUserExamId > 0
            ? requestedUserExamId
            : undefined,
    });

    const {
        scopeNodes,
        isScopeLoading,
        scopeMessage,
    } = useExamScopeNodes({
        examVersionId: selectedExamVersionId,
    });

    const {
        submitting,
        processing,
        learningNoteId,
        error,
        submit,
        reset,
    } = useLearningNoteCreation();

    const noteTitle = title.trim() || (
        selectedUserExam ? `${selectedUserExam.examName} 학습노트` : ""
    );

    const activeStepIndex = useMemo(() => {
        if (processing?.status === "COMPLETED") return PROCESS_STEPS.length;
        return Math.max(
            0,
            PROCESS_STEPS.findIndex((step) => step.key === processing?.currentStep),
        );
    }, [processing]);

    const progress = useMemo(() => {
        if (!processing) return 0;
        if (processing.status === "UPLOADING") return 8;
        if (processing.status === "PENDING") return 14;
        if (processing.status === "COMPLETED") return 100;
        if (processing.status === "FAILED") return Math.max(12, activeStepIndex * 20);
        return Math.min(92, 22 + activeStepIndex * 17);
    }, [activeStepIndex, processing]);

    const isWorking = Boolean(processing)
        && !["COMPLETED", "FAILED"].includes(processing.status);
    const isCompleted = processing?.status === "COMPLETED";
    const isFailed = processing?.status === "FAILED";
    const canSubmit = Boolean(selectedUserExamId && noteTitle.trim() && file)
        && !submitting
        && !isWorking;
    const displayName = sanitizeDisplayName(
        userInfo?.nickname || userInfo?.name || "사용자",
    );

    useEffect(() => {
        if (!isCompleted || !learningNoteId) return undefined;

        const timerId = window.setTimeout(() => {
            navigate(`/learning?view=notes&noteId=${learningNoteId}`, { replace: true });
        }, 650);

        return () => window.clearTimeout(timerId);
    }, [isCompleted, learningNoteId, navigate]);

    if (Number.isInteger(requestedNoteId) && requestedNoteId > 0) {
        return <LearningNoteDetailPage learningNoteId={requestedNoteId} />;
    }

    const selectFile = (candidate) => {
        if (!candidate) return;

        const extension = candidate.name.split(".").pop()?.toLowerCase();
        if (!ACCEPTED_EXTENSIONS.includes(extension)) {
            setValidationMessage("PDF 또는 DOCX 파일만 업로드할 수 있습니다.");
            return;
        }
        if (candidate.size <= 0) {
            setValidationMessage("내용이 없는 파일은 업로드할 수 없습니다.");
            return;
        }
        if (candidate.size > MAX_FILE_SIZE) {
            setValidationMessage("파일 크기는 최대 20MB까지 가능합니다.");
            return;
        }

        setFile(candidate);
        setValidationMessage("");
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (!canSubmit) return;
        await submit({
            userExamId: selectedUserExamId,
            title: noteTitle,
            file,
        });
    };

    const startAgain = () => {
        reset();
        setFile(null);
        setValidationMessage("");
        setTitle("");
    };

    if (isExamLoading) {
        return (
            <main className="studio-page studio-loading">
                <span className="studio-spinner" />
                <p>노트 스튜디오를 준비하고 있어요.</p>
            </main>
        );
    }

    return (
        <main className="studio-page">
            <section className="studio-shell">
                {userExams.length === 0 ? (
                    <EmptyExamState onMove={() => navigate("/mypage")} message={examMessage} />
                ) : (
                    <section className="studio-conversation">
                        <header className="studio-hero">
                            <p className="studio-greeting">안녕하세요, {displayName}님!</p>
                            <h2>오늘 어떤 자료를 완벽하게 요약해 드릴까요?</h2>
                            <p className="studio-hero-description">학습 자료를 올리시면 시험 출제 기준에 맞춰 핵심 목차와 학습 노트를 생성해 드립니다.</p>
                        </header>
                        <section className="studio-panel studio-ai-canvas">
                            <form className="studio-form" onSubmit={handleSubmit}>
                                <fieldset disabled={isWorking || submitting}>
                                    <div className="canvas-context-bar">
                                        <div className="studio-title-field">
                                            <div className="studio-title-label"><strong>학습 노트 제목</strong><span>AI가 생성할 노트의 이름을 입력해 주세요</span></div>
                                            <label className="canvas-title-pill">
                                                <span className="title-field-icon" aria-hidden="true">Aa</span>
                                                <input
                                                    value={title}
                                                    maxLength={255}
                                                    onChange={(event) => setTitle(event.target.value)}
                                                    placeholder={selectedUserExam ? `예: ${selectedUserExam.examName} 핵심 요약` : "학습 노트 제목을 입력하세요"}
                                                    aria-label="학습 노트 제목"
                                                />
                                            </label>
                                        </div>
                                    </div>
                                    <div className="studio-upload-layout">
                                <aside
                                    className="studio-syllabus-panel"
                                    aria-label={`${selectedUserExam?.examName ?? "선택한 시험"} 시험 목차`}
                                >
                                    <div className="studio-syllabus-scroll">
                                        {isScopeLoading ? (
                                            <div className="studio-syllabus-state" aria-busy="true">
                                                <span className="studio-spinner" />
                                                <p>목차를 불러오고 있어요.</p>
                                            </div>
                                        ) : scopeNodes.length > 0 ? (
                                            <SyllabusTree
                                                nodes={scopeNodes}
                                                selectedNodeId={null}
                                                onSelectNode={() => {}}
                                                selectable={false}
                                            />
                                        ) : (
                                            <div className="studio-syllabus-state">
                                                <p>{scopeMessage || "표시할 시험 목차가 없습니다."}</p>
                                            </div>
                                        )}
                                    </div>
                                    <p className="studio-syllabus-caption">
                                        업로드한 내용은 관련 있는 목차에 자동으로 연결됩니다.
                                    </p>
                                </aside>
                                <div className="studio-upload-column">
                                <div className="studio-field studio-upload-field">
                                    {!file ? (
                                        <div
                                            className={`studio-dropzone ${dragging ? "dragging" : ""}`}
                                            onDragEnter={(event) => { event.preventDefault(); setDragging(true); }}
                                            onDragOver={(event) => event.preventDefault()}
                                            onDragLeave={(event) => {
                                                if (!event.currentTarget.contains(event.relatedTarget)) setDragging(false);
                                            }}
                                            onDrop={(event) => {
                                                event.preventDefault();
                                                setDragging(false);
                                                selectFile(event.dataTransfer.files?.[0]);
                                            }}
                                            onClick={() => fileInputRef.current?.click()}
                                            onKeyDown={(event) => {
                                                if (["Enter", " "].includes(event.key)) fileInputRef.current?.click();
                                            }}
                                            role="button"
                                            tabIndex={0}
                                        >
                                            <span className="dropzone-icon"><DocumentIcon /><i><SparkleIcon /></i></span>
                                            <h3>{dragging ? "좋아요, 파일을 놓아주세요" : "학습 자료(PDF, DOCX)를 드래그하거나 클릭하여 업로드하세요"}</h3>
                                            <p>최대 20MB까지 안전하게 분석해 드려요</p>
                                            <div className="dropzone-supports" aria-label="지원 파일 형식">
                                                <span>PDF</span><span>DOCX</span>
                                            </div>
                                        </div>
                                    ) : (
                                        <div className="selected-file">
                                            <span className="selected-file-icon">{getExtension(file.name)}</span>
                                            <div><b>{file.name}</b><small>{formatFileSize(file.size)} · 업로드 준비 완료</small></div>
                                            <button type="button" onClick={() => setFile(null)} aria-label="선택한 파일 제거"><CloseIcon /></button>
                                        </div>
                                    )}
                                    <input
                                        ref={fileInputRef}
                                        id="note-file"
                                        className="studio-file-input"
                                        type="file"
                                        accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                        onChange={(event) => {
                                            selectFile(event.target.files?.[0]);
                                            event.target.value = "";
                                        }}
                                    />
                                    {validationMessage && <p className="studio-validation"><AlertIcon />{validationMessage}</p>}
                                </div>
                                </div>
                                    </div>
                                </fieldset>

                                {file && (
                                    <div className="canvas-action-row">
                                        <span><ShieldIcon />문서는 노트 생성에만 사용돼요</span>
                                        <button className="studio-submit" type="submit" disabled={!canSubmit}>
                                            {submitting ? <><span className="button-spinner" />생성 중...</> : <><SparkleIcon />AI 학습노트 생성<ArrowIcon /></>}
                                        </button>
                                    </div>
                                )}
                            </form>
                        </section>
                    </section>
                )}
            </section>
            {processing && (
                <LoadingModal
                    progress={progress}
                    completed={isCompleted}
                    failed={isFailed}
                    error={error}
                    onReset={startAgain}
                />
            )}
        </main>
    );
}

function LoadingModal({ progress, completed, failed, error, onReset }) {
    return (
        <div className={`loading-modal-backdrop ${completed ? "completed" : ""}`} role="presentation">
            <section className="loading-modal" role="dialog" aria-modal="true" aria-labelledby="loading-modal-title">
                <div className={`loading-sparkle ${failed ? "failed" : ""}`}>
                    {failed ? <AlertIcon /> : <SparkleIcon />}
                    {!failed && <span />}
                </div>
                <h2 id="loading-modal-title">
                    {failed ? "학습노트 생성에 실패했어요" : "AI가 맞춤 학습노트를 생성하고 있어요"}
                </h2>
                {!failed ? (
                    <>
                        <div className="loading-progress-meta"><span>노트 생성 진행률</span><b>{progress}%</b></div>
                        <div className="loading-progress-track"><span style={{ width: `${progress}%` }} /></div>
                        <p className="loading-status">시험 출제 목차와 학습 자료의 핵심 개념을 분석 중입니다...</p>
                        <p className="loading-caption">잠시만 기다려주시면 나만의 노트를 펼쳐드려요 ✨</p>
                    </>
                ) : (
                    <>
                        <p className="loading-status">{error || "문서를 처리하지 못했습니다. 파일을 확인한 뒤 다시 시도해 주세요."}</p>
                        <button type="button" className="loading-reset" onClick={onReset}>파일 다시 선택하기</button>
                    </>
                )}
            </section>
        </div>
    );
}

function EmptyExamState({ onMove, message }) {
    return (
        <section className="studio-empty">
            <span><DocumentIcon /></span><h2>학습 시험을 먼저 등록해 주세요</h2>
            <p>{message || "마이페이지에서 학습할 시험을 추가하면 노트 스튜디오를 사용할 수 있습니다."}</p>
            <button type="button" onClick={onMove}>마이페이지로 이동<ArrowIcon /></button>
        </section>
    );
}

const Icon = ({ children, size = 20 }) => <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden="true">{children}</svg>;
const SparkleIcon = () => <Icon size={17}><path d="M12 2.8c.8 4.4 2.8 6.5 7.2 7.2-4.4.8-6.5 2.8-7.2 7.2-.8-4.4-2.8-6.5-7.2-7.2 4.4-.8 6.5-2.8 7.2-7.2Z" fill="currentColor" /><path d="M19 16.5c.3 1.7 1.1 2.5 2.8 2.8-1.7.3-2.5 1.1-2.8 2.8-.3-1.7-1.1-2.5-2.8-2.8 1.7-.3 2.5-1.1 2.8-2.8Z" fill="currentColor" /></Icon>;
const DocumentIcon = () => <Icon size={34}><path d="M6 3.5h8l4 4V21H6V3.5Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" /><path d="M14 3.5v4h4M9 12h6M9 16h5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" /></Icon>;
const CloseIcon = () => <Icon size={18}><path d="m7 7 10 10M17 7 7 17" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" /></Icon>;
const ArrowIcon = () => <Icon size={18}><path d="M5 12h14m-5-5 5 5-5 5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const AlertIcon = () => <Icon size={18}><path d="M12 3 2.8 20h18.4L12 3Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" /><path d="M12 9v5m0 3v.1" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" /></Icon>;
const ShieldIcon = () => <Icon size={15}><path d="M12 3 5 6v5c0 4.5 2.8 7.8 7 10 4.2-2.2 7-5.5 7-10V6l-7-3Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" /><path d="m9 12 2 2 4-4" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" /></Icon>;
function formatFileSize(bytes = 0) {
    if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))}KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)}MB`;
}

function sanitizeDisplayName(value) {
    return String(value).trim().replace(/_[a-z0-9]{8,}$/i, "") || "사용자";
}

function getExtension(name = "") {
    return name.split(".").pop()?.toUpperCase() || "FILE";
}

export default AiLearningPage;
