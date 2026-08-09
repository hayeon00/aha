import EditableTitle from "./EditableTitle.jsx";

export default function NoteHeader({
    title,
    onTitleSave,
    documents,
    selectedDocumentId,
    onDocumentSelect,
    editing,
    onEdit,
    titleEditable = true,
}) {
    return (
        <header className="study-view-header">
            <EditableTitle title={title} onSave={onTitleSave} editable={titleEditable} />
            <div className="study-view-actions">
                <details className="related-documents">
                    <summary>📄 연관 문서 ({documents.length})</summary>
                    <div className="related-documents-menu">
                        {documents.map((document) => (
                            <button
                                type="button"
                                key={document.id}
                                className={document.documentId === selectedDocumentId ? "selected" : ""}
                                onClick={() => onDocumentSelect(document.documentId)}
                            >
                                <strong>{document.name}</strong>
                            </button>
                        ))}
                    </div>
                </details>
                {onEdit && (
                    <button type="button" className="note-edit-button" onClick={onEdit}>
                        {editing ? "편집 완료" : "편집 / 설정 ⚙️"}
                    </button>
                )}
            </div>
        </header>
    );
}
