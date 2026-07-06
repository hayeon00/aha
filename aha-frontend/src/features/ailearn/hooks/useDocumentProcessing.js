import { useCallback, useEffect, useState } from "react";
import {getDocumentProcessingStatus, getUserExamDocumentState, uploadLearningDocuments,} from "../api/learningDocumentApi.js";
import { getApiData } from "../utils/apiResponseUtils.js";

export const useDocumentProcessing = ({
                                          selectedUserExamId,
                                          selectedNodeId,
                                          onCompleted,
                                      } = {}) => {
    const [isUploading, setIsUploading] = useState(false);
    const [processingId, setProcessingId] = useState(null);
    const [processingStatus, setProcessingStatus] = useState(null);
    const [isProgressModalOpen, setIsProgressModalOpen] = useState(false);
    const [uploadErrorMessage, setUploadErrorMessage] = useState("");
    const [hasProcessedDocuments, setHasProcessedDocuments] = useState(false);
    const [isDocumentStateLoading, setIsDocumentStateLoading] = useState(false);

    const resetDocumentState = useCallback(() => {
        setHasProcessedDocuments(false);
        setUploadErrorMessage("");
        setProcessingId(null);
        setProcessingStatus(null);
        setIsProgressModalOpen(false);
    }, []);

    const fetchUserExamDocumentState = useCallback(async (userExamId) => {
        if (!userExamId) {
            setHasProcessedDocuments(false);
            return;
        }

        try {
            setIsDocumentStateLoading(true);

            const response = await getUserExamDocumentState(userExamId);
            const stateData = getApiData(response);

            setHasProcessedDocuments(Boolean(stateData?.hasUploadedDocuments));
        } catch (error) {
            console.error("문서 업로드 상태 조회 실패:", error);
            setHasProcessedDocuments(false);
        } finally {
            setIsDocumentStateLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchUserExamDocumentState(selectedUserExamId);
    }, [selectedUserExamId, fetchUserExamDocumentState]);

    const uploadDocuments = useCallback(async (files) => {
        if (!selectedUserExamId || files.length === 0) {
            return;
        }

        try {
            setIsUploading(true);
            setUploadErrorMessage("");

            const response = await uploadLearningDocuments(
                selectedUserExamId,
                files
            );

            const uploadData = getApiData(response);
            const nextProcessingId =
                uploadData?.processingId || uploadData?.processingGroupId;

            if (!nextProcessingId) {
                throw new Error("processingId not found");
            }

            setProcessingId(nextProcessingId);
            setProcessingStatus(uploadData);
            setIsProgressModalOpen(true);
        } catch (error) {
            console.error("문서 업로드 실패:", error);

            setUploadErrorMessage("문서 업로드에 실패했습니다.");
            setProcessingStatus({
                status: "FAILED",
                currentStep: "FAILED",
                progressRate: 0,
                totalFileCount: files.length,
                completedFileCount: 0,
                errorMessage: "문서 업로드에 실패했습니다.",
            });
            setIsProgressModalOpen(true);
        } finally {
            setIsUploading(false);
        }
    }, [selectedUserExamId]);

    useEffect(() => {
        if (!processingId || !isProgressModalOpen) {
            return undefined;
        }

        const pollProcessingStatus = async () => {
            try {
                const response = await getDocumentProcessingStatus(processingId);
                const statusData = getApiData(response);

                if (!statusData) {
                    return;
                }

                setProcessingStatus(statusData);

                if (statusData.status === "COMPLETED") {
                    setIsProgressModalOpen(false);
                    setProcessingId(null);
                    setProcessingStatus(null);
                    setUploadErrorMessage("");
                    setHasProcessedDocuments(true);

                    await fetchUserExamDocumentState(selectedUserExamId);

                    await onCompleted?.({
                        selectedUserExamId,
                        selectedNodeId,
                    });
                }

                if (
                    statusData.status === "FAILED" ||
                    statusData.status === "PARTIAL_FAILED"
                ) {
                    setUploadErrorMessage(
                        statusData.errorMessage ||
                        "문서 처리 중 오류가 발생했습니다."
                    );
                    setProcessingId(null);
                }
            } catch (error) {
                console.error("문서 처리 상태 조회 실패:", error);
                setUploadErrorMessage("문서 처리 상태를 조회하지 못했습니다.");
            }
        };

        pollProcessingStatus();

        const intervalId = window.setInterval(
            pollProcessingStatus,
            2000
        );

        return () => window.clearInterval(intervalId);
    }, [
        processingId,
        isProgressModalOpen,
        selectedUserExamId,
        selectedNodeId,
        fetchUserExamDocumentState,
        onCompleted,
    ]);

    const closeProgressModal = useCallback(() => {
        setIsProgressModalOpen(false);
        setProcessingId(null);
    }, []);

    return {
        isUploading,
        processingStatus,
        isProgressModalOpen,
        uploadErrorMessage,
        hasProcessedDocuments,
        isDocumentStateLoading,
        uploadDocuments,
        closeProgressModal,
        resetDocumentState,
        refetchDocumentState: fetchUserExamDocumentState,
    };
};