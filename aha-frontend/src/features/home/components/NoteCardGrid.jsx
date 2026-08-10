const formatDate = (value) => value
    ? new Date(value).toLocaleDateString("ko-KR", { year: "numeric", month: "2-digit", day: "2-digit" })
    : "최근 기록 없음";

export default function NoteCardGrid({ notes, category = "AI 학습노트", onCreate, onOpen, onRemove, deletingNoteId }) {
    return (
        <div className="note-card-grid">
            {onCreate && <button type="button" className="note-create-card" onClick={onCreate}>
                <span>+</span>
                <strong>새 학습노트 만들기</strong>
                <small>교안 파일(PDF)을 연결해 AI 노트를 생성하세요</small>
            </button>}
            {notes.map((note) => (
                <article className="learning-note-card" key={note.id}>
                    <div className="note-card-top">
                        <span className="note-category-chip">{note.examName || category}</span>
                        {onRemove && (
                            <details className="note-card-menu">
                                <summary aria-label={`${note.title} 메뉴`}>⋯</summary>
                                <div>
                                    <button type="button" disabled={deletingNoteId === note.id} onClick={() => onRemove(note.id)}>
                                        {deletingNoteId === note.id ? "삭제 중..." : "삭제"}
                                    </button>
                                </div>
                            </details>
                        )}
                    </div>
                    <button type="button" className="note-card-open" onClick={() => onOpen(note)}>
                        <h3>{note.title}</h3>
                        <p>연결된 교안 {note.sourceDocumentCount || 1}개 · AI 분석 완료</p>
                        <div className="note-progress"><i /></div>
                        <footer><span>최근 학습: {formatDate(note.updatedAt)}</span><b>→</b></footer>
                    </button>
                </article>
            ))}
        </div>
    );
}
