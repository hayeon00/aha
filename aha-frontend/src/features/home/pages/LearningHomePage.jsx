import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import NoteCardGrid from "../components/NoteCardGrid.jsx";
import { deleteLearningNote, getCompletedLearningNotes } from "../../ailearn/api/learningNoteApi.js";
import { getUserExams } from "../../exam/api/userExamApi.js";
import { getApiData } from "../../ailearn/utils/apiResponseUtils.js";
import "./LearningHomePage.css";

export default function LearningHomePage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const activeUserExamId = Number(searchParams.get("userExamId") || sessionStorage.getItem("activeUserExamId"));
    const [notes, setNotes] = useState([]);
    const [userExams, setUserExams] = useState([]);
    const [message, setMessage] = useState("");
    const [deletingNoteId, setDeletingNoteId] = useState(null);

    useEffect(() => {
        let cancelled = false;
        Promise.all([getCompletedLearningNotes(), getUserExams()])
            .then(([noteResponse, examResponse]) => {
                if (cancelled) return;
                setNotes(getApiData(noteResponse) || []);
                setUserExams(getApiData(examResponse) || []);
            })
            .catch((error) => !cancelled && setMessage(error.response?.data?.message || "학습노트를 불러오지 못했습니다."));
        return () => { cancelled = true; };
    }, []);

    const activeExam = userExams.find((exam) => Number(exam.userExamId) === activeUserExamId);

    const examNotes = useMemo(() => {
        const hasExamRelation = notes.some((note) => note.userExamId != null);
        return hasExamRelation && activeUserExamId
            ? notes.filter((note) => Number(note.userExamId) === activeUserExamId)
            : notes;
    }, [notes, activeUserExamId]);

    const createNote = () => navigate(`/learning${activeUserExamId ? `?userExamId=${activeUserExamId}` : ""}`);
    const openNote = (note) => navigate(`/learning?noteId=${note.id}${activeUserExamId ? `&userExamId=${activeUserExamId}` : ""}`);
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
        <div className="learning-home-page">
            <div className="learning-home-content">
                <header className="learning-dashboard-header">
                    <span>{[activeExam?.examCode, activeExam?.versionName].filter(Boolean).join(" · ") || "최신 출제 기준"}</span>
                    <h1>내 학습 노트</h1>
                    <p>생성된 AI 학습 노트를 선택해 정독하거나 새로운 노트를 만들어 보세요.</p>
                </header>
                <NoteCardGrid notes={examNotes} category={activeExam?.examName || "AI 학습노트"} onCreate={createNote} onOpen={openNote} onRemove={removeNote} deletingNoteId={deletingNoteId} />
                {message && <p className="learning-home-error" role="alert">{message}</p>}
            </div>
        </div>
    );
}
