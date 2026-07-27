import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import NoteCardGrid from "../components/NoteCardGrid.jsx";
import {
    getLearningNotes,
    LEARNING_NOTES_UPDATED_EVENT,
    removeLearningNote,
} from "../../ailearn/utils/learningNoteStorage.js";

import "./MainPage.css";

function MainPage() {
    const navigate = useNavigate();
    const [notes, setNotes] = useState(() => getLearningNotes());

    useEffect(() => {
        const refresh = () => setNotes(getLearningNotes());
        window.addEventListener(LEARNING_NOTES_UPDATED_EVENT, refresh);
        window.addEventListener("storage", refresh);
        return () => {
            window.removeEventListener(LEARNING_NOTES_UPDATED_EVENT, refresh);
            window.removeEventListener("storage", refresh);
        };
    }, []);

    const createNote = () => navigate("/learning");
    const openNote = (note) => navigate(`/learning?view=notes&documentId=${note.documentId}&tocId=${note.tocId}`);
    const removeNote = (noteId) => {
        removeLearningNote(noteId);
        setNotes(getLearningNotes());
    };

    return (
        <div className="main-page">
            <main className="main-content">
                <header className="learning-home-header">
                    <div>
                        <h1>학습노트</h1>
                        <p>내 자료로 만든 개념 노트를 이어서 학습해 보세요.</p>
                    </div>
                </header>
                <NoteCardGrid
                    notes={notes}
                    onCreate={createNote}
                    onOpen={openNote}
                    onRemove={removeNote}
                />
            </main>
        </div>
    );
}

export default MainPage;
