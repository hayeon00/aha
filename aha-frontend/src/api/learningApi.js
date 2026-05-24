import axiosInstance from "./axiosInstance";

export const createLearningSession = async ({ examScopeNodeId, learningContentId }) => {
    const response = await axiosInstance.post("/api/v1/learning/sessions", {
        examScopeNodeId,
        learningContentId,
    });

    return response.data.data;
};

export const getConceptProblems = async (learningSessionId) => {
    const response = await axiosInstance.get(
        `/api/v1/learning/sessions/${learningSessionId}/concept-problems`
    );

    return response.data.data;
};