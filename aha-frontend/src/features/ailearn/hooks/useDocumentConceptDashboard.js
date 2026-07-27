import { useCallback, useEffect, useState } from "react";
import {
    createDocumentLearningNote,
    generateDocumentConcept,
    getDocumentConceptDashboard,
    getOwnedDocuments,
    updateDocumentConcept,
} from "../api/documentConceptApi.js";
import { getApiData } from "../utils/apiResponseUtils.js";

export function useDocumentConceptDashboard({ userExamId, enabled }) {
    const [documents, setDocuments] = useState([]);
    const [documentId, setDocumentId] = useState(null);
    const [dashboard, setDashboard] = useState(null);
    const [loading, setLoading] = useState(false);
    const [generatingIds, setGeneratingIds] = useState([]);
    const [batchGenerating, setBatchGenerating] = useState(false);
    const [savingIds, setSavingIds] = useState([]);
    const [error, setError] = useState("");

    const loadDashboard = useCallback(async (nextDocumentId) => {
        if (!nextDocumentId) return setDashboard(null);
        try {
            setLoading(true);
            setError("");
            const response = await getDocumentConceptDashboard(nextDocumentId);
            setDashboard(getApiData(response));
        } catch (requestError) {
            setDashboard(null);
            setError(requestError.response?.data?.message || "문서 학습방을 불러오지 못했습니다.");
        } finally {
            setLoading(false);
        }
    }, []);

    const loadDocuments = useCallback(async () => {
        if (!enabled || !userExamId) {
            setDocuments([]); setDocumentId(null); setDashboard(null); return;
        }
        try {
            setLoading(true);
            const response = await getOwnedDocuments(userExamId);
            const items = getApiData(response) || [];
            const firstId = items[0]?.documentId ?? null;
            setDocuments(items);
            setDocumentId(firstId);
            await loadDashboard(firstId);
        } catch (requestError) {
            setError(requestError.response?.data?.message || "문서 목록을 불러오지 못했습니다.");
        } finally {
            setLoading(false);
        }
    }, [enabled, userExamId, loadDashboard]);

    useEffect(() => { queueMicrotask(loadDocuments); }, [loadDocuments]);

    const selectDocument = useCallback((nextId) => {
        setDocumentId(nextId);
        loadDashboard(nextId);
    }, [loadDashboard]);

    const generateOne = useCallback(async (tocId, prompt) => {
        if (!documentId || generatingIds.includes(tocId)) return;
        try {
            setGeneratingIds((ids) => [...ids, tocId]);
            setError("");
            const response = await generateDocumentConcept(documentId, tocId, prompt);
            await loadDashboard(documentId);
            return getApiData(response);
        } catch (requestError) {
            setError(requestError.response?.data?.message || "AI 보완 설명 생성에 실패했습니다.");
        } finally {
            setGeneratingIds((ids) => ids.filter((id) => id !== tocId));
        }
    }, [documentId, generatingIds, loadDashboard]);

    const generateAll = useCallback(async (targetDocumentId = documentId) => {
        if (!targetDocumentId || batchGenerating) return false;
        try {
            setBatchGenerating(true);
            setError("");
            await createDocumentLearningNote(targetDocumentId);
            await loadDashboard(targetDocumentId);
            return true;
        } catch (requestError) {
            setError(requestError.response?.data?.message || "학습노트를 만들지 못했습니다.");
            return false;
        } finally {
            setBatchGenerating(false);
        }
    }, [documentId, batchGenerating, loadDashboard]);

    const saveOne = useCallback(async (tocId, content) => {
        if (!documentId || savingIds.includes(tocId)) return null;
        try {
            setSavingIds((ids) => [...ids, tocId]);
            setError("");
            const response = await updateDocumentConcept(documentId, tocId, content);
            await loadDashboard(documentId);
            return getApiData(response);
        } catch (requestError) {
            setError(requestError.response?.data?.message || "개념 설명 저장에 실패했습니다.");
            return null;
        } finally {
            setSavingIds((ids) => ids.filter((id) => id !== tocId));
        }
    }, [documentId, savingIds, loadDashboard]);

    return { documents, documentId, dashboard, loading, error, generatingIds,
        batchGenerating, savingIds, selectDocument, generateOne, generateAll,
        saveOne, refresh: loadDocuments };
}
