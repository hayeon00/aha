import axiosInstance from "../../../common/api/axiosInstance.js";

export const getExams = async () => {
    const response = await axiosInstance.get("/api/v1/exams");
    return response.data;
};

export const getExamScopeNodes = async (examVersionId) => {
    const response = await axiosInstance.get(
        `/api/v1/exam-versions/${examVersionId}/scope-nodes`
    );

    return response.data;
};
