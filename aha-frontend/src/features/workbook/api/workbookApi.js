import axiosInstance from "../../../common/api/axiosInstance.js";

const getApiData = (response) => response.data?.data ?? response.data ?? response;

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

export const getWorkbookExams = async () => {
    console.log("[api] GET /api/v1/exams");

    const response = await axiosInstance.get("/api/v1/exams");

    return {
        data: getApiData(response),
    };
};

export const getWorkbookTypes = async () => {
    console.log("[api] GET /api/v1/workbook-types");

    const response = await axiosInstance.get("/api/v1/workbook-types");
    const workbookTypes = getApiData(response) || [];

    return {
        data: [...workbookTypes].sort(
            (first, second) => first.displayOrder - second.displayOrder
        ),
    };
};

export const getWorkbooks = async ({
    examVersionId,
    workbookTypeCode,
    forceExamVersionError = false,
}) => {
    console.log(
        `[api] GET /api/v1/exam-versions/${examVersionId}/workbooks?workbookTypeCode=${workbookTypeCode}`,
        {
            forceExamVersionError,
        }
    );

    if (forceExamVersionError) {
        throw createExamVersionError();
    }

    try {
        const response = await axiosInstance.get(
            `/api/v1/exam-versions/${examVersionId}/workbooks`,
            {
                params: {
                    workbookTypeCode,
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

export const getWorkbookItems = async (workbookId) => {
    try {
        const response = await axiosInstance.get(
            `/api/v1/workbooks/${workbookId}/items`
        );

        return { data: getApiData(response) || [] };
    } catch (error) {
        throw normalizeError(error);
    }
};

export const getWorkbookAttemptAnswers = async (attemptId) => {
    try {
        const response = await axiosInstance.get(
            `/api/v1/workbook-attempts/${attemptId}/answers`
        );

        return { data: getApiData(response) || [] };
    } catch (error) {
        throw normalizeError(error);
    }
};

export const saveWorkbookAnswer = async ({ attemptId, problemId, userAnswer }) => {
    try {
        await axiosInstance.patch(
            `/api/v1/workbook-attempts/${attemptId}/problems/${problemId}/answers`,
            { userAnswer }
        );
    } catch (error) {
        throw normalizeError(error);
    }
};

export const toggleWorkbookProblemCheck = async ({ attemptId, problemId }) => {
    try {
        await axiosInstance.patch(
            `/api/v1/workbook-attempts/${attemptId}/problems/${problemId}/check`
        );
    } catch (error) {
        throw normalizeError(error);
    }
};

export const startWorkbookAttempt = async (workbookId) => {
    console.log(`[api] POST /api/v1/workbooks/${workbookId}/attempts`);

    try {
        const response = await axiosInstance.post(
            `/api/v1/workbooks/${workbookId}/attempts`
        );

        return {
            data: getApiData(response),
        };
    } catch (error) {
        throw normalizeError(error);
    }
};

export const submitWorkbookAttempt = async (attemptId) => {
    try {
        const response = await axiosInstance.post(
            `/api/v1/workbook-attempts/${attemptId}/submit`
        );

        return { data: getApiData(response) };
    } catch (error) {
        throw normalizeError(error);
    }
};

export const getWorkbookAttemptResult = async (attemptId) => {
    try {
        const response = await axiosInstance.get(
            `/api/v1/workbook-attempts/${attemptId}`
        );

        return { data: getApiData(response) };
    } catch (error) {
        throw normalizeError(error);
    }
};
