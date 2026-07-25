import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getVisibleUserExams } from "../../exam/api/userExamApi.js";
import {
    getPastPapers,
    startPastPaperAttempt,
} from "../api/pastPaperApi.js";
import "./PastPaperListPage.css";

const getPastPaperTitle = (pastPaper) => {
    if (pastPaper.title) {
        return pastPaper.title;
    }

    if (pastPaper.year && pastPaper.roundNo) {
        return `${pastPaper.year}년 복원기출 ${pastPaper.roundNo}회`;
    }

    if (pastPaper.year) {
        return `${pastPaper.year}년 복원기출`;
    }

    return `기출 문제 #${pastPaper.pastPaperId}`;
};

const formatExamDate = (examDate) => {
    if (!examDate) {
        return null;
    }

    const date = new Date(examDate);

    if (Number.isNaN(date.getTime())) {
        return null;
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}.${month}.${day}`;
};

const formatTimeLimit = (timeLimit) => {
    if (!timeLimit) {
        return null;
    }

    const minutes = Math.round(timeLimit / 60);

    return `${minutes}분`;
};

const formatProblemCount = (totalItemCount) => {
    if (!totalItemCount) {
        return null;
    }

    return `${totalItemCount}문항`;
};

const getExamVersionLabel = (exam) => {
    const versionName = exam.versionName?.trim() || "2025 개정판";
    const duplicatedCode = `${exam.examCode} `;

    return versionName.startsWith(duplicatedCode)
        ? versionName.slice(duplicatedCode.length)
        : versionName;
};

const getAttemptErrorMessage = (errorCode) => {
    switch (errorCode) {
        case "PAST_PAPER_001":
        case "PAST_PAPER_002":
            return "지금은 이 기출 문제를 시작할 수 없습니다.";
        case "EXAM_VERSION_001":
        case "EXAM_001":
            return "시험 정보가 변경되었습니다. 목록을 새로 확인해 주세요.";
        default:
            return "풀이를 시작하지 못했습니다. 잠시 후 다시 시도해 주세요.";
    }
};

function PastPaperListPage() {
    const navigate = useNavigate();
    const [exams, setExams] = useState([]);
    const [selectedExam, setSelectedExam] = useState(null);
    const [pastPapers, setPastPapers] = useState([]);
    const [isExamLoading, setIsExamLoading] = useState(true);
    const [isPastPaperLoading, setIsPastPaperLoading] = useState(false);
    const [startingPastPaperId, setStartingPastPaperId] = useState(null);
    const [attemptFeedback, setAttemptFeedback] = useState(null);
    const [isLeaving, setIsLeaving] = useState(false);

    useEffect(() => {
        let isMounted = true;

        const fetchExams = async () => {
            try {
                const response = await getVisibleUserExams();

                if (!isMounted) {
                    return;
                }

                setExams(response.data);
            } catch (error) {
                if (isMounted) {
                    console.error("시험 목록 mock 조회 실패:", error);
                }
            } finally {
                if (isMounted) {
                    setIsExamLoading(false);
                }
            }
        };

        fetchExams();

        return () => {
            isMounted = false;
        };
    }, []);

    const refreshExamAndRetry = useCallback(
        async ({ staleExam }) => {
            console.log(
                "[pastPaper] EXAM_VERSION_001 감지 - 사용자 알림 없이 시험 목록을 다시 동기화합니다."
            );

            const examsResponse = await getVisibleUserExams();
            const latestExams = examsResponse.data;
            const latestExam = latestExams.find(
                (exam) => exam.userExamId === staleExam.userExamId
            );

            setExams(latestExams);

            if (
                !latestExam ||
                latestExam.examVersionId === staleExam.examVersionId
            ) {
                throw new Error("사용자 시험 버전이 갱신되지 않았습니다.");
            }

            setSelectedExam(latestExam);

            console.log("[pastPaper] 실패했던 기출 조회를 최신 ID로 재시도합니다.", {
                previousExamVersionId: staleExam.examVersionId,
                nextExamVersionId: latestExam.examVersionId,
            });

            const retryResponse = await getPastPapers({
                examVersionId: latestExam.examVersionId,
                forceExamVersionError: false,
            });

            setPastPapers(retryResponse.data);
        },
        []
    );

    const fetchPastPapers = useCallback(
        async ({ exam }) => {
            setIsPastPaperLoading(true);

            try {
                const response = await getPastPapers({
                    examVersionId: exam.examVersionId,
                    forceExamVersionError: false,
                });

                setPastPapers(response.data);
            } catch (error) {
                if (error.errorCode === "EXAM_VERSION_001") {
                    await refreshExamAndRetry({
                        staleExam: exam,
                    });
                    return;
                }

                console.error("기출 목록 조회 실패:", error);
            } finally {
                setIsPastPaperLoading(false);
            }
        },
        [refreshExamAndRetry]
    );

    const handleSelectExam = async (exam) => {
        if (selectedExam) {
            return;
        }

        setSelectedExam(exam);

        try {
            await fetchPastPapers({
                exam,
            });
        } catch (error) {
            console.error("기출 문제 초기 구성 실패:", error);
        }
    };

    const handleStartAttempt = async (pastPaper) => {
        if (startingPastPaperId !== null) {
            return;
        }

        const pastPaperId = pastPaper.pastPaperId;
        const pastPaperTitle = getPastPaperTitle(pastPaper);
        const isContinuing = Boolean(pastPaper.solvingAttemptId);

        setStartingPastPaperId(pastPaperId);
        setAttemptFeedback(null);
        sessionStorage.setItem(
            `past-paper-title-${pastPaperId}`,
            pastPaperTitle
        );

        try {
            const response = await startPastPaperAttempt(pastPaperId);
            const { attemptId, attemptStatus, startedAt, dueAt } = response.data;

            if (!attemptId) {
                throw new Error("풀이 시작 응답에 attemptId가 없습니다.");
            }

            sessionStorage.setItem(
                `past-paper-attempt-${attemptId}`,
                JSON.stringify(response.data)
            );

            setAttemptFeedback({
                pastPaperId,
                type: "success",
                message: isContinuing
                    ? "진행 중인 풀이를 불러왔습니다."
                    : "풀이 준비가 완료되었습니다.",
                attempt: {
                    attemptId,
                    attemptStatus,
                    startedAt,
                    dueAt,
                },
            });
            setIsLeaving(true);
            await new Promise((resolve) => window.setTimeout(resolve, 260));
            navigate(`/past-papers/${pastPaperId}/attempts/${attemptId}`, {
                state: {
                    pastPaperTitle,
                    attempt: response.data,
                },
            });
        } catch (error) {
            console.error("기출 문제 풀이 시작 실패:", error);
            setAttemptFeedback({
                pastPaperId,
                type: "error",
                message: getAttemptErrorMessage(error.errorCode),
            });
        } finally {
            setStartingPastPaperId(null);
        }
    };

    const handleChangeExam = (event) => {
        event.stopPropagation();
        setSelectedExam(null);
        setPastPapers([]);
        setAttemptFeedback(null);
    };

    const isSelected = Boolean(selectedExam);

    return (
        <div
            className={[
                "workbook-page",
                isSelected ? "selected" : "",
                isLeaving ? "is-leaving" : "",
            ].filter(Boolean).join(" ")}
        >
            <header className="exam-selection-header">
                <span>기출 문제</span>
                <h1>준비 중인 시험을 선택해 주세요</h1>
                <p>학습할 자격증을 선택하면 복원된 기출 문제를 확인할 수 있어요.</p>
            </header>

            <section className="exam-card-layer" aria-label="시험 선택">
                {isExamLoading && <p className="loading-text">Loading...</p>}

                {exams.map((exam, index) => (
                    <div
                        key={exam.userExamId}
                        role="button"
                        tabIndex={selectedExam ? -1 : 0}
                        className={[
                            "exam-card",
                            selectedExam?.userExamId === exam.userExamId
                                ? "is-selected"
                                : "",
                            selectedExam &&
                            selectedExam.userExamId !== exam.userExamId
                                ? "is-hidden"
                                : "",
                        ]
                            .filter(Boolean)
                            .join(" ")}
                        style={{
                            "--card-index": index,
                            "--card-offset": `${
                                (index - (exams.length - 1) / 2) * 300
                            }px`,
                        }}
                        onClick={() => handleSelectExam(exam)}
                        onKeyDown={(event) => {
                            if (event.key === "Enter" || event.key === " ") {
                                handleSelectExam(exam);
                            }
                        }}
                    >
                        <div className="exam-card-copy">
                            <strong>{exam.examName}</strong>
                            {selectedExam?.userExamId === exam.userExamId && (
                                <span className="exam-card-version">
                                    {exam.examCode} {getExamVersionLabel(exam)}
                                </span>
                            )}
                        </div>
                        {selectedExam?.userExamId === exam.userExamId && (
                            <button
                                type="button"
                                className="change-exam-button"
                                onClick={handleChangeExam}
                            >
                                다른 시험 선택
                            </button>
                        )}
                    </div>
                ))}
            </section>

            <section className="workbook-list-area" aria-label="기출 문제 목록">
                <div className="workbook-list-head">
                    <h2>기출 문제 목록</h2>
                    {isPastPaperLoading && <span>조회 중...</span>}
                </div>

                <div className="workbook-list">
                    {pastPapers.map((pastPaper) => (
                        <article
                            className="workbook-item"
                            key={pastPaper.pastPaperId}
                        >
                            <div className="workbook-item-main">
                                <div className="workbook-item-heading">
                                    <strong>{getPastPaperTitle(pastPaper)}</strong>
                                    <span
                                        className={
                                            pastPaper.reviewed
                                                ? "past-paper-review-badge reviewed"
                                                : "past-paper-review-badge pending"
                                        }
                                    >
                                        {pastPaper.reviewed
                                            ? "검수 완료"
                                            : "검수 중"}
                                    </span>
                                </div>

                                <div className="workbook-meta-grid">
                                    {formatExamDate(pastPaper.examDate) && (
                                        <span className="workbook-meta">
                                            <span className="workbook-meta-label">
                                                시행일
                                            </span>
                                            {formatExamDate(pastPaper.examDate)}
                                        </span>
                                    )}

                                    {formatTimeLimit(pastPaper.timeLimit) && (
                                        <span className="workbook-meta">
                                            <span className="workbook-meta-label">
                                                제한시간
                                            </span>
                                            {formatTimeLimit(pastPaper.timeLimit)}
                                        </span>
                                    )}

                                    {formatProblemCount(
                                        pastPaper.totalItemCount
                                    ) && (
                                        <span className="workbook-meta">
                                            <span className="workbook-meta-label">
                                                문항수
                                            </span>
                                            {formatProblemCount(
                                                pastPaper.totalItemCount
                                            )}
                                        </span>
                                    )}
                                </div>
                            </div>

                            <div className="workbook-item-footer">
                                <button
                                    type="button"
                                    className={`workbook-start-button ${
                                        startingPastPaperId ===
                                        pastPaper.pastPaperId
                                            ? "is-loading"
                                            : ""
                                    } ${
                                        pastPaper.solvingAttemptId
                                            ? "is-resume"
                                            : ""
                                    }`}
                                    disabled={
                                        startingPastPaperId !== null ||
                                        (attemptFeedback?.pastPaperId ===
                                            pastPaper.pastPaperId &&
                                            attemptFeedback.type === "success")
                                    }
                                    onClick={() => handleStartAttempt(pastPaper)}
                                >
                                    {startingPastPaperId ===
                                    pastPaper.pastPaperId
                                        ? pastPaper.solvingAttemptId
                                            ? "풀이 불러오는 중..."
                                            : "준비 중..."
                                        : attemptFeedback?.pastPaperId ===
                                              pastPaper.pastPaperId &&
                                            attemptFeedback.type === "success"
                                          ? "준비 완료"
                                          : pastPaper.solvingAttemptId
                                            ? "이어풀기"
                                            : "풀이 시작"}
                                </button>
                            </div>

                        </article>
                    ))}

                    {!isPastPaperLoading && pastPapers.length === 0 && (
                        <p className="empty-text">조회된 기출 문제가 없습니다.</p>
                    )}
                </div>
            </section>
        </div>
    );
}

export default PastPaperListPage;
