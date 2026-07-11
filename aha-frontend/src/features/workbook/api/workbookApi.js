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
