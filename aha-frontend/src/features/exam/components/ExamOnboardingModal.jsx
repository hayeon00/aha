import { useEffect, useMemo, useRef, useState } from "react";
import { getExams } from "../api/examApi.js";
import { completeExamOnboarding } from "../api/userExamApi.js";
import "./ExamOnboardingModal.css";

const getApiData = (response) => response?.data?.data ?? response?.data ?? response ?? [];

const examMeta = (exam, index) => {
    const value = `${exam.code ?? ""} ${exam.name ?? ""}`.toLowerCase();
    if (value.includes("정보") || value.includes("it")) return { icon: "⌨", category: "IT · 개발" };
    if (value.includes("데이터") || value.includes("sql")) return { icon: "▥", category: "데이터" };
    if (value.includes("한국사")) return { icon: "▤", category: "인문 · 역사" };
    if (value.includes("회계") || value.includes("재무")) return { icon: "₩", category: "경영 · 회계" };
    if (value.includes("영어") || value.includes("toeic")) return { icon: "A", category: "어학" };
    const fallbacks = [
        { icon: "✓", category: "국가 자격" },
        { icon: "✦", category: "전문 자격" },
        { icon: "◎", category: "자격 시험" },
    ];
    return fallbacks[index % fallbacks.length];
};

export default function ExamOnboardingModal({ open, onComplete }) {
    const [exams, setExams] = useState([]);
    const [selectedIds, setSelectedIds] = useState([]);
    const [status, setStatus] = useState("loading");
    const [message, setMessage] = useState("");
    const [centeredCarouselIndex, setCenteredCarouselIndex] = useState(1);
    const carouselRef = useRef(null);
    const dragStateRef = useRef({ active: false, startX: 0, scrollLeft: 0, moved: false });

    const loadExams = async () => {
        try {
            setStatus("loading");
            setMessage("");
            const response = await getExams();
            const data = getApiData(response);
            const availableExams = Array.isArray(data) ? data.filter((exam) => exam.activeVersionId) : [];
            setExams(availableExams);
            setCenteredCarouselIndex(availableExams.length > 1 ? 1 : 0);
            setStatus("ready");
        } catch (error) {
            console.error("시험 목록 조회 실패:", error);
            setMessage("시험 목록을 불러오지 못했어요. 잠시 후 다시 시도해주세요.");
            setStatus("error");
        }
    };

    useEffect(() => {
        if (open) {
            queueMicrotask(() => loadExams());
        }
    }, [open]);

    useEffect(() => {
        if (exams.length < 2) return;
        requestAnimationFrame(() => {
            const firstRealCard = carouselRef.current?.querySelector('[data-carousel-index="1"]');
            firstRealCard?.scrollIntoView({ behavior: "auto", block: "nearest", inline: "center" });
        });
    }, [exams]);

    const selectedCount = selectedIds.length;
    const carouselExams = useMemo(() => (
        exams.length > 1 ? [exams.at(-1), ...exams, exams[0]] : exams
    ), [exams]);
    const buttonLabel = useMemo(() => (
        status === "saving" ? "학습 공간을 준비하고 있어요…" : "선택한 시험으로 시작하기"
    ), [status]);

    if (!open) return null;

    const toggleExam = (examId) => {
        if (dragStateRef.current.moved) {
            dragStateRef.current.moved = false;
            return;
        }
        setSelectedIds((current) => current.includes(examId)
            ? current.filter((id) => id !== examId)
            : [...current, examId]);
        setMessage("");
    };

    const handleDragStart = (event) => {
        const carousel = carouselRef.current;
        if (!carousel) return;
        dragStateRef.current = {
            active: true,
            startX: event.clientX,
            scrollLeft: carousel.scrollLeft,
            moved: false,
        };
    };

    const handleDragMove = (event) => {
        const carousel = carouselRef.current;
        const drag = dragStateRef.current;
        if (!carousel || !drag.active) return;
        const distance = event.clientX - drag.startX;
        if (Math.abs(distance) > 5 && !drag.moved) {
            drag.moved = true;
            carousel.setPointerCapture(event.pointerId);
            carousel.classList.add("is-dragging");
        }
        if (!drag.moved) return;
        carousel.scrollLeft = drag.scrollLeft - distance;
    };

    const handleDragEnd = (event) => {
        const carousel = carouselRef.current;
        if (!carousel) return;
        dragStateRef.current.active = false;
        if (carousel.hasPointerCapture(event.pointerId)) {
            carousel.releasePointerCapture(event.pointerId);
        }
        carousel.classList.remove("is-dragging");
        if (dragStateRef.current.moved) {
            const carouselCenter = carousel.getBoundingClientRect().left + carousel.clientWidth / 2;
            const cards = [...carousel.querySelectorAll(".exam-select-card")];
            const nearestCard = cards.reduce((nearest, card) => {
                const cardCenter = card.getBoundingClientRect().left + card.clientWidth / 2;
                const distance = Math.abs(carouselCenter - cardCenter);
                return !nearest || distance < nearest.distance ? { card, distance } : nearest;
            }, null)?.card;
            const carouselIndex = Number(nearestCard?.dataset.carouselIndex);
            if (Number.isInteger(carouselIndex)) setCenteredCarouselIndex(carouselIndex);
            nearestCard?.scrollIntoView({ behavior: "smooth", block: "nearest", inline: "center" });

            if (exams.length > 1 && (carouselIndex === 0 || carouselIndex === exams.length + 1)) {
                const normalizedIndex = carouselIndex === 0 ? exams.length : 1;
                window.setTimeout(() => {
                    const normalizedCard = carousel.querySelector(`[data-carousel-index="${normalizedIndex}"]`);
                    normalizedCard?.scrollIntoView({ behavior: "auto", block: "nearest", inline: "center" });
                    setCenteredCarouselIndex(normalizedIndex);
                }, 260);
            }
        }
    };

    const handleCarouselScroll = () => {
        const carousel = carouselRef.current;
        if (!carousel) return;
        const carouselCenter = carousel.getBoundingClientRect().left + carousel.clientWidth / 2;
        const cards = [...carousel.querySelectorAll(".exam-select-card")];
        const nearestCard = cards.reduce((nearest, card) => {
            const cardCenter = card.getBoundingClientRect().left + card.clientWidth / 2;
            const distance = Math.abs(carouselCenter - cardCenter);
            return !nearest || distance < nearest.distance ? { card, distance } : nearest;
        }, null);
        const carouselIndex = Number(nearestCard?.card.dataset.carouselIndex);
        if (Number.isInteger(carouselIndex)) setCenteredCarouselIndex(carouselIndex);
    };

    const handleSubmit = async () => {
        if (!selectedCount || status === "saving") return;
        try {
            setStatus("saving");
            setMessage("");
            await completeExamOnboarding(selectedIds);
            onComplete?.(selectedIds);
        } catch (error) {
            console.error("학습 목표 시험 저장 실패:", error);
            setMessage(error.response?.data?.message ?? "선택 내용을 저장하지 못했어요. 다시 시도해주세요.");
            setStatus("ready");
        }
    };

    return (
        <div className="exam-onboarding-backdrop">
            <section className="exam-onboarding-modal" role="dialog" aria-modal="true" aria-labelledby="exam-onboarding-title">
                <div className="exam-onboarding-accent" aria-hidden="true" />
                <header className="exam-onboarding-header">
                    <h1 id="exam-onboarding-title">어떤 <span>시험</span>을 준비할까요?</h1>
                </header>

                <div className="exam-onboarding-content">
                    {status === "loading" ? (
                        <div className="exam-card-carousel is-loading" aria-label="시험 목록을 불러오는 중">
                            {[0, 1, 2, 3].map((item) => <div className="exam-card-skeleton" key={item} />)}
                        </div>
                    ) : status === "error" ? (
                        <div className="exam-load-error"><p>{message}</p><button type="button" onClick={loadExams}>다시 불러오기</button></div>
                    ) : exams.length === 0 ? (
                        <div className="exam-load-error"><p>현재 선택할 수 있는 시험이 없습니다.</p></div>
                    ) : (
                        <div
                            className="exam-card-carousel"
                            ref={carouselRef}
                            onPointerDown={handleDragStart}
                            onPointerMove={handleDragMove}
                            onPointerUp={handleDragEnd}
                            onPointerCancel={handleDragEnd}
                            onScroll={handleCarouselScroll}
                            aria-label="지원 시험 목록. 좌우로 드래그하여 탐색할 수 있습니다."
                        >
                            {carouselExams.map((exam, carouselIndex) => {
                                const originalIndex = exams.findIndex((item) => item.id === exam.id);
                                const selected = selectedIds.includes(exam.id);
                                const meta = examMeta(exam, originalIndex);
                                return (
                                    <button
                                        type="button"
                                        key={`${exam.id}-${carouselIndex}`}
                                        data-exam-id={exam.id}
                                        data-carousel-index={carouselIndex}
                                        className={`exam-select-card${selected ? " is-selected" : ""}${centeredCarouselIndex === carouselIndex ? " is-centered" : ""}`}
                                        onClick={() => toggleExam(exam.id)}
                                        aria-pressed={selected}
                                    >
                                        <span className="exam-card-icon" aria-hidden="true">{meta.icon}</span>
                                        <span className="exam-card-copy"><strong>{exam.name}</strong><small>{meta.category}</small></span>
                                        <span className="exam-card-check" aria-hidden="true">✓</span>
                                    </button>
                                );
                            })}
                        </div>
                    )}
                    {message && status !== "error" && <p className="exam-save-error" role="alert">{message}</p>}
                </div>

                <footer className="exam-onboarding-footer">
                    <button type="button" className="exam-start-button" disabled={!selectedCount || status === "saving"} onClick={handleSubmit}>
                        <span>{buttonLabel}</span><i className="exam-start-arrow" aria-hidden="true" />
                    </button>
                </footer>
            </section>
        </div>
    );
}
