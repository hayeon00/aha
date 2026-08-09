import { useMemo, useRef, useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";

import { useUserExams } from "../../exam/hooks/useUserExams.js";
import { useLearningNoteCreation } from "../hooks/useLearningNoteCreation.js";

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
        isExamLoading,
        examMessage,
        changeUserExam,
    } = useUserExams();

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

    const currentStep = PROCESS_STEPS[activeStepIndex] ?? PROCESS_STEPS.at(-1);
    const isWorking = Boolean(processing)
        && !["COMPLETED", "FAILED"].includes(processing.status);
    const isCompleted = processing?.status === "COMPLETED";
    const isFailed = processing?.status === "FAILED";
    const canSubmit = Boolean(selectedUserExamId && noteTitle.trim() && file)
        && !submitting
        && !isWorking;
    const displayName = userInfo?.nickname || userInfo?.name || "사용자";

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
                ) : isCompleted ? (
                    <CompletionView
                        title={noteTitle}
                        file={file}
                        learningNoteId={learningNoteId}
                        onHome={() => navigate("/main")}
                        onCreateAnother={startAgain}
                    />
                ) : (
                    <section className="studio-conversation">
                        <header className="studio-hero">
                            <h2>안녕하세요 {displayName}님! 오늘 어떤 시험을 완벽 대비해 드릴까요?</h2>
                            <p>교안이나 요약 자료를 올려주시면 시험 출제 기준에 맞춰 핵심만 정리해 드립니다.</p>
                        </header>
                        <section className="studio-panel studio-ai-canvas">
                            {processing ? (
                                <ProcessingPanel
                                    processing={processing}
                                    progress={progress}
                                    activeStepIndex={activeStepIndex}
                                    currentStep={currentStep}
                                    error={error}
                                    failed={isFailed}
                                    onReset={startAgain}
                                />
                            ) : (
                            <form className="studio-form" onSubmit={handleSubmit}>
                            <fieldset disabled={isWorking || submitting}>
                                <div className="canvas-context-bar">
                                    <div className="canvas-exam-pill">
                                        <span aria-hidden="true">🎯</span>
                                        <select
                                            value={selectedUserExamId ?? ""}
                                            onChange={(event) => changeUserExam(Number(event.target.value))}
                                            aria-label="준비 중인 시험 변경"
                                        >
                                            {userExams.map((exam) => (
                                                <option key={exam.userExamId} value={exam.userExamId}>
                                                    {exam.examName}{exam.versionName ? ` · ${exam.versionName}` : ""}
                                                </option>
                                            ))}
                                        </select>
                                        <ChevronIcon />
                                    </div>
                                    <label className="canvas-title-pill">
                                        <span aria-hidden="true">🏷️</span>
                                        <input
                                            value={title}
                                            maxLength={255}
                                            onChange={(event) => setTitle(event.target.value)}
                                            placeholder={selectedUserExam ? `${selectedUserExam.examName} 핵심 요약` : "노트 제목"}
                                            aria-label="노트 제목"
                                        />
                                    </label>
                                </div>
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
                                            <span className="dropzone-icon"><SparkleIcon /></span>
                                            <h3>{dragging ? "좋아요, 파일을 놓아주세요" : "여기에 교안(PDF/DOCX)을 끌어놓거나 클릭하여 선택하세요"}</h3>
                                            <p>최대 20MB까지 안전하게 업로드할 수 있어요</p>
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
                            )}
                        </section>
                    </section>
                )}
            </section>
        </main>
    );
}

function ProcessingPanel({ processing, progress, activeStepIndex, currentStep, error, failed, onReset }) {
    return (
        <div className={`processing-panel ${failed ? "failed" : ""}`}>
            <div className="processing-top">
                <span className="processing-pulse">{failed ? <AlertIcon /> : <SparkleIcon />}</span>
                <div><span>{failed ? "처리 중 문제가 발생했어요" : "AI가 노트를 만들고 있어요"}</span><h2>{failed ? "문서를 다시 확인해 주세요" : currentStep?.label}</h2></div>
            </div>
            <div className="progress-track"><span style={{ width: `${progress}%` }} /></div>
            <div className="progress-meta"><span>{failed ? "처리 중단" : currentStep?.description}</span><b>{progress}%</b></div>
            <ol className="processing-steps">
                {PROCESS_STEPS.map((step, index) => {
                    const done = processing.status === "COMPLETED" || index < activeStepIndex;
                    const active = !failed && index === activeStepIndex;
                    const failedHere = failed && index === activeStepIndex;
                    return (
                        <li key={step.key} className={`${done ? "done" : ""} ${active ? "active" : ""} ${failedHere ? "error" : ""}`}>
                            <span>{done ? <CheckIcon /> : index + 1}</span>
                            <div><b>{step.label}</b><small>{active ? step.description : done ? "완료" : "대기 중"}</small></div>
                        </li>
                    );
                })}
            </ol>
            {error && <p className="processing-error"><AlertIcon />{error}</p>}
            {failed && <button type="button" className="processing-reset" onClick={onReset}>다른 파일로 다시 만들기</button>}
            {!failed && <p className="processing-notice">페이지를 닫지 않아도 진행 상태가 자동으로 업데이트됩니다.</p>}
        </div>
    );
}

function CompletionView({ title, file, learningNoteId, onHome, onCreateAnother }) {
    return (
        <section className="completion-card">
            <div className="completion-icon"><CheckIcon /><span>✦</span></div>
            <span className="side-kicker">CREATION COMPLETE</span>
            <h2>학습노트가 준비됐어요</h2>
            <p>업로드한 문서 분석을 마쳤습니다. 완성된 노트는 학습 홈에서 이어서 확인할 수 있어요.</p>
            <div className="completion-note">
                <span className="selected-file-icon">{getExtension(file?.name)}</span>
                <div><b>{title}</b><small>{file?.name} · 노트 #{learningNoteId}</small></div>
                <CheckIcon />
            </div>
            <div className="completion-actions">
                <button type="button" className="secondary" onClick={onCreateAnother}>새 노트 만들기</button>
                <button type="button" className="primary" onClick={onHome}>학습 홈으로 이동<ArrowIcon /></button>
            </div>
        </section>
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
const CheckIcon = () => <Icon size={16}><path d="m5 12 4 4 10-10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const CloseIcon = () => <Icon size={18}><path d="m7 7 10 10M17 7 7 17" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" /></Icon>;
const ArrowIcon = () => <Icon size={18}><path d="M5 12h14m-5-5 5 5-5 5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const AlertIcon = () => <Icon size={18}><path d="M12 3 2.8 20h18.4L12 3Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" /><path d="M12 9v5m0 3v.1" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" /></Icon>;
const ShieldIcon = () => <Icon size={15}><path d="M12 3 5 6v5c0 4.5 2.8 7.8 7 10 4.2-2.2 7-5.5 7-10V6l-7-3Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" /><path d="m9 12 2 2 4-4" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" /></Icon>;
const ChevronIcon = () => <Icon size={17}><path d="m7 9 5 5 5-5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" /></Icon>;

function formatFileSize(bytes = 0) {
    if (bytes < 1024 * 1024) return `${Math.max(1, Math.round(bytes / 1024))}KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)}MB`;
}

function getExtension(name = "") {
    return name.split(".").pop()?.toUpperCase() || "FILE";
}

export default AiLearningPage;
