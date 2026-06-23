import axiosInstance from "../axiosInstance";

export const getUserLearningContent = (
    userExamId,
    examScopeNodeId
) => {
    return axiosInstance.get(
        `/api/v1/aiLearn/learning-contents/user-exams/${userExamId}/scope-nodes/${examScopeNodeId}`
    );
};