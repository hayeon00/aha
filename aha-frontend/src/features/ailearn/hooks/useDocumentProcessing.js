import { useCallback, useEffect, useState } from "react";
import {getDocumentProcessingStatus, getUserExamDocumentState, retryDocumentProcessing, uploadLearningDocuments,} from "../api/learningDocumentApi.js";
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
    const [completedProcessingKey, setCompletedProcessingKey] = useState(0);
    const [isRetrying, setIsRetrying] = useState(false);

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

            setHasProcessedDocuments(stateData?.status === "COMPLETED");

            if (["PENDING", "PROCESSING", "FAILED", "PARTIAL_FAILED"].includes(stateData?.status)) {
                setProcessingId(stateData.processingGroupId);
                setProcessingStatus(stateData);
                setUploadErrorMessage(stateData.errorMessage || "");
                setIsProgressModalOpen(
                    ["FAILED", "PARTIAL_FAILED"].includes(stateData.status)
                );
            }
        } catch (error) {
            console.error("문서 업로드 상태 조회 실패:", error);
            setHasProcessedDocuments(false);
        } finally {
            setIsDocumentStateLoading(false);
        }
    }, []);

    useEffect(() => {
        queueMicrotask(() => {
            fetchUserExamDocumentState(selectedUserExamId);
        });
    }, [selectedUserExamId, fetchUserExamDocumentState]);

    const uploadDocuments = useCallback(async (files) => {
        if (!selectedUserExamId || files.length === 0) {
            return;
        }

        try {
            setIsUploading(true);
            setUploadErrorMessage("");
            setProcessingStatus({
                status: "UPLOADING",
                currentStep: "UPLOAD_PENDING",
                stepMessage: "문서 업로드를 준비하고 있어요.",
                progressRate: 0,
                totalFileCount: files.length,
                completedFileCount: 0,
            });
            setIsProgressModalOpen(false);

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
            setIsProgressModalOpen(false);
        } catch (error) {
            console.error("문서 업로드 실패:", error);

            try {
                const recoveryResponse = await getUserExamDocumentState(selectedUserExamId);
                const recoveredState = getApiData(recoveryResponse);

                if (["PENDING", "PROCESSING"].includes(recoveredState?.status)) {
                    setProcessingId(recoveredState.processingGroupId);
                    setProcessingStatus(recoveredState);
                    setIsProgressModalOpen(false);
                    return;
                }
            } catch (recoveryError) {
                console.error("업로드 작업 복구 조회 실패:", recoveryError);
            }

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
        if (
            !processingId ||
            ["FAILED", "PARTIAL_FAILED"].includes(processingStatus?.status)
        ) {
            return undefined;
        }

        const abortController = new AbortController();
        let timeoutId;
        let consecutiveErrorCount = 0;

        const pollProcessingStatus = async () => {
            try {
                const response = await getDocumentProcessingStatus(processingId, {
                    signal: abortController.signal,
                });
                const statusData = getApiData(response);

                if (!statusData || abortController.signal.aborted) {
                    return;
                }

                consecutiveErrorCount = 0;
                setUploadErrorMessage("");
                setProcessingStatus(statusData);

                if (statusData.status === "COMPLETED") {
                    setIsProgressModalOpen(false);
                    setProcessingId(null);
                    setProcessingStatus(null);
                    setUploadErrorMessage("");
                    setHasProcessedDocuments(true);
                    setCompletedProcessingKey((previousKey) => previousKey + 1);

                    await fetchUserExamDocumentState(selectedUserExamId);

                    await onCompleted?.({
                        selectedUserExamId,
                        selectedNodeId,
                    });

                    return;
                }

                if (
                    statusData.status === "FAILED" ||
                    statusData.status === "PARTIAL_FAILED"
                ) {
                    setIsProgressModalOpen(true);
                    setUploadErrorMessage(
                        statusData.errorMessage ||
                        "문서 처리 중 오류가 발생했습니다."
                    );

                    return;
                }
            } catch (error) {
                if (abortController.signal.aborted) {
                    return;
                }

                console.error("문서 처리 상태 조회 실패:", error);
                setUploadErrorMessage("문서 처리 상태를 조회하지 못했습니다.");
                consecutiveErrorCount += 1;
            }

            const delay = Math.min(2000 * (2 ** consecutiveErrorCount), 10000);
            timeoutId = window.setTimeout(pollProcessingStatus, delay);
        };

        pollProcessingStatus();

        return () => {
            abortController.abort();
            window.clearTimeout(timeoutId);
        };
    }, [
        processingId,
        selectedUserExamId,
        selectedNodeId,
        fetchUserExamDocumentState,
        onCompleted,
        processingStatus?.status,
    ]);

    const retryProcessing = useCallback(async () => {
        if (!processingId || isRetrying) {
            return;
        }

        try {
            setIsRetrying(true);
            setUploadErrorMessage("");

            const response = await retryDocumentProcessing(processingId);
            const retryData = getApiData(response);

            setProcessingStatus(retryData);
            setIsProgressModalOpen(false);
        } catch (error) {
            console.error("문서 분석 재시도 실패:", error);
            setUploadErrorMessage(
                error?.response?.data?.message ||
                "재시도 요청에 실패했습니다. 잠시 후 다시 시도해 주세요."
            );
        } finally {
            setIsRetrying(false);
        }
    }, [processingId, isRetrying]);

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
        completedProcessingKey,
        isRetrying,
        uploadDocuments,
        retryProcessing,
        closeProgressModal,
        resetDocumentState,
        refetchDocumentState: fetchUserExamDocumentState,
    };
};
