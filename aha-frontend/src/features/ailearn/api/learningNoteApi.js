import axiosInstance from "../../../common/api/axiosInstance.js";

export const getCompletedLearningNotes = () =>
    axiosInstance.get("/api/v1/learning-notes");

export const getLearningNoteDetail = (learningNoteId) =>
    axiosInstance.get(`/api/v1/learning-notes/${learningNoteId}`);

export const deleteLearningNote = (learningNoteId) =>
    axiosInstance.delete(`/api/v1/learning-notes/${learningNoteId}`);

export const generateLearningNoteTopic = (learningNoteId, tocId) =>
    axiosInstance.post(`/api/v1/learning-notes/${learningNoteId}/contents/${tocId}/generate`);

export const updateLearningNoteTitle = (learningNoteId, title) =>
    axiosInstance.patch(
        `/api/v1/learning-notes/${learningNoteId}/title`,
        { title },
    );

export const updateLearningNoteContent = (learningNoteId, tocId, content) =>
    axiosInstance.patch(
        `/api/v1/learning-notes/${learningNoteId}/contents/${tocId}`,
        { content },
    );
