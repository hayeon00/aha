import { useCallback, useEffect, useState } from "react";
import {
    generateDocumentConcept,
    generateMissingDocumentConcepts,
    getDocumentConceptDashboard,
    getOwnedDocuments,
} from "../api/documentConceptApi.js";
import { getApiData } from "../utils/apiResponseUtils.js";

export function useDocumentConceptDashboard({ userExamId, enabled }) {
    const [documents, setDocuments] = useState([]);
    const [documentId, setDocumentId] = useState(null);
    const [dashboard, setDashboard] = useState(null);
    const [loading, setLoading] = useState(false);
    const [generatingIds, setGeneratingIds] = useState([]);
    const [batchGenerating, setBatchGenerating] = useState(false);
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

    const generateOne = useCallback(async (tocId) => {
        if (!documentId || generatingIds.includes(tocId)) return;
        try {
            setGeneratingIds((ids) => [...ids, tocId]);
            setError("");
            await generateDocumentConcept(documentId, tocId);
            await loadDashboard(documentId);
        } catch (requestError) {
            setError(requestError.response?.data?.message || "AI 보완 설명 생성에 실패했습니다.");
        } finally {
            setGeneratingIds((ids) => ids.filter((id) => id !== tocId));
        }
    }, [documentId, generatingIds, loadDashboard]);

    const generateAll = useCallback(async () => {
        if (!documentId || batchGenerating) return;
        try {
            setBatchGenerating(true);
            setError("");
            await generateMissingDocumentConcepts(documentId);
            await loadDashboard(documentId);
        } catch (requestError) {
            setError(requestError.response?.data?.message || "일괄 생성 중 일부 작업에 실패했습니다.");
        } finally {
            setBatchGenerating(false);
        }
    }, [documentId, batchGenerating, loadDashboard]);

    return { documents, documentId, dashboard, loading, error, generatingIds,
        batchGenerating, selectDocument, generateOne, generateAll, refresh: loadDocuments };
}
