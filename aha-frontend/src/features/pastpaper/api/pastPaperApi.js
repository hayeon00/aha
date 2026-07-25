import axiosInstance from "../../../common/api/axiosInstance.js";

const RESULT_STORAGE_PREFIX = "past-paper-attempt-result-";

const getApiData = (response) =>
    response.data?.data ?? response.data ?? response;

const normalizeError = (error) => {
    const responseData = error.response?.data;
    error.status = error.response?.status ?? error.status;
    error.errorCode =
        responseData?.errorCode ||
        responseData?.code ||
        error.errorCode ||
        error.code;
    error.data = responseData?.data ?? error.data;

    return error;
};

const createExamVersionError = () => {
    const error = new Error("Exam version is stale.");
    error.status = 400;
    error.errorCode = "EXAM_VERSION_001";
    error.response = {
        status: 400,
        data: {
            code: "EXAM_VERSION_001",
        },
    };

    return error;
};

export const getPastPapers = async ({
    examVersionId,
    forceExamVersionError = false,
}) => {
    if (forceExamVersionError) {
        throw createExamVersionError();
    }

    try {
        const response = await axiosInstance.get(
            `/api/v1/exam-versions/${examVersionId}/past-papers`
        );

        return {
            data: getApiData(response) || [],
        };
    } catch (error) {
        throw normalizeError(error);
    }
};

export const getPastPaperAttempts = async ({
    attemptStatus = "GRADED",
    page = 0,
    size = 10,
} = {}) => {
    try {
        const response = await axiosInstance.get(
            "/api/v1/past-paper-attempts",
            {
                params: {
                    attemptStatus,
                    page,
                    size,
                },
            }
        );

        return {
            data: getApiData(response),
        };
    } catch (error) {
        throw normalizeError(error);
    }
};

export const startPastPaperAttempt = async (pastPaperId) => {
    try {
        const response = await axiosInstance.post(
            `/api/v1/past-papers/${pastPaperId}/attempts`
        );

        return {
            data: getApiData(response),
        };
    } catch (error) {
        throw normalizeError(error);
    }
};

export const getPastPaperItems = async (attemptId) => {
    try {
        const response = await axiosInstance.get(
            `/api/v1/past-paper-attempts/${attemptId}/items`
        );

        return {
            data: getApiData(response) || [],
        };
    } catch (error) {
        throw normalizeError(error);
    }
};

export const getPastPaperAttemptAnswers = async (attemptId) => {
    try {
        const response = await axiosInstance.get(
            `/api/v1/past-paper-attempts/${attemptId}/answers`
        );

        return {
            data: getApiData(response),
        };
    } catch (error) {
        throw normalizeError(error);
    }
};

export const savePastPaperAnswer = async ({
    attemptId,
    problemId,
    userAnswer,
}) => {
    try {
        await axiosInstance.patch(
            `/api/v1/past-paper-attempts/${attemptId}/problems/${problemId}/answers`,
            { userAnswer }
        );
    } catch (error) {
        throw normalizeError(error);
    }
};

export const togglePastPaperReviewMark = async ({
    attemptId,
    problemId,
}) => {
    try {
        await axiosInstance.patch(
            `/api/v1/past-paper-attempts/${attemptId}/problems/${problemId}/review-mark`
        );
    } catch (error) {
        throw normalizeError(error);
    }
};

export const submitPastPaperAttempt = async (attemptId) => {
    try {
        const response = await axiosInstance.patch(
            `/api/v1/past-paper-attempts/${attemptId}/submit`
        );
        const submitResponse = getApiData(response);

        sessionStorage.setItem(
            `${RESULT_STORAGE_PREFIX}${attemptId}`,
            JSON.stringify(submitResponse)
        );

        return {
            data: submitResponse,
        };
    } catch (error) {
        throw normalizeError(error);
    }
};

export const getStoredPastPaperResult = (attemptId) => {
    try {
        const storedResponse = sessionStorage.getItem(
            `${RESULT_STORAGE_PREFIX}${attemptId}`
        );

        if (!storedResponse) {
            return null;
        }

        return JSON.parse(storedResponse)?.result ?? null;
    } catch {
        return null;
    }
};
