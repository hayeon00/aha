import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

import NoteCardGrid from "../components/NoteCardGrid.jsx";
import { getExams } from "../../exam/api/examApi.js";
import { addUserExams, completeExamOnboarding, getUserExams } from "../../exam/api/userExamApi.js";
import { deleteLearningNote, getCompletedLearningNotes } from "../../ailearn/api/learningNoteApi.js";
import { getApiData } from "../../ailearn/utils/apiResponseUtils.js";
import LoginModal from "../../auth/components/LoginModal.jsx";
import { getMyInfo } from "../../user/api/userApi.js";

import "./MainPage.css";

const examVisuals = [
    { icon: "SQL", tone: "orange", category: "데이터 · 개발" },
    { icon: "IT", tone: "indigo", category: "IT · 소프트웨어" },
    { icon: "AD", tone: "cyan", category: "데이터 분석" },
    { icon: "史", tone: "emerald", category: "인문 · 역사" },
    { icon: "₩", tone: "violet", category: "경영 · 회계" },
];

function getExamVisual(exam, index) {
    const value = `${exam.code || ""} ${exam.name || ""}`.toLowerCase();
    if (value.includes("sql") || value.includes("데이터")) return examVisuals[0];
    if (value.includes("정보") || value.includes("it")) return examVisuals[1];
    if (value.includes("adsp") || value.includes("분석")) return examVisuals[2];
    if (value.includes("한국사") || value.includes("역사")) return examVisuals[3];
    if (value.includes("회계") || value.includes("재무")) return examVisuals[4];
    return examVisuals[index % examVisuals.length];
}

function MainPage({ isLoggedIn, onLoginSuccess, onLogout }) {
    const navigate = useNavigate();
    const examSectionRef = useRef(null);
    const [exams, setExams] = useState([]);
    const [userExams, setUserExams] = useState([]);
    const [notes, setNotes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState("");
    const [startingExamId, setStartingExamId] = useState(null);
    const [deletingNoteId, setDeletingNoteId] = useState(null);
    const [enteringExam, setEnteringExam] = useState(null);
    const [loginOpen, setLoginOpen] = useState(false);
    const [accountOpen, setAccountOpen] = useState(false);
    const [userInfo, setUserInfo] = useState(null);
    const accountRef = useRef(null);
    const pendingExamIdRef = useRef(sessionStorage.getItem("pendingExamId"));

    useEffect(() => {
        let cancelled = false;
        const requests = isLoggedIn
            ? Promise.all([getExams(), getUserExams(), getCompletedLearningNotes(), getMyInfo()])
            : Promise.all([getExams(), Promise.resolve(null), Promise.resolve(null), Promise.resolve(null)]);
        requests
            .then(([examResponse, userExamResponse, noteResponse, userResponse]) => {
                if (cancelled) return;
                setExams((getApiData(examResponse) || []).filter((exam) => exam.activeVersionId));
                setUserExams(getApiData(userExamResponse) || []);
                setNotes(getApiData(noteResponse) || []);
                setUserInfo(getApiData(userResponse));
            })
            .catch((error) => {
                if (!cancelled) setMessage(error.response?.data?.message || "학습 공간을 불러오지 못했습니다.");
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });
        return () => { cancelled = true; };
    }, [isLoggedIn]);

    useEffect(() => {
        const closeAccountMenu = (event) => {
            if (accountRef.current && !accountRef.current.contains(event.target)) setAccountOpen(false);
        };
        document.addEventListener("mousedown", closeAccountMenu);
        return () => document.removeEventListener("mousedown", closeAccountMenu);
    }, []);

    const startLearning = async (exam) => {
        if (startingExamId) return;
        if (!isLoggedIn) {
            sessionStorage.setItem("pendingExamId", String(exam.id));
            pendingExamIdRef.current = String(exam.id);
            setLoginOpen(true);
            return;
        }
        try {
            const transitionStartedAt = performance.now();
            setStartingExamId(exam.id);
            setEnteringExam(exam);
            setMessage("");
            let userExam = userExams.find((item) => item.examId === exam.id);
            if (!userExam) {
                const response = userExams.length === 0
                    ? await completeExamOnboarding([exam.id])
                    : await addUserExams([exam.id]);
                const addedExams = getApiData(response) || [];
                userExam = addedExams.find((item) => item.examId === exam.id);
                setUserExams((current) => [...current, ...addedExams]);
            }
            if (!userExam?.userExamId) throw new Error("학습 시험을 준비하지 못했습니다.");
            sessionStorage.setItem("activeUserExamId", String(userExam.userExamId));
            sessionStorage.setItem("activeExamName", userExam.examName || exam.name || "자격증 학습");
            const remainingTransition = Math.max(0, 1500 - (performance.now() - transitionStartedAt));
            await new Promise((resolve) => window.setTimeout(resolve, remainingTransition));
            navigate(`/learning-home?userExamId=${userExam.userExamId}`);
        } catch (error) {
            setMessage(error.response?.data?.message || error.message || "학습 화면을 준비하지 못했습니다.");
            setStartingExamId(null);
            setEnteringExam(null);
        }
    };

    useEffect(() => {
        const pendingExamId = pendingExamIdRef.current;
        if (!isLoggedIn || !pendingExamId || loading || exams.length === 0) return;
        const exam = exams.find((item) => String(item.id) === String(pendingExamId));
        sessionStorage.removeItem("pendingExamId");
        pendingExamIdRef.current = null;
        if (exam) startLearning(exam);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isLoggedIn, loading, exams]);

    const removeNote = async (noteId) => {
        const note = notes.find((item) => item.id === noteId);
        if (!window.confirm(`'${note?.title || "선택한 학습노트"}'을 삭제할까요?\n삭제된 노트는 복구할 수 없습니다.`)) return;
        try {
            setDeletingNoteId(noteId);
            await deleteLearningNote(noteId);
            setNotes((current) => current.filter((item) => item.id !== noteId));
        } catch (error) {
            setMessage(error.response?.data?.message || "학습노트를 삭제하지 못했습니다.");
        } finally {
            setDeletingNoteId(null);
        }
    };

    return (
        <div className="public-home-shell">
            <header className="public-home-header">
                <button type="button" className="public-brand" onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}><img src="/brand/aha-mark.png" alt="" /><strong>Aha</strong></button>
                <nav aria-label="메인 메뉴"><button type="button" onClick={() => examSectionRef.current?.scrollIntoView({ behavior: "smooth" })}>지원 자격증</button><button type="button" onClick={() => document.querySelector(".how-it-works")?.scrollIntoView({ behavior: "smooth" })}>이용 방법</button></nav>
                {isLoggedIn ? (
                    <div className="public-profile" ref={accountRef}>
                        <button type="button" className={accountOpen ? "public-profile-trigger active" : "public-profile-trigger"} onClick={() => setAccountOpen((current) => !current)} aria-expanded={accountOpen}>
                            <span>{String(userInfo?.nickname || userInfo?.name || "A").charAt(0).toUpperCase()}</span>
                            <strong>{userInfo?.nickname || userInfo?.name || "내 계정"}</strong>
                            <i>⌄</i>
                        </button>
                        {accountOpen && <div className="public-profile-menu"><header><small>내 계정</small><strong>{userInfo?.email || userInfo?.nickname || "Aha 학습자"}</strong></header><button type="button" onClick={() => navigate("/mypage")}>마이페이지 <b>›</b></button><button type="button" className="logout" onClick={() => { setAccountOpen(false); onLogout(); }}>로그아웃</button></div>}
                    </div>
                ) : <button type="button" className="public-login" onClick={() => setLoginOpen(true)}>로그인</button>}
            </header>
            <main className="discovery-page">
            <section className="discovery-hero">
                <div className="hero-copy">
                    <span className="hero-kicker"><SparkleIcon /> SMART CERTIFICATE LEARNING</span>
                    <h1>합격을 향한 공부,<br /><em>Aha에서 더 선명하게.</em></h1>
                    <p>자격증 선택부터 개념 학습, 문제 풀이와 스터디까지.<br />합격에 필요한 모든 학습 흐름을 하나의 공간에서 이어가세요.</p>
                    <div className="hero-actions">
                        <button type="button" className="hero-primary" onClick={() => examSectionRef.current?.scrollIntoView({ behavior: "smooth" })}>지원 자격증 둘러보기 <ArrowIcon /></button>
                        {notes.length > 0 && <button type="button" className="hero-secondary" onClick={() => document.querySelector("#my-notes")?.scrollIntoView({ behavior: "smooth" })}>내 학습노트 보기</button>}
                    </div>
                    <div className="hero-proof">
                        <span><CheckIcon /> 맞춤 학습 공간</span>
                        <span><CheckIcon /> AI 학습 도구</span>
                        <span><CheckIcon /> 문제 풀이와 스터디</span>
                    </div>
                </div>
                <HeroPreview />
            </section>

            <section className="exam-discovery" ref={examSectionRef}>
                <header className="section-heading">
                    <div><span>SUPPORTED CERTIFICATES</span><h2>어떤 자격증을 준비하시나요?</h2><p>원하는 자격증을 선택하면 해당 시험만을 위한 전용 학습 공간이 열립니다.</p></div>
                    <b>{exams.length}개 자격증 지원</b>
                </header>

                {loading ? (
                    <div className="exam-catalog skeleton-catalog">{[0, 1, 2, 3].map((item) => <div key={item} />)}</div>
                ) : (
                    <div className="exam-catalog">
                        {exams.map((exam, index) => {
                            const visual = getExamVisual(exam, index);
                            const registered = userExams.some((item) => item.examId === exam.id);
                            return (
                                <article className="certificate-card" key={exam.id}>
                                    <div className={`certificate-icon ${visual.tone}`}>{visual.icon}</div>
                                    <div className="certificate-meta"><span>{visual.category}</span>{registered && <b>학습 중</b>}</div>
                                    <h3>{exam.name}</h3>
                                    <p>{exam.versionName || "최신 출제 기준"}</p>
                                    <div className="certificate-features"><span>AI 학습노트</span><span>시험 목차 연동</span></div>
                                    <button type="button" disabled={Boolean(startingExamId)} onClick={() => startLearning(exam)}>
                                        {startingExamId === exam.id ? "학습 공간 준비 중..." : registered ? "이어서 학습하기" : "학습하기"}<ArrowIcon />
                                    </button>
                                </article>
                            );
                        })}
                    </div>
                )}
                {message && <p className="discovery-error" role="alert">{message}</p>}
            </section>

            <section className="how-it-works">
                <div className="section-heading"><div><span>HOW IT WORKS</span><h2>문서 한 장이 학습노트가 되는 과정</h2></div></div>
                <div className="process-grid">
                    <article><b>01</b><FileIcon /><h3>자료 업로드</h3><p>PDF 또는 DOCX 학습 자료를 간편하게 올려주세요.</p></article>
                    <article><b>02</b><ScopeIcon /><h3>시험 목차 분석</h3><p>AI가 교안과 시험 출제 목차를 연결해 핵심을 찾습니다.</p></article>
                    <article><b>03</b><NoteIcon /><h3>맞춤 노트 완성</h3><p>단원별 개념 설명이 담긴 나만의 합격 노트를 확인하세요.</p></article>
                </div>
            </section>

            {notes.length > 0 && (
                <section className="my-notes-section" id="my-notes">
                    <div className="section-heading"><div><span>MY LIBRARY</span><h2>최근 학습노트</h2><p>만들어 둔 노트에서 바로 학습을 이어가세요.</p></div></div>
                    <NoteCardGrid notes={notes} onOpen={(note) => {
                        if (note.userExamId) sessionStorage.setItem("activeUserExamId", String(note.userExamId));
                        if (note.examName) sessionStorage.setItem("activeExamName", note.examName);
                        navigate(`/learning?noteId=${note.id}${note.userExamId ? `&userExamId=${note.userExamId}` : ""}`);
                    }} onRemove={removeNote} deletingNoteId={deletingNoteId} />
                </section>
            )}
            </main>
            <footer className="public-home-footer"><div className="public-brand"><img src="/brand/aha-mark.png" alt="" /><strong>Aha</strong></div><p>AI와 함께 더 선명해지는 합격의 순간.</p></footer>
            <LoginModal open={loginOpen} onClose={() => setLoginOpen(false)} onLoginSuccess={onLoginSuccess} onMoveSignup={() => navigate("/signup")} />
            {enteringExam && <div className="learning-entry-backdrop" role="status" aria-live="polite"><div className="learning-entry-card"><div className="learning-entry-orbit"><i /><span>{getExamVisual(enteringExam, 0).icon}</span></div><small><b />LEARNING WORKSPACE</small><h2>{enteringExam.name}</h2><p>학습 기록과 AI 노트를 불러오고 있어요</p><div className="learning-entry-progress"><i /></div><div className="learning-entry-meta"><span>전용 학습 공간 준비 중</span><b>•••</b></div></div></div>}
        </div>
    );
}

function HeroPreview() {
    return <div className="hero-visual" aria-hidden="true"><div className="preview-glow" /><div className="preview-window platform-preview"><div className="preview-bar"><i /><i /><i /><span>Aha Learning Workspace</span></div><div className="platform-dashboard"><header><div><small>MY LEARNING</small><h3>오늘의 학습</h3></div><b>SQLD</b></header><section className="journey-card"><div><span>합격 여정</span><strong>68%</strong></div><p>이번 주 목표까지 조금만 더 힘내세요</p><i><b /></i></section><div className="feature-preview-grid"><article><span>✦</span><small>AI 학습노트</small><strong>핵심 노트 6개</strong><i>이어서 학습 →</i></article><article><span>✓</span><small>기출 문제</small><strong>오늘 20문제</strong><i>문제 풀기 →</i></article><article><span>◎</span><small>함께 학습</small><strong>스터디 3회</strong><i>일정 보기 →</i></article><article><span>↗</span><small>학습 리포트</small><strong>이번 주 +12%</strong><i>리포트 보기 →</i></article></div></div></div><span className="float-chip chip-one">✦ 오늘의 학습 루틴</span><span className="float-chip chip-two">✓ 주간 목표 달성 중</span></div>;
}

const Icon = ({ children }) => <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">{children}</svg>;
const SparkleIcon = () => <Icon><path d="M12 2.8c.8 4.4 2.8 6.5 7.2 7.2-4.4.8-6.5 2.8-7.2 7.2-.8-4.4-2.8-6.5-7.2-7.2 4.4-.8 6.5-2.8 7.2-7.2Z" fill="currentColor" /></Icon>;
const ArrowIcon = () => <Icon><path d="M5 12h14m-5-5 5 5-5 5" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const CheckIcon = () => <Icon><path d="m5 12 4 4 10-10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></Icon>;
const FileIcon = () => <Icon><path d="M6 3.5h8l4 4V21H6V3.5Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round" /><path d="M14 3.5v4h4" stroke="currentColor" strokeWidth="1.7" /></Icon>;
const ScopeIcon = () => <Icon><circle cx="12" cy="12" r="8" stroke="currentColor" strokeWidth="1.7" /><circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="1.7" /></Icon>;
const NoteIcon = () => <Icon><path d="M5 4h14v16H5z" stroke="currentColor" strokeWidth="1.7" /><path d="M8 9h8M8 13h6" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" /></Icon>;

export default MainPage;
