export default function ConceptEditor({
    value,
    onChange,
    onRegenerate,
    onCancel,
    onSave,
    generating,
    saving,
    isExternalKnowledge,
}) {
    return (
        <section className="study-note-editor">
            <div className="concept-editor-toolbar">
                <span>
                    {isExternalKnowledge
                        ? "💡 표준 개념 지식 기반 생성됨 (업로드 문서 내용 없음)"
                        : "문서 기반 개념 노트 편집"}
                </span>
                <div>
                    {isExternalKnowledge && (
                        <button type="button" onClick={onRegenerate} disabled={generating || saving}>
                            다시 생성 🔄
                        </button>
                    )}
                    <button type="button" onClick={onCancel} disabled={saving}>편집 취소</button>
                    <button type="button" className="save" onClick={onSave} disabled={saving || !value.trim()}>
                        {saving ? "저장 중..." : "저장 💾"}
                    </button>
                </div>
            </div>
            <label htmlFor="study-note-draft">개념 노트 내용</label>
            <textarea
                id="study-note-draft"
                value={value}
                onChange={(event) => onChange(event.target.value)}
                placeholder="이 목차의 핵심 개념을 작성해 주세요."
                autoFocus
            />
            <p>마크다운 문법을 사용할 수 있습니다.</p>
        </section>
    );
}
