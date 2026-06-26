import axiosInstance from "../axiosInstance";

export const uploadLearningDocuments = (userExamId, files) => {
    const formData = new FormData();

    formData.append("userExamId", userExamId);

    files.forEach((file) => {
        formData.append("files", file);
    });

    return axiosInstance.post(
        "/api/v1/ai-learning/documents/upload",
        formData,
        {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        }
    );
};

export const getDocumentProcessingStatus = (
    processingGroupId
) => {
    return axiosInstance.get(
        `/api/v1/ai-learning/documents/processing-groups/${processingGroupId}`
    );
};

export const getUserExamDocumentState = (
    userExamId
) => {
    return axiosInstance.get(
        `/api/v1/ai-learning/documents/user-exams/${userExamId}/state`
    );
};