import { useEffect, useRef, useState } from "react";

export default function EditableTitle({ title, onSave }) {
    const [editing, setEditing] = useState(false);
    const [value, setValue] = useState(title);
    const inputRef = useRef(null);

    useEffect(() => {
        if (editing) {
            inputRef.current?.focus();
            inputRef.current?.select();
        }
    }, [editing]);

    const commit = async () => {
        const nextTitle = value.trim() || title;
        setEditing(false);
        if (nextTitle !== title) await onSave(nextTitle);
    };

    if (editing) {
        return (
            <input
                ref={inputRef}
                className="editable-note-title-input"
                value={value}
                onChange={(event) => setValue(event.target.value)}
                onBlur={commit}
                onKeyDown={(event) => {
                    if (event.key === "Enter") event.currentTarget.blur();
                    if (event.key === "Escape") {
                        setValue(title);
                        setEditing(false);
                    }
                }}
                aria-label="학습노트 제목"
            />
        );
    }

    return (
        <button
            type="button"
            className="editable-note-title"
            title="클릭하여 학습노트 제목 수정"
            onClick={() => {
                setValue(title);
                setEditing(true);
            }}
        >
            <strong>{title}</strong>
            <span className="editable-title-pencil" aria-hidden="true">✏️</span>
        </button>
    );
}
