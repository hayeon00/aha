import axiosInstance from "../../../common/api/axiosInstance.js";

export const getOwnedDocuments = (userExamId) =>
    axiosInstance.get(`/api/documents/user-exams/${userExamId}`);

export const getDocumentConceptDashboard = (documentId) =>
    axiosInstance.get(`/api/documents/${documentId}/concepts`);

export const generateDocumentConcept = (documentId, tocId, prompt) =>
    axiosInstance.post(
        `/api/documents/${documentId}/toc/${tocId}/generate-concept`,
        { prompt },
    );

export const generateMissingDocumentConcepts = (documentId) =>
    axiosInstance.post(`/api/documents/${documentId}/generate-missing-concepts`);

export const createDocumentLearningNote = (documentId) =>
    axiosInstance.post(`/api/documents/${documentId}/learning-note`);

export const updateDocumentConcept = (documentId, tocId, content) =>
    axiosInstance.patch(
        `/api/documents/${documentId}/toc/${tocId}/concept`,
        { content },
    );
