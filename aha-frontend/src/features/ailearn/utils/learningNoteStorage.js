const STORAGE_KEY = "aha:learning-notes";
export const LEARNING_NOTES_UPDATED_EVENT = "aha:learning-notes-updated";

export function getLearningNotes() {
    try {
        const value = JSON.parse(window.localStorage.getItem(STORAGE_KEY) || "[]");
        if (!Array.isArray(value)) return [];
        const notesByDocument = new Map();
        value.forEach((note) => {
            const key = String(note.documentId ?? note.id);
            const current = notesByDocument.get(key);
            if (!current || new Date(note.updatedAt ?? 0) > new Date(current.updatedAt ?? 0)) {
                notesByDocument.set(key, note);
            }
        });
        return [...notesByDocument.values()];
    } catch {
        return [];
    }
}

export function saveLearningNote(note) {
    const notes = getLearningNotes();
    const current = notes.find((item) => item.documentId === note.documentId);
    const id = note.id ?? current?.id ?? `document:${note.documentId}`;
    const nextNote = {
        progress: 0,
        ...current,
        ...note,
        topicContents: note.tocId && note.content != null
            ? {
                ...(current?.topicContents ?? {}),
                [note.tocId]: note.content,
            }
            : current?.topicContents ?? {},
        id,
        updatedAt: new Date().toISOString(),
    };
    const nextNotes = [
        nextNote,
        ...notes.filter((item) => item.documentId !== note.documentId && item.id !== id),
    ];

    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(nextNotes));
    window.dispatchEvent(new CustomEvent(LEARNING_NOTES_UPDATED_EVENT));
    return nextNote;
}

export function removeLearningNote(noteId) {
    const nextNotes = getLearningNotes().filter((note) => note.id !== noteId);
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(nextNotes));
    window.dispatchEvent(new CustomEvent(LEARNING_NOTES_UPDATED_EVENT));
}
