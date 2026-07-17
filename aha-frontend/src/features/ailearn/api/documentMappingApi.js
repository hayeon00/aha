import axiosInstance from "../../../common/api/axiosInstance.js";

export const getUnassignedDocumentChunks = (userExamId) =>
    axiosInstance.get(
        `/api/v1/ai-learning/document-mappings/user-exams/${userExamId}/unassigned`
    );

export const assignDocumentChunk = (chunkId, examScopeNodeId) =>
    axiosInstance.put(
        `/api/v1/ai-learning/document-mappings/chunks/${chunkId}`,
        { examScopeNodeId }
    );
