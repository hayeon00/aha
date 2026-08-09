import axiosInstance from "../../../common/api/axiosInstance.js";

export const createLearningNote = async ({ userExamId, title, file }) => {
    if (!userExamId) {
        throw new Error("userExamId가 필요합니다.");
    }
    if (!title?.trim()) {
        throw new Error("학습노트 제목이 필요합니다.");
    }
    if (!file) {
        throw new Error("업로드할 파일이 필요합니다.");
    }

    const formData = new FormData();
    formData.append("userExamId", String(userExamId));
    formData.append("title", title.trim());
    formData.append("file", file);

    return axiosInstance.post(
        "/api/v1/learning-notes/documents",
        formData,
        {
            timeout: 120000,
        }
    );
};

export const getDocumentProcessingStatus = (processingId, config = {}) =>
    axiosInstance.get(
        `/api/v1/document-processings/${processingId}`,
        config
    );
