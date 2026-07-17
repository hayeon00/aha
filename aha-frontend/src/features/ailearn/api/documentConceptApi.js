import axiosInstance from "../../../common/api/axiosInstance.js";

export const getOwnedDocuments = (userExamId) =>
    axiosInstance.get(`/api/documents/user-exams/${userExamId}`);

export const getDocumentConceptDashboard = (documentId) =>
    axiosInstance.get(`/api/documents/${documentId}/concepts`);

export const generateDocumentConcept = (documentId, tocId) =>
    axiosInstance.post(`/api/documents/${documentId}/toc/${tocId}/generate-concept`);

export const generateMissingDocumentConcepts = (documentId) =>
    axiosInstance.post(`/api/documents/${documentId}/generate-missing-concepts`);
