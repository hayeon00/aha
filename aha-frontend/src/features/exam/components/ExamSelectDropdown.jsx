import { useEffect, useRef, useState } from "react";
import "./ExamSelectDropdown.css";

function ExamSelectDropdown({
    exams,
    selectedExamId,
    onChange,
    loading = false,
    ariaLabel = "시험 선택",
}) {
    const dropdownRef = useRef(null);
    const [isOpen, setIsOpen] = useState(false);
    const selectedExam = exams.find(
        (exam) => exam.userExamId === selectedExamId
    );

    useEffect(() => {
        if (!isOpen) {
            return undefined;
        }

        const closeOnOutsideClick = (event) => {
            if (!dropdownRef.current?.contains(event.target)) {
                setIsOpen(false);
            }
        };
        const closeOnEscape = (event) => {
            if (event.key === "Escape") {
                setIsOpen(false);
            }
        };

        document.addEventListener("mousedown", closeOnOutsideClick);
        document.addEventListener("keydown", closeOnEscape);

        return () => {
            document.removeEventListener("mousedown", closeOnOutsideClick);
            document.removeEventListener("keydown", closeOnEscape);
        };
    }, [isOpen]);

    const handleSelect = (exam) => {
        setIsOpen(false);
        onChange(exam);
    };

    return (
        <div className="exam-dropdown-wrap" ref={dropdownRef}>
            <button
                type="button"
                className={`exam-select-trigger ${isOpen ? "is-open" : ""}`}
                onClick={() => setIsOpen((current) => !current)}
                disabled={loading || exams.length === 0}
                aria-expanded={isOpen}
                aria-haspopup="listbox"
                aria-label={ariaLabel}
            >
                <span>
                    {loading
                        ? "불러오는 중"
                        : selectedExam?.examCode ?? "활성 시험 없음"}
                </span>
                <svg
                    className="exam-select-arrow"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    aria-hidden="true"
                >
                    <path
                        d="m6 9 6 6 6-6"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                </svg>
            </button>

            {isOpen && (
                <div
                    className="exam-select-menu"
                    role="listbox"
                    aria-label={`${ariaLabel} 목록`}
                >
                    {exams.map((exam) => {
                        const isSelected =
                            exam.userExamId === selectedExamId;

                        return (
                            <button
                                type="button"
                                role="option"
                                aria-selected={isSelected}
                                className={isSelected ? "is-selected" : ""}
                                key={exam.userExamId}
                                onClick={() => handleSelect(exam)}
                            >
                                <span>{exam.examCode}</span>
                                {isSelected && (
                                    <span
                                        className="exam-option-check"
                                        aria-hidden="true"
                                    >
                                        ✓
                                    </span>
                                )}
                            </button>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

export default ExamSelectDropdown;
