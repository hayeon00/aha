import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import NoteCardGrid from "../components/NoteCardGrid.jsx";
import { getCompletedLearningNotes } from "../../ailearn/api/learningNoteApi.js";
import { getApiData } from "../../ailearn/utils/apiResponseUtils.js";

import "./MainPage.css";

function MainPage() {
    const navigate = useNavigate();
    const [notes, setNotes] = useState([]);
    const [message, setMessage] = useState("");

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
    const openNote = (note) => navigate(`/learning?view=notes&documentId=${note.documentId}&tocId=${note.tocId}`);

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
                />
                {message && <p role="alert">{message}</p>}
            </main>
        </div>
    );
}

export default MainPage;
