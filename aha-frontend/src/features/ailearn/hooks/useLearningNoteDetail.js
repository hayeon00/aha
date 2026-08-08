import { useCallback, useEffect, useState } from "react";

import { getLearningNoteDetail } from "../api/learningNoteApi.js";
import { getApiData } from "../utils/apiResponseUtils.js";

export function useLearningNoteDetail(learningNoteId) {
    const [detail, setDetail] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const loadDetail = useCallback(async () => {
        if (!learningNoteId) {
            setDetail(null);
            setError("");
            return;
        }

        try {
            setLoading(true);
            setError("");
            const response = await getLearningNoteDetail(learningNoteId);
            setDetail(getApiData(response));
        } catch (requestError) {
            setDetail(null);
            setError(
                requestError.response?.data?.message
                || "학습노트를 불러오지 못했습니다.",
            );
        } finally {
            setLoading(false);
        }
    }, [learningNoteId]);

    useEffect(() => {
        queueMicrotask(loadDetail);
    }, [loadDetail]);

    return {
        detail,
        loading,
        error,
        refresh: loadDetail,
    };
}
