import { useCallback, useEffect, useState } from "react";

import {
    createLearningNote,
    getDocumentProcessingStatus,
} from "../api/learningDocumentApi.js";
import { getApiData } from "../utils/apiResponseUtils.js";

const POLLING_INTERVAL = 1500;

export function useLearningNoteCreation() {
    const [submitting, setSubmitting] = useState(false);
    const [processingId, setProcessingId] = useState(null);
    const [learningNoteId, setLearningNoteId] = useState(null);
    const [processing, setProcessing] = useState(null);
    const [error, setError] = useState("");

    const submit = useCallback(async ({ userExamId, title, file }) => {
        try {
            setSubmitting(true);
            setError("");
            setProcessing({
                status: "UPLOADING",
                currentStep: null,
            });

            const response = await createLearningNote({ userExamId, title, file });
            const created = getApiData(response);

            if (!created?.processingId || !created?.learningNoteId) {
                throw new Error("생성된 처리 작업 정보를 확인할 수 없습니다.");
            }

            setProcessingId(created.processingId);
            setLearningNoteId(created.learningNoteId);
            setProcessing({
                processingId: created.processingId,
                learningNoteId: created.learningNoteId,
                status: created.processingStatus ?? "PENDING",
                currentStep: null,
            });
        } catch (requestError) {
            setProcessing(null);
            setError(
                requestError.response?.data?.message
                || requestError.message
                || "학습노트 생성을 시작하지 못했습니다.",
            );
        } finally {
            setSubmitting(false);
        }
    }, []);

    useEffect(() => {
        if (!processingId || ["COMPLETED", "FAILED"].includes(processing?.status)) {
            return undefined;
        }

        const controller = new AbortController();
        let timerId;
        let consecutiveErrors = 0;

        const poll = async () => {
            try {
                const response = await getDocumentProcessingStatus(processingId, {
                    signal: controller.signal,
                });
                const next = getApiData(response);

                if (controller.signal.aborted) return;

                consecutiveErrors = 0;
                setProcessing(next);
                setLearningNoteId(next?.learningNoteId ?? null);

                if (["COMPLETED", "FAILED"].includes(next?.status)) {
                    if (next.status === "FAILED") {
                        setError(next.errorMessage || "문서를 처리하지 못했습니다.");
                    }
                    return;
                }
            } catch {
                if (controller.signal.aborted) return;
                consecutiveErrors += 1;
                if (consecutiveErrors >= 3) {
                    setError("처리 상태를 확인하지 못하고 있습니다. 잠시 후 다시 확인해 주세요.");
                }
            }

            const delay = Math.min(
                POLLING_INTERVAL * (2 ** consecutiveErrors),
                10000,
            );
            timerId = window.setTimeout(poll, delay);
        };

        poll();

        return () => {
            controller.abort();
            window.clearTimeout(timerId);
        };
    }, [processing?.status, processingId]);

    const reset = useCallback(() => {
        setSubmitting(false);
        setProcessingId(null);
        setLearningNoteId(null);
        setProcessing(null);
        setError("");
    }, []);

    return {
        submitting,
        processing,
        processingId,
        learningNoteId,
        error,
        submit,
        reset,
    };
}
