import LearningContentMarkdown from "./LearningContentMarkdown.jsx";

export default function ConceptEditor({
    value,
    onChange,
    onCancel,
    onSave,
    saving,
    isUserAuthored,
    title,
}) {
    return (
        <section className={`study-note-editor ${isUserAuthored ? "user-authored" : ""}`}>
            <div className="concept-editor-toolbar">
                {isUserAuthored
                    ? <h2>{title}</h2>
                    : <span>문서 기반 개념 노트 편집</span>}
                <div>
                    <button type="button" onClick={onCancel} disabled={saving}>편집 취소</button>
                    <button type="button" className="save" onClick={onSave} disabled={saving || !value.trim()}>
                        {saving ? "저장 중..." : "저장 💾"}
                    </button>
                </div>
            </div>
            {isUserAuthored ? (
                <div className="concept-split-editor">
                    <section className="concept-live-preview" aria-label="실시간 미리보기">
                        <header>
                            <span className="concept-pane-dot preview" />
                            <strong>미리보기</strong>
                            <small>실시간 반영</small>
                        </header>
                        <div className="concept-preview-content">
                            {value.trim() ? (
                                <LearningContentMarkdown content={value} />
                            ) : (
                                <div className="concept-preview-empty">
                                    <span>⌁</span>
                                    <strong>아직 작성된 내용이 없어요</strong>
                                    <p>오른쪽에 입력한 내용이 여기에 바로 표시됩니다.</p>
                                </div>
                            )}
                        </div>
                    </section>

                    <section className="concept-writing-pane">
                        <header>
                            <span className="concept-pane-dot writing" />
                            <strong>작성</strong>
                            <small>Markdown</small>
                        </header>
                        <textarea
                            id="study-note-draft"
                            value={value}
                            onChange={(event) => onChange(event.target.value)}
                            placeholder={"# 개념 제목\n\n이 목차에서 기억하고 싶은 내용을 작성해 보세요.\n\n- 핵심 개념\n- 중요한 예시"}
                            aria-label="개념 노트 내용"
                            autoFocus
                        />
                    </section>
                </div>
            ) : (
                <>
                    <label htmlFor="study-note-draft">개념 노트 내용</label>
                    <textarea
                        id="study-note-draft"
                        value={value}
                        onChange={(event) => onChange(event.target.value)}
                        placeholder="이 목차의 핵심 개념을 작성해 주세요."
                        autoFocus
                    />
                    <p>마크다운 문법을 사용할 수 있습니다.</p>
                </>
            )}
        </section>
    );
}
