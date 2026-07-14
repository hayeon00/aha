import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    getWorkbookExams,
    getWorkbooks,
    getWorkbookTypes,
    startWorkbookAttempt,
} from "../api/workbookApi.js";
import "./WorkbookPage.css";

const DEFAULT_WORKBOOK_TYPE_CODE = "PAST";

const formatQuery = (examVersionId, workbookTypeCode) =>
    `/api/v1/exam-versions/${examVersionId}/workbooks?workbookTypeCode=${workbookTypeCode}`;

const getWorkbookTitle = (workbook) => {
    if (workbook.title) {
        return workbook.title;
    }

    if (workbook.year && workbook.roundNo) {
        return `${workbook.year}년 복원기출 ${workbook.roundNo}회`;
    }

    if (workbook.year) {
        return `${workbook.year}년 복원기출`;
    }

    return `문제집 #${workbook.id}`;
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

const formatProblemCount = (totalProblemCount) => {
    if (!totalProblemCount) {
        return null;
    }

    return `${totalProblemCount}문항`;
};

const getAttemptErrorMessage = (errorCode) => {
    switch (errorCode) {
        case "WORKBOOK_001":
        case "WORKBOOK_002":
            return "지금은 이 문제집을 시작할 수 없습니다.";
        case "EXAM_VERSION_001":
        case "EXAM_001":
            return "시험 정보가 변경되었습니다. 목록을 새로 확인해 주세요.";
        case "WORKBOOK_003":
            return "이미 풀이 중인 문제집입니다.";
        default:
            return "풀이를 시작하지 못했습니다. 잠시 후 다시 시도해 주세요.";
    }
};

function WorkbookPage() {
    const navigate = useNavigate();
    const [exams, setExams] = useState([]);
    const [selectedExam, setSelectedExam] = useState(null);
    const [workbookTypes, setWorkbookTypes] = useState([]);
    const [activeWorkbookTypeCode, setActiveWorkbookTypeCode] = useState(
        DEFAULT_WORKBOOK_TYPE_CODE
    );
    const [workbooks, setWorkbooks] = useState([]);
    const [isExamLoading, setIsExamLoading] = useState(true);
    const [isWorkbookLoading, setIsWorkbookLoading] = useState(false);
    const [forceExamVersionError, setForceExamVersionError] = useState(false);
    const [statusText, setStatusText] = useState("시험 목록을 불러오는 중");
    const [lastQuery, setLastQuery] = useState("");
    const [silentRefreshCount, setSilentRefreshCount] = useState(0);
    const [startingWorkbookId, setStartingWorkbookId] = useState(null);
    const [attemptFeedback, setAttemptFeedback] = useState(null);

    useEffect(() => {
        let isMounted = true;

        const fetchExams = async () => {
            try {
                const response = await getWorkbookExams();

                if (!isMounted) {
                    return;
                }

                setExams(response.data);
                setStatusText("시험 카드를 선택해 주세요");
            } catch (error) {
                if (isMounted) {
                    console.error("시험 목록 mock 조회 실패:", error);
                    setStatusText("시험 목록 조회 실패");
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
        async ({ staleExam, workbookTypeCode }) => {
            console.log(
                "[workbook] EXAM_VERSION_001 감지 - 사용자 알림 없이 시험 목록을 다시 동기화합니다."
            );
            setStatusText("Silent Refresh: 최신 시험 버전 동기화 중");
            setSilentRefreshCount((count) => count + 1);

            const examsResponse = await getWorkbookExams();
            const latestExams = examsResponse.data;
            const latestExam =
                latestExams.find((exam) => exam.code === staleExam.code) ||
                latestExams[0];

            setExams(latestExams);
            setSelectedExam(latestExam);
            setStatusText(
                `Silent Refresh 완료: activeVersionId ${staleExam.activeVersionId} -> ${latestExam.activeVersionId}`
            );

            console.log("[workbook] 실패했던 워크북 조회를 최신 ID로 재시도합니다.", {
                previousExamVersionId: staleExam.activeVersionId,
                nextExamVersionId: latestExam.activeVersionId,
                workbookTypeCode,
            });

            const retryResponse = await getWorkbooks({
                examVersionId: latestExam.activeVersionId,
                workbookTypeCode,
                forceExamVersionError: false,
            });

            setLastQuery(formatQuery(latestExam.activeVersionId, workbookTypeCode));
            setWorkbooks(retryResponse.data);
            setStatusText("Retry 성공: 워크북 목록 갱신 완료");
        },
        []
    );

    const fetchWorkbooks = useCallback(
        async ({ exam, workbookTypeCode, allowForcedError = true }) => {
            setIsWorkbookLoading(true);
            setLastQuery(formatQuery(exam.activeVersionId, workbookTypeCode));
            setStatusText("워크북 목록 조회 중");

            try {
                const response = await getWorkbooks({
                    examVersionId: exam.activeVersionId,
                    workbookTypeCode,
                    forceExamVersionError:
                        allowForcedError && forceExamVersionError,
                });

                setWorkbooks(response.data);
                setStatusText("워크북 목록 조회 완료");
            } catch (error) {
                if (error.errorCode === "EXAM_VERSION_001") {
                    await refreshExamAndRetry({
                        staleExam: exam,
                        workbookTypeCode,
                    });
                    return;
                }

                console.error("워크북 목록 mock 조회 실패:", error);
                setStatusText("워크북 목록 조회 실패");
            } finally {
                setIsWorkbookLoading(false);
            }
        },
        [forceExamVersionError, refreshExamAndRetry]
    );

    const handleSelectExam = async (exam) => {
        if (selectedExam || exam.code !== "SQLD") {
            return;
        }

        setSelectedExam(exam);
        setActiveWorkbookTypeCode(DEFAULT_WORKBOOK_TYPE_CODE);
        setStatusText(`${exam.name} 선택됨`);

        try {
            const typesResponse = await getWorkbookTypes();
            setWorkbookTypes(typesResponse.data);

            await fetchWorkbooks({
                exam,
                workbookTypeCode: DEFAULT_WORKBOOK_TYPE_CODE,
            });
        } catch (error) {
            console.error("문제집 초기 구성 실패:", error);
            setStatusText("문제집 초기 구성 실패");
        }
    };

    const handleSelectWorkbookType = async (workbookTypeCode) => {
        if (!selectedExam || activeWorkbookTypeCode === workbookTypeCode) {
            return;
        }

        setActiveWorkbookTypeCode(workbookTypeCode);

        await fetchWorkbooks({
            exam: selectedExam,
            workbookTypeCode,
        });
    };

    const handleStartAttempt = async (workbook) => {
        if (startingWorkbookId !== null) {
            return;
        }

        const workbookId = workbook.id;
        const workbookTitle = getWorkbookTitle(workbook);

        setStartingWorkbookId(workbookId);
        setAttemptFeedback(null);
        sessionStorage.setItem(`workbook-title-${workbookId}`, workbookTitle);

        try {
            const response = await startWorkbookAttempt(workbookId);
            const attemptId = response.data?.id ?? response.data?.attemptId;

            if (!attemptId) {
                throw new Error("풀이 시작 응답에 attemptId가 없습니다.");
            }

            setAttemptFeedback({
                workbookId,
                type: "success",
                message: "풀이 준비가 완료되었습니다.",
                attempt: response.data,
            });
            navigate(`/problems/${workbookId}/attempts/${attemptId}`, {
                state: { workbookTitle },
            });
        } catch (error) {
            if (error.errorCode === "WORKBOOK_003") {
                const existingAttemptId =
                    error.data?.attemptId ??
                    (typeof error.data === "number" ? error.data : null);

                if (existingAttemptId) {
                    navigate(`/problems/${workbookId}/attempts/${existingAttemptId}`, {
                        state: { workbookTitle },
                    });
                    return;
                }
            }
            console.error("문제집 풀이 시작 실패:", error);
            setAttemptFeedback({
                workbookId,
                type: "error",
                message: getAttemptErrorMessage(error.errorCode),
            });
        } finally {
            setStartingWorkbookId(null);
        }
    };

    const isSelected = Boolean(selectedExam);

    return (
        <div className={isSelected ? "workbook-page selected" : "workbook-page"}>
            <section className="workbook-debug-panel" aria-label="워크북 상태">
                <button
                    type="button"
                    className={
                        forceExamVersionError
                            ? "error-toggle active"
                            : "error-toggle"
                    }
                    onClick={() =>
                        setForceExamVersionError((isActive) => !isActive)
                    }
                >
                    에러 강제 발생
                </button>
                <span>{forceExamVersionError ? "ON" : "OFF"}</span>
                <span>{statusText}</span>
                <span>Silent Refresh {silentRefreshCount}회</span>
            </section>

            <section className="exam-card-layer" aria-label="시험 선택">
                {isExamLoading && <p className="loading-text">Loading...</p>}

                {exams.map((exam, index) => (
                    <button
                        type="button"
                        key={exam.id}
                        className={[
                            "exam-card",
                            selectedExam?.id === exam.id ? "is-selected" : "",
                            selectedExam && selectedExam.id !== exam.id
                                ? "is-hidden"
                                : "",
                            exam.code !== "SQLD" ? "is-disabled" : "",
                        ]
                            .filter(Boolean)
                            .join(" ")}
                        style={{
                            "--card-index": index,
                            "--card-offset": `${(index - 1) * 270}px`,
                        }}
                        onClick={() => handleSelectExam(exam)}
                    >
                        <strong>{exam.name}</strong>
                        <span>{exam.versionName}</span>
                        <small>activeVersionId: {exam.activeVersionId}</small>
                    </button>
                ))}
            </section>

            <section className="workbook-top-area" aria-label="문제집 유형">
                <div className="workbook-tabs">
                    {workbookTypes.map((workbookType) => (
                        <button
                            type="button"
                            key={workbookType.code}
                            className={
                                activeWorkbookTypeCode === workbookType.code
                                    ? "active"
                                    : ""
                            }
                            onClick={() =>
                                handleSelectWorkbookType(workbookType.code)
                            }
                        >
                            {workbookType.name}
                        </button>
                    ))}
                </div>

                <div className="query-box">
                    <strong>현재 요청</strong>
                    <code>{lastQuery || "-"}</code>
                </div>
            </section>

            <section className="workbook-list-area" aria-label="워크북 목록">
                <div className="workbook-list-head">
                    <h2>문제집 목록</h2>
                    {isWorkbookLoading && <span>조회 중...</span>}
                </div>

                <div className="workbook-list">
                    {workbooks.map((workbook) => (
                        <article className="workbook-item" key={workbook.id}>
                            <div className="workbook-item-main">
                                <strong>{getWorkbookTitle(workbook)}</strong>

                                <div className="workbook-meta-grid">
                                    {formatExamDate(workbook.examDate) && (
                                        <span className="workbook-meta">
                                            <span className="workbook-meta-label">
                                                시행일
                                            </span>
                                            {formatExamDate(workbook.examDate)}
                                        </span>
                                    )}

                                    {formatTimeLimit(workbook.timeLimit) && (
                                        <span className="workbook-meta">
                                            <span className="workbook-meta-label">
                                                제한시간
                                            </span>
                                            {formatTimeLimit(workbook.timeLimit)}
                                        </span>
                                    )}

                                    {formatProblemCount(
                                        workbook.totalProblemCount
                                    ) && (
                                        <span className="workbook-meta">
                                            <span className="workbook-meta-label">
                                                문항수
                                            </span>
                                            {formatProblemCount(
                                                workbook.totalProblemCount
                                            )}
                                        </span>
                                    )}
                                </div>
                            </div>

                            <div className="workbook-item-footer">
                                <span className="workbook-id">ID {workbook.id}</span>
                                <button
                                    type="button"
                                    className="workbook-start-button"
                                    disabled={
                                        startingWorkbookId !== null ||
                                        (attemptFeedback?.workbookId === workbook.id &&
                                            attemptFeedback.type === "success")
                                    }
                                    onClick={() => handleStartAttempt(workbook)}
                                >
                                    {startingWorkbookId === workbook.id
                                        ? "시작 중..."
                                        : attemptFeedback?.workbookId === workbook.id &&
                                            attemptFeedback.type === "success"
                                          ? "준비 완료"
                                          : "풀이 시작"}
                                </button>
                            </div>

                            {attemptFeedback?.workbookId === workbook.id && (
                                <p
                                    className={`workbook-attempt-feedback ${attemptFeedback.type}`}
                                    role={attemptFeedback.type === "error" ? "alert" : "status"}
                                >
                                    {attemptFeedback.message}
                                </p>
                            )}
                        </article>
                    ))}

                    {!isWorkbookLoading && workbooks.length === 0 && (
                        <p className="empty-text">조회된 문제집이 없습니다.</p>
                    )}
                </div>
            </section>
        </div>
    );
}

export default WorkbookPage;
