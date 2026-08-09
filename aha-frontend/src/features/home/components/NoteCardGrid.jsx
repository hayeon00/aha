export default function NoteCardGrid({ notes, onCreate, onOpen, onRemove, deletingNoteId }) {
    return (
        <div className="note-card-grid">
            <button type="button" className="note-create-card" onClick={onCreate}>
                <span>+</span>
                <strong>새 학습노트 만들기</strong>
                <small>학습 자료를 연결해 새 노트를 시작하세요</small>
            </button>
            {notes.map((note) => (
                <article className="learning-note-card" key={note.id}>
                    <div className="note-card-top">
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
                    </button>
                </article>
            ))}
        </div>
    );
}
