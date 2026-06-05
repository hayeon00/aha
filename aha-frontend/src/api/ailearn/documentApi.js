import axiosInstance from "../axiosInstance";

export const uploadLearningDocuments = (userExamId, files) => {
    const formData = new FormData();
    formData.append("userExamId", userExamId);

    files.forEach((file) => {
        formData.append("files", file);
    });

    return axiosInstance.post("/api/v1/aiLearn/documents/batch", formData, {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    });
};

export const getDocumentProcessingStatus = (processingId) => {
    return axiosInstance.get(`/api/v1/aiLearn/documents/processing-groups/${processingId}`);
};
