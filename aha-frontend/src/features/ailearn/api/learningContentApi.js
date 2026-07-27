import axiosInstance from "../../../common/api/axiosInstance.js";

export const getUserLearningContent = (
    userExamId,
    examScopeNodeId
) => {
    return axiosInstance.get(
        `/api/v1/ai-learning/learning-contents/user-exams/${userExamId}/topics/${examScopeNodeId}`
    );
};

