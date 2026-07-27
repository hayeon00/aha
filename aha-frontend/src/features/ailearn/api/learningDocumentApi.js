import axiosInstance from "../../../common/api/axiosInstance.js";

export const uploadLearningDocuments = async (
    userExamId,
    files
) => {
    if (!userExamId) {
        throw new Error("userExamId가 필요합니다.");
    }

    if (!Array.isArray(files) || files.length === 0) {
        throw new Error("업로드할 파일이 없습니다.");
    }

    const formData = new FormData();

    files.forEach((file) => {
        formData.append("files", file);
    });

    const response = await axiosInstance.post(
        "/api/v1/ai-learning/document-uploads",
        formData,
        {
            params: {
                userExamId,
            },
            timeout: 120000,
        }
    );

    return response.data;
};

export const getDocumentProcessingStatus = (
    processingGroupId,
    config = {}
) => {
    return axiosInstance.get(
        `/api/v1/ai-learning/document-processings/${processingGroupId}`,
        config
    );
};

export const getUserExamDocumentState = (
    userExamId
) => {
    return axiosInstance.get(
        `/api/v1/ai-learning/document-processings/user-exams/${userExamId}/latest`
    );
};

export const retryDocumentProcessing = (processingGroupId) => {
    if (!processingGroupId) {
        throw new Error("processingGroupId가 필요합니다.");
    }

    return axiosInstance.post(
        `/api/v1/ai-learning/document-processings/${processingGroupId}/retry`
    );
};