export const isRecord = (value) => value !== null && typeof value === "object" && !Array.isArray(value);
export const contentText = (value) => typeof value === "string" || typeof value === "number" ? String(value) : "";

export function parseConceptBody(body) {
    try {
        const parsed = JSON.parse(body);
        if (!isRecord(parsed) || !Array.isArray(parsed.blocks)) return null;
        return parsed.blocks.filter((block) => isRecord(block) && (
            ((block.type === "definition" || block.type === "exam_tip") && contentText(block.text)) ||
            (block.type === "example" && (contentText(block.text) || contentText(block.sql) || block.result != null))
        ));
    } catch {
        return null;
    }
}

export function resultTable(result) {
    if (!Array.isArray(result) || !result.length || !result.every(isRecord)) return null;
    const columns = [...new Set(result.flatMap((row) => Object.keys(row)))];
    return columns.length ? { columns, rows: result } : null;
}

export const formatCell = (value) => value === null ? "NULL" : value === undefined ? "—" : typeof value === "object" ? JSON.stringify(value) : String(value);
