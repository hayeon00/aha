import axiosInstance from "../../../common/api/axiosInstance.js";

export const getConcepts = (examVersionId, signal, examScopeNodeId) =>
    axiosInstance.get("/api/v1/concepts", { params: { examVersionId, examScopeNodeId }, signal });

export const getConcept = (conceptId, signal) =>
    axiosInstance.get(`/api/v1/concepts/${encodeURIComponent(conceptId)}`, { signal });
