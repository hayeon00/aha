import { useCallback, useEffect, useState } from "react";
import {
    getMappedDocumentChunks,
    getUserLearningContent,
} from "../api/learningContentApi.js";
import { getApiData } from "../utils/apiResponseUtils.js";

export const useLearningContent = ({
                                       userExamId,
                                       examScopeNodeId,
                                       enabled = true,
                                   }) => {
    const [learningContent, setLearningContent] = useState(null);
    const [mappedDocumentChunks, setMappedDocumentChunks] = useState([]);
    const [isContentLoading, setIsContentLoading] = useState(false);
    const [contentErrorMessage, setContentErrorMessage] = useState("");

    const resetLearningContent = useCallback(() => {
        setLearningContent(null);
        setMappedDocumentChunks([]);
        setContentErrorMessage("");
    }, []);

    const fetchMappedDocumentChunks = useCallback(async () => {
        const response = await getMappedDocumentChunks(
            userExamId,
            examScopeNodeId
        );

        const contentData = getApiData(response);
        setMappedDocumentChunks(Array.isArray(contentData) ? contentData : []);
    }, [userExamId, examScopeNodeId]);

    const fetchLearningContent = useCallback(async () => {
        if (!enabled || !userExamId || !examScopeNodeId) {
            resetLearningContent();
            return;
        }

        try {
            setIsContentLoading(true);
            setContentErrorMessage("");

            const response = await getUserLearningContent(
                userExamId,
                examScopeNodeId
            );

            const contentData = getApiData(response);
            setLearningContent(contentData || null);
            setMappedDocumentChunks([]);
        } catch (error) {
            if (error.response?.status === 404) {
                setLearningContent(null);

                try {
                    await fetchMappedDocumentChunks();
                } catch (mappedError) {
                    console.error("문서 기반 개념 내용 조회 실패:", mappedError);

                    setMappedDocumentChunks([]);
                    setContentErrorMessage(
                        mappedError.response?.data?.message ||
                        "문서 기반 개념 내용을 불러오지 못했습니다."
                    );
                }

                return;
            }

            console.error("문서 기반 개념 내용 조회 실패:", error);

            setLearningContent(null);
            setMappedDocumentChunks([]);
            setContentErrorMessage(
                error.response?.data?.message ||
                "문서 기반 개념 내용을 불러오지 못했습니다."
            );
        } finally {
            setIsContentLoading(false);
        }
    }, [
        enabled,
        userExamId,
        examScopeNodeId,
        resetLearningContent,
        fetchMappedDocumentChunks,
    ]);

    useEffect(() => {
        queueMicrotask(() => {
            fetchLearningContent();
        });
    }, [fetchLearningContent]);

    return {
        learningContent,
        mappedDocumentChunks,
        isContentLoading,
        contentErrorMessage,
        setLearningContent,
        resetLearningContent,
        refetchLearningContent: fetchLearningContent,
    };
};
