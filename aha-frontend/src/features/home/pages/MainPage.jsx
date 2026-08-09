import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import NoteCardGrid from "../components/NoteCardGrid.jsx";
import { deleteLearningNote, getCompletedLearningNotes } from "../../ailearn/api/learningNoteApi.js";
import { getApiData } from "../../ailearn/utils/apiResponseUtils.js";

import "./MainPage.css";

function MainPage() {
    const navigate = useNavigate();
    const [notes, setNotes] = useState([]);
    const [message, setMessage] = useState("");
    const [deletingNoteId, setDeletingNoteId] = useState(null);

    useEffect(() => {
        let cancelled = false;
        getCompletedLearningNotes()
            .then((response) => {
                if (!cancelled) setNotes(getApiData(response) || []);
            })
            .catch((error) => {
                if (!cancelled) {
                    setMessage(
                        error.response?.data?.message
                        || "학습노트를 불러오지 못했습니다.",
                    );
                }
            });
        return () => { cancelled = true; };
    }, []);

    const createNote = () => navigate("/learning");
    const openNote = (note) => navigate(`/learning?view=notes&noteId=${note.id}`);
    const removeNote = async (noteId) => {
        const note = notes.find((item) => item.id === noteId);
        if (!window.confirm(`'${note?.title || "선택한 학습노트"}'을 삭제할까요?\n삭제된 노트는 복구할 수 없습니다.`)) return;

        try {
            setDeletingNoteId(noteId);
            setMessage("");
            await deleteLearningNote(noteId);
            setNotes((current) => current.filter((item) => item.id !== noteId));
        } catch (error) {
            setMessage(error.response?.data?.message || "학습노트를 삭제하지 못했습니다.");
        } finally {
            setDeletingNoteId(null);
        }
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
                    deletingNoteId={deletingNoteId}
                />
                {message && <p role="alert">{message}</p>}
            </main>
        </div>
    );
}

export default MainPage;
