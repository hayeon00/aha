import axiosInstance from "../../../common/api/axiosInstance.js";

export const getUserExams = async () => {
    const response = await axiosInstance.get("/api/v1/user-exams");
    return response.data;
};

export const getVisibleUserExams = async () => {
    const response = await axiosInstance.get("/api/v1/user-exams/visible");
    return response.data;
};

export const updateUserExamHidden = async (userExamId, hidden) => {
    const response = await axiosInstance.patch(
        `/api/v1/user-exams/${userExamId}/hidden`,
        {
            hidden,
        }
    );

    return response.data;
};