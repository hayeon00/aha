import { useRef, useState } from "react";
import "./UnmappedTopicEmptyState.css";

const ACCEPTED_FILES = ".pdf,.docx,.png,.jpg,.jpeg,.webp";

const EDITOR_TOOLS = [
    { id: "heading", icon: "H", label: "제목", command: "formatBlock", value: "h3" },
    { id: "bold", icon: "B", label: "강조", command: "bold" },
    { id: "list", icon: "≡", label: "목록", command: "insertUnorderedList" },
    { id: "table", icon: "▦", label: "표", command: "insertTable" },
];

export default function UnmappedTopicEmptyState({
    topicTitle,
    onAddFiles,
    onSaveSummary,
}) {
    const fileInputRef = useRef(null);
    const editorRef = useRef(null);
    const [activeTab, setActiveTab] = useState("file");
    const [noteTitle, setNoteTitle] = useState(topicTitle || "");
    const [isDragging, setIsDragging] = useState(false);
    const [hasEditorContent, setHasEditorContent] = useState(false);
    const [feedback, setFeedback] = useState("");

    const changeTab = (tab) => {
        setActiveTab(tab);
        setFeedback("");
    };

    const submitFiles = (fileList) => {
        const files = Array.from(fileList || []);
        if (!files.length) return;
        onAddFiles?.(files);
        setFeedback(`${files.length}개의 자료를 선택했어요.`);
    };

    const runEditorCommand = (tool) => {
        editorRef.current?.focus();
        if (tool.command === "insertTable") {
            document.execCommand(
                "insertHTML",
                false,
                "<table><tbody><tr><th>항목</th><th>내용</th></tr><tr><td><br></td><td><br></td></tr></tbody></table><p><br></p>",
            );
        } else {
            document.execCommand(tool.command, false, tool.value);
        }
        setHasEditorContent(Boolean(editorRef.current?.innerText.trim()));
    };

    const saveNote = () => {
        const content = editorRef.current?.innerText.trim() || "";
        if (!content) return;
        onSaveSummary?.(noteTitle.trim() ? `${noteTitle.trim()}\n\n${content}` : content);
        setFeedback("직접 작성한 노트를 저장했어요.");
    };

    return (
        <section className="two-way-empty" aria-labelledby="two-way-empty-title">
            <header className="two-way-header">
                <h2 id="two-way-empty-title">
                    내 문서에서 <strong>‘{topicTitle}’</strong>를 찾지 못했어요 <span aria-hidden="true">🔍</span>
                </h2>
                <p>관련 자료를 더하거나, 내 언어로 직접 정리해 보세요.</p>
            </header>

            <nav className="two-way-tabs" aria-label="개념 채우기 방식">
                <button
                    type="button"
                    className={activeTab === "file" ? "is-active" : ""}
                    aria-pressed={activeTab === "file"}
                    onClick={() => changeTab("file")}
                >
                    <span aria-hidden="true">📂</span> 파일 추가
                </button>
                <button
                    type="button"
                    className={activeTab === "editor" ? "is-active" : ""}
                    aria-pressed={activeTab === "editor"}
                    onClick={() => changeTab("editor")}
                >
                    <span aria-hidden="true">✍️</span> 직접 작성
                </button>
            </nav>

            <div className="two-way-stage">
                {activeTab === "file" ? (
                    <div className="two-way-file-view">
                        <div
                            className={`two-way-dropzone ${isDragging ? "is-dragging" : ""}`}
                            onDragEnter={(event) => {
                                event.preventDefault();
                                setIsDragging(true);
                            }}
                            onDragOver={(event) => event.preventDefault()}
                            onDragLeave={() => setIsDragging(false)}
                            onDrop={(event) => {
                                event.preventDefault();
                                setIsDragging(false);
                                submitFiles(event.dataTransfer.files);
                            }}
                        >
                            <span className="two-way-upload-icon" aria-hidden="true">↑</span>
                            <h3>관련 파일을 끌어다 놓으세요</h3>
                            <p>‘{topicTitle}’ 내용이 담긴 자료를 추가하면 문서를 다시 분석해요.</p>
                            <small>PDF · DOCX · PNG · JPG</small>
                            <button type="button" onClick={() => fileInputRef.current?.click()}>
                                파일 찾아보기
                            </button>
                            <input
                                ref={fileInputRef}
                                type="file"
                                accept={ACCEPTED_FILES}
                                multiple
                                hidden
                                onChange={(event) => {
                                    submitFiles(event.target.files);
                                    event.target.value = "";
                                }}
                            />
                        </div>
                    </div>
                ) : (
                    <div className="two-way-editor-view">
                        <div className="two-way-editor-main">
                            <label className="two-way-title-field">
                                <span>노트 제목</span>
                                <input
                                    value={noteTitle}
                                    maxLength={100}
                                    placeholder="노트 제목을 입력하세요"
                                    onChange={(event) => setNoteTitle(event.target.value)}
                                />
                            </label>

                            <div className="two-way-content-field">
                                <span>내용</span>
                                <div
                                    ref={editorRef}
                                    className="two-way-content-editor"
                                    contentEditable
                                    suppressContentEditableWarning
                                    data-placeholder="이 단원에서 기억하고 싶은 핵심 개념을 작성해 보세요."
                                    onInput={(event) => setHasEditorContent(Boolean(event.currentTarget.innerText.trim()))}
                                />
                            </div>
                        </div>

                        <aside className="two-way-editor-tools">
                            <div>
                                <span>편집 도구</span>
                                <p>내용을 구조화해 정리해 보세요.</p>
                            </div>
                            <div className="two-way-tool-list">
                                {EDITOR_TOOLS.map((tool) => (
                                    <button type="button" key={tool.id} onClick={() => runEditorCommand(tool)}>
                                        <span aria-hidden="true">{tool.icon}</span>
                                        {tool.label}
                                    </button>
                                ))}
                            </div>
                        </aside>

                        <footer className="two-way-editor-actions">
                            <button type="button" className="is-secondary" onClick={() => changeTab("file")}>
                                취소
                            </button>
                            <button
                                type="button"
                                className="is-primary"
                                disabled={!hasEditorContent}
                                onClick={saveNote}
                            >
                                저장하기
                            </button>
                        </footer>
                    </div>
                )}
            </div>

            {feedback && <p className="two-way-feedback" role="status">✓ {feedback}</p>}
        </section>
    );
}
