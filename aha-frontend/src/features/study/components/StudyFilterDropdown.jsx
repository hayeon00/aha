import { useEffect, useRef, useState } from "react";
import "./StudyFilterDropdown.css";

function StudyFilterDropdown({
    label,
    value,
    options,
    onChange,
}) {
    const dropdownRef = useRef(null);
    const [isOpen, setIsOpen] = useState(false);
    const selectedOption =
        options.find((option) => option.value === value) || options[0];

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

    const handleSelect = (nextValue) => {
        onChange(nextValue);
        setIsOpen(false);
    };

    return (
        <div className="study-filter-dropdown" ref={dropdownRef}>
            <button
                type="button"
                className={`study-filter-trigger ${
                    isOpen ? "is-open" : ""
                }`}
                aria-haspopup="listbox"
                aria-expanded={isOpen}
                onClick={() => setIsOpen((current) => !current)}
            >
                <span>
                    <strong>{label}:</strong> {selectedOption?.label}
                </span>
                <span
                    className="study-filter-arrow"
                    aria-hidden="true"
                >
                    ▾
                </span>
            </button>

            {isOpen && (
                <div
                    className="study-filter-menu"
                    role="listbox"
                    aria-label={`${label} 선택`}
                >
                    {options.map((option) => {
                        const isSelected = option.value === value;

                        return (
                            <button
                                type="button"
                                role="option"
                                aria-selected={isSelected}
                                className={
                                    isSelected ? "is-selected" : ""
                                }
                                key={option.value}
                                onClick={() =>
                                    handleSelect(option.value)
                                }
                            >
                                {option.label}
                            </button>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

export default StudyFilterDropdown;
