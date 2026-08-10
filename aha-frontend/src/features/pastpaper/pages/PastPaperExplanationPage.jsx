import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import PastPaperProblemViewer from "../components/PastPaperProblemViewer.jsx";
import {
    getPastPaperAttemptAnswers,
    getPastPaperItems,
} from "../api/pastPaperApi.js";
import "./PastPaperAttemptPage.css";

function PastPaperExplanationPage() {
    const { pastPaperId, attemptId } = useParams();
    const location = useLocation();
    const navigate = useNavigate();
    const [items, setItems] = useState([]);
    const [answers, setAnswers] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState(null);

    const pastPaperTitle =
        location.state?.pastPaperTitle ||
        sessionStorage.getItem(`past-paper-title-attempt-${attemptId}`) ||
        sessionStorage.getItem(`past-paper-title-${pastPaperId}`) ||
        (pastPaperId ? `기출 문제 #${pastPaperId}` : "스터디 기출문제");

    const loadExplanation = useCallback(async () => {
        setIsLoading(true);
        setLoadError(null);

        try {
            const [itemsResponse, answersResponse] = await Promise.all([
                getPastPaperItems(attemptId),
                getPastPaperAttemptAnswers(attemptId),
            ]);
            const sortedItems = [...itemsResponse.data]
                .sort((first, second) => first.sortOrder - second.sortOrder)
                .map((item) => ({
                    ...item,
                    problemChoiceResponses: [
                        ...(item.problemChoiceResponses || []),
                    ].sort(
                        (first, second) =>
                            first.sortOrder - second.sortOrder
                    ),
                }));

            setItems(sortedItems);
            setAnswers(answersResponse.data?.userAnswerResponses || []);
        } catch (error) {
            console.error("문항별 해설 조회 실패:", error);
            setLoadError(
                error.errorCode === "PAST_PAPER_005"
                    ? "채점이 완료된 후 해설을 확인할 수 있습니다."
                    : "문항별 해설을 불러오지 못했습니다."
            );
        } finally {
            setIsLoading(false);
        }
    }, [attemptId]);

    useEffect(() => {
        queueMicrotask(loadExplanation);
    }, [loadExplanation]);

    if (isLoading) {
        return (
            <div className="attempt-state-card">
                <strong>문항별 해설을 불러오는 중입니다.</strong>
            </div>
        );
    }

    if (loadError || items.length === 0) {
        return (
            <div className="attempt-state-card error">
                <strong>{loadError || "표시할 문항이 없습니다."}</strong>
                <div className="attempt-state-actions">
                    <button type="button" onClick={loadExplanation}>
                        다시 시도
                    </button>
                    <button
                        type="button"
                        className="secondary"
                        onClick={() =>
                            navigate(
                                pastPaperId
                                    ? `/past-papers/${pastPaperId}/attempts/${attemptId}/result`
                                    : `/past-paper-attempts/${attemptId}/result`,
                                {
                                    state: {
                                        pastPaperTitle,
                                        studyRoomId: location.state?.studyRoomId,
                                    },
                                }
                            )
                        }
                    >
                        결과로 돌아가기
                    </button>
                </div>
            </div>
        );
    }

    return (
        <PastPaperProblemViewer
            mode="explanation"
            title={pastPaperTitle}
            items={items}
            answers={answers}
        />
    );
}

export default PastPaperExplanationPage;
