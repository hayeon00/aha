import { useCallback, useEffect, useState } from "react";

import { getOwnedLearningDocuments } from "../api/learningDocumentApi.js";
import { getApiData } from "../utils/apiResponseUtils.js";

export function useLearningDocumentWorkspace({ userExamId, learningNoteId, enabled }) {
    const [documents, setDocuments] = useState([]);
    const [documentId, setDocumentId] = useState(null);
    const [loading, setLoading] = useState(false);
    const batchGenerating = false;
    const [error, setError] = useState("");

    const refresh = useCallback(async () => {
        if (!enabled || !userExamId) {
            setDocuments([]);
            setDocumentId(null);
            return;
        }

        try {
            setLoading(true);
            setError("");
            const response = await getOwnedLearningDocuments(userExamId);
            const items = (getApiData(response) || []).filter(
                (item) => item.learningNoteId === learningNoteId,
            );
            setDocuments(items);
            setDocumentId((current) => (
                items.some((item) => item.documentId === current)
                    ? current
                    : items[0]?.documentId ?? null
            ));
        } catch (requestError) {
            setError(
                requestError.response?.data?.message
                || "문서 목록을 불러오지 못했습니다.",
            );
        } finally {
            setLoading(false);
        }
    }, [enabled, learningNoteId, userExamId]);

    useEffect(() => {
        queueMicrotask(refresh);
    }, [refresh]);

    const createNote = useCallback(async (targetDocumentId = documentId) => {
        if (!targetDocumentId || batchGenerating) return null;

        const targetDocument = documents.find(
            (item) => item.documentId === targetDocumentId,
        );
        if (!targetDocument?.learningNoteId) {
            setError("연결된 학습노트를 찾지 못했습니다.");
            return null;
        }
        return { id: targetDocument.learningNoteId };
    }, [batchGenerating, documentId, documents]);

    return {
        documents,
        documentId,
        loading,
        error,
        batchGenerating,
        selectDocument: setDocumentId,
        createNote,
        refresh,
    };
}
