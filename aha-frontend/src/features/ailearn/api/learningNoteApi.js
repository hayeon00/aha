import axiosInstance from "../../../common/api/axiosInstance.js";

export const getCompletedLearningNotes = () =>
    axiosInstance.get("/api/v1/learning-notes");
