import { useCallback, useEffect, useMemo, useState } from "react";
import { getVisibleUserExams } from "../api/userExamApi.js";
import { getApiData } from "../../ailearn/utils/apiResponseUtils.js";

export const useUserExams = ({ onEmpty, onChange, initialUserExamId } = {}) => {
    const [userExams, setUserExams] = useState([]);
    const [selectedUserExamId, setSelectedUserExamId] = useState(null);
    const [isExamLoading, setIsExamLoading] = useState(true);
    const [examMessage, setExamMessage] = useState("");

    const selectedUserExam = useMemo(() => {
        return userExams.find(
            (userExam) => userExam.userExamId === selectedUserExamId
        );
    }, [userExams, selectedUserExamId]);

    const selectedExamVersionId = selectedUserExam?.examVersionId ?? null;

    const fetchVisibleUserExams = useCallback(async () => {
        try {
            setIsExamLoading(true);
            setExamMessage("");

            const response = await getVisibleUserExams();
            const visibleUserExams = getApiData(response) || [];
            const nextUserExams = Array.isArray(visibleUserExams)
                ? visibleUserExams
                : [];

            setUserExams(nextUserExams);
            const requestedExam = nextUserExams.find(
                (userExam) => userExam.userExamId === initialUserExamId,
            );
            setSelectedUserExamId(requestedExam?.userExamId ?? nextUserExams[0]?.userExamId ?? null);

            if (nextUserExams.length === 0) {
                onEmpty?.();
            }
        } catch (error) {
            console.error("표시 시험 조회 실패:", error);

            setUserExams([]);
            setSelectedUserExamId(null);
            setExamMessage("표시 중인 시험 목록을 불러오지 못했습니다.");
            onEmpty?.();
        } finally {
            setIsExamLoading(false);
        }
    }, [initialUserExamId, onEmpty]);

    useEffect(() => {
        queueMicrotask(() => {
            fetchVisibleUserExams();
        });
    }, [fetchVisibleUserExams]);

    const changeUserExam = useCallback((userExamId) => {
        setSelectedUserExamId(userExamId);
        onChange?.();
    }, [onChange]);

    return {
        userExams,
        selectedUserExamId,
        selectedUserExam,
        selectedExamVersionId,
        isExamLoading,
        examMessage,
        changeUserExam,
        refetchUserExams: fetchVisibleUserExams,
    };
};
