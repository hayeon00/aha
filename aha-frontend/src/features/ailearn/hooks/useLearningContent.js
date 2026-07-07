import { useCallback, useEffect, useState } from "react";
import { getUserLearningContent } from "../api/learningContentApi.js";
import { getApiData } from "../utils/apiResponseUtils.js";

export const useLearningContent = ({
                                       userExamId,
                                       examScopeNodeId,
                                       enabled = true,
                                   }) => {
    const [learningContent, setLearningContent] = useState(null);
    const [isContentLoading, setIsContentLoading] = useState(false);
    const [contentErrorMessage, setContentErrorMessage] = useState("");

    const resetLearningContent = useCallback(() => {
        setLearningContent(null);
        setContentErrorMessage("");
    }, []);

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
            setLearningContent(contentData);
        } catch (error) {
            if (error.response?.status === 404) {
                setLearningContent(null);
                setContentErrorMessage("");
                return;
            }

            console.error("개념 설명 조회 실패:", error);

            setLearningContent(null);
            setContentErrorMessage(
                error.response?.data?.message ||
                "개념 설명을 불러오지 못했습니다."
            );
        } finally {
            setIsContentLoading(false);
        }
    }, [
        enabled,
        userExamId,
        examScopeNodeId,
        resetLearningContent,
    ]);

    useEffect(() => {
        queueMicrotask(() => {
            fetchLearningContent();
        });
    }, [fetchLearningContent]);

    return {
        learningContent,
        isContentLoading,
        contentErrorMessage,
        setLearningContent,
        resetLearningContent,
        refetchLearningContent: fetchLearningContent,
    };
};