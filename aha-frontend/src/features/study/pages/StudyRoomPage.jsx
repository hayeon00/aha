import { useCallback, useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { useNavigate, useParams } from "react-router-dom";
import { getVisibleUserExams } from "../../exam/api/userExamApi.js";
import ExamSelectDropdown from "../../exam/components/ExamSelectDropdown.jsx";
import StudyFilterDropdown from "../components/StudyFilterDropdown.jsx";
import { getPastPapers } from "../../pastpaper/api/pastPaperApi.js";
import {
    createStudyRoom,
    getCurrentStudyRoom,
    getStudyRoom,
    getStudyRooms,
    joinStudyRoom,
} from "../api/studyRoomApi.js";
import "./StudyRoomPage.css";

const PAGE_SIZE = 10;
const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const SORT_OPTIONS = [
    { value: "LATEST", label: "최신순" },
    { value: "OLDEST", label: "오래된순" },
    { value: "MOST_MEMBERS", label: "참여자 많은순" },
];

const STATUS_OPTIONS = [
    { value: "WAITING", label: "모집 중" },
    { value: "SOLVING", label: "풀이 중" },
    { value: "FEEDBACK", label: "피드백 중" },
];

const STUDY_ERROR_MESSAGES = {
    STUDY_001: "이미 참여 중인 활성 스터디룸이 있습니다.",
    STUDY_002: "존재하지 않는 스터디룸입니다.",
    STUDY_004: "취소된 스터디룸입니다.",
    STUDY_005: "정원이 모두 찬 스터디룸입니다.",
    STUDY_006: "이미 풀이가 시작된 스터디룸입니다.",
};

const initialCreateForm = {
    pastPaperId: "",
    title: "",
    description: "",
    capacity: 2,
    timeLimitMinutes: 60,
};

const getPastPaperTitle = (pastPaper) => {
    if (pastPaper.title) {
        return pastPaper.title;
    }

    if (pastPaper.year && pastPaper.roundNo) {
        return `${pastPaper.year}년 ${pastPaper.roundNo}회차 시험`;
    }

    return `기출문제 #${pastPaper.pastPaperId}`;
};

const formatDateTime = (value) => {
    if (!value) {
        return "";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return "";
    }

    return new Intl.DateTimeFormat("ko-KR", {
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(date);
};

const getVisiblePages = (currentPage, totalPages) => {
    const start = Math.max(
        0,
        Math.min(currentPage - 2, Math.max(totalPages - 5, 0))
    );
    const end = Math.min(start + 5, totalPages);

    return Array.from({ length: end - start }, (_, index) => start + index);
};

const getProfileImageUrl = (imageUrl) => {
    if (!imageUrl) {
        return null;
    }

    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
        return imageUrl;
    }

    return `${API_BASE_URL}${imageUrl}`;
};

const formatExamDate = (value) => {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return new Intl.DateTimeFormat("ko-KR", {
        year: "numeric",
        month: "long",
        day: "numeric",
    }).format(date);
};

const getErrorMessage = (error, fallback) => {
    if (STUDY_ERROR_MESSAGES[error?.errorCode]) {
        return STUDY_ERROR_MESSAGES[error.errorCode];
    }

    return error?.response?.data?.message || error?.message || fallback;
};

const getValidationErrors = (error) => {
    const errors = error?.response?.data?.errors;

    if (!Array.isArray(errors)) {
        return {};
    }

    return errors.reduce((fieldErrors, validationError) => {
        if (!validationError?.field || !validationError?.reason) {
            return fieldErrors;
        }

        const field =
            validationError.field === "timeLimit"
                ? "timeLimitMinutes"
                : validationError.field;

        if (!fieldErrors[field]) {
            fieldErrors[field] = validationError.reason;
        }

        return fieldErrors;
    }, {});
};

function StudyRoomPage() {
    const navigate = useNavigate();
    const { studyRoomId } = useParams();

    const [exams, setExams] = useState([]);
    const [selectedExam, setSelectedExam] = useState(null);
    const [rooms, setRooms] = useState([]);
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: PAGE_SIZE,
        totalPages: 0,
        totalElements: 0,
        first: true,
        last: true,
    });
    const [filters, setFilters] = useState({
        status: "WAITING",
        sortType: "LATEST",
    });
    const [page, setPage] = useState(0);
    const [isExamLoading, setIsExamLoading] = useState(true);
    const [isListLoading, setIsListLoading] = useState(false);
    const [listError, setListError] = useState("");

    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [pastPapers, setPastPapers] = useState([]);
    const [isPastPaperLoading, setIsPastPaperLoading] = useState(false);
    const [createForm, setCreateForm] = useState(initialCreateForm);
    const [createError, setCreateError] = useState("");
    const [createFieldErrors, setCreateFieldErrors] = useState({});
    const [isCreating, setIsCreating] = useState(false);
    const [currentStudyRoom, setCurrentStudyRoom] = useState(null);
    const [isCurrentRoomLoading, setIsCurrentRoomLoading] = useState(true);
    const [currentRoomError, setCurrentRoomError] = useState("");
    const [detailRoom, setDetailRoom] = useState(null);
    const [isDetailLoading, setIsDetailLoading] = useState(false);
    const [detailError, setDetailError] = useState("");
    const [isJoining, setIsJoining] = useState(false);
    const [joinError, setJoinError] = useState("");
    const headerActionsTarget =
        document.getElementById("page-header-actions");

    useEffect(() => {
        let active = true;

        const loadExams = async () => {
            try {
                const response = await getVisibleUserExams();

                if (active) {
                    const loadedExams = response.data || [];
                    setExams(loadedExams);
                    setSelectedExam((current) => current || loadedExams[0] || null);
                }
            } catch (error) {
                if (active) {
                    console.error("시험 목록 조회 실패:", error);
                }
            } finally {
                if (active) {
                    setIsExamLoading(false);
                }
            }
        };

        loadExams();

        return () => {
            active = false;
        };
    }, []);

    const loadStudyRooms = useCallback(async (overrides = {}) => {
        if (!selectedExam?.examVersionId) {
            return;
        }

        const targetStatus = overrides.status ?? filters.status;
        const targetSortType = overrides.sortType ?? filters.sortType;
        const targetPage = overrides.page ?? page;

        setIsListLoading(true);
        setListError("");

        try {
            const response = await getStudyRooms({
                examVersionId: selectedExam.examVersionId,
                status: targetStatus,
                sortType: targetSortType,
                page: targetPage,
                size: PAGE_SIZE,
            });

            setRooms(response?.content || []);
            setPageInfo({
                page: response?.page ?? targetPage,
                size: response?.size ?? PAGE_SIZE,
                totalPages: response?.totalPages ?? 0,
                totalElements: response?.totalElements ?? 0,
                first: response?.first ?? true,
                last: response?.last ?? true,
            });
        } catch (error) {
            console.error("스터디룸 목록 조회 실패:", error);
            setRooms([]);
            setListError(
                getErrorMessage(
                    error,
                    "스터디룸 목록을 불러오지 못했습니다."
                )
            );
        } finally {
            setIsListLoading(false);
        }
    }, [filters.sortType, filters.status, page, selectedExam]);

    useEffect(() => {
        queueMicrotask(() => {
            loadStudyRooms();
        });
    }, [loadStudyRooms]);

    const loadCurrentStudyRoom = useCallback(async () => {
        setIsCurrentRoomLoading(true);
        setCurrentRoomError("");

        try {
            const response = await getCurrentStudyRoom();
            setCurrentStudyRoom(response);
        } catch (error) {
            if (error?.errorCode === "STUDY_003") {
                setCurrentStudyRoom(null);
                return;
            }

            console.error("현재 참여 스터디룸 조회 실패:", error);
            setCurrentStudyRoom(null);
            setCurrentRoomError(
                getErrorMessage(
                    error,
                    "현재 참여 중인 스터디룸을 불러오지 못했습니다."
                )
            );
        } finally {
            setIsCurrentRoomLoading(false);
        }
    }, []);

    useEffect(() => {
        queueMicrotask(() => {
            loadCurrentStudyRoom();
        });
    }, [loadCurrentStudyRoom]);

    const loadStudyRoomDetail = useCallback(async () => {
        if (!studyRoomId) {
            setDetailRoom(null);
            setDetailError("");
            return;
        }

        setIsDetailLoading(true);
        setDetailError("");

        try {
            const response = await getStudyRoom(studyRoomId);
            setDetailRoom(response);
        } catch (error) {
            console.error("스터디룸 상세 조회 실패:", error);
            setDetailRoom(null);
            setDetailError(
                getErrorMessage(
                    error,
                    "스터디룸 상세 정보를 불러오지 못했습니다."
                )
            );
        } finally {
            setIsDetailLoading(false);
        }
    }, [studyRoomId]);

    useEffect(() => {
        queueMicrotask(() => {
            loadStudyRoomDetail();
        });
    }, [loadStudyRoomDetail]);

    const handleSelectExam = (exam) => {
        setSelectedExam(exam);
        setRooms([]);
        setListError("");
        setPage(0);
        setIsCreateOpen(false);
        navigate("/study-rooms", { replace: true });
    };

    const handleFilterChange = (name, value) => {
        setFilters((current) => ({
            ...current,
            [name]: value,
        }));
        setPage(0);
    };

    const openCreateModal = async () => {
        if (!selectedExam) {
            return;
        }

        setCreateForm(initialCreateForm);
        setCreateError("");
        setCreateFieldErrors({});
        setPastPapers([]);
        setIsCreateOpen(true);
        setIsPastPaperLoading(true);

        try {
            const response = await getPastPapers({
                examVersionId: selectedExam.examVersionId,
            });
            setPastPapers(response.data || []);
        } catch (error) {
            console.error("기출문제 목록 조회 실패:", error);
            setCreateError("기출문제 목록을 불러오지 못했습니다.");
        } finally {
            setIsPastPaperLoading(false);
        }
    };

    const closeCreateModal = () => {
        if (!isCreating) {
            setIsCreateOpen(false);
        }
    };

    const handleCreateFormChange = (event) => {
        const { name, value } = event.target;

        setCreateFieldErrors((current) => {
            if (!current[name]) {
                return current;
            }

            const next = { ...current };
            delete next[name];
            return next;
        });
        setCreateError("");

        setCreateForm((current) => {
            const next = {
                ...current,
                [name]: value,
            };

            if (name === "pastPaperId") {
                const selectedPaper = pastPapers.find(
                    (paper) => String(paper.pastPaperId) === value
                );

                if (selectedPaper?.timeLimit) {
                    next.timeLimitMinutes = Math.max(
                        1,
                        Math.round(selectedPaper.timeLimit / 60)
                    );
                }
            }

            return next;
        });
    };

    const handleCreateSubmit = async (event) => {
        event.preventDefault();
        setCreateError("");
        setCreateFieldErrors({});

        const validationErrors = {};
        const title = createForm.title.trim();
        const description = createForm.description.trim();
        const capacity = Number(createForm.capacity);
        const timeLimitMinutes = Number(createForm.timeLimitMinutes);

        if (!createForm.pastPaperId) {
            validationErrors.pastPaperId =
                "함께 풀 기출문제를 선택해 주세요.";
        }

        if (!title) {
            validationErrors.title = "제목을 입력해 주세요.";
        } else if (title.length > 100) {
            validationErrors.title =
                "제목은 최대 100글자까지 입력할 수 있습니다.";
        }

        if (!description) {
            validationErrors.description = "설명을 입력해 주세요.";
        } else if (description.length > 500) {
            validationErrors.description =
                "설명은 최대 500글자까지 입력할 수 있습니다.";
        }

        if (!Number.isInteger(capacity) || capacity < 2 || capacity > 5) {
            validationErrors.capacity = "정원은 2명 이상 5명 이하여야 합니다.";
        }

        if (
            !Number.isFinite(timeLimitMinutes) ||
            timeLimitMinutes <= 0
        ) {
            validationErrors.timeLimitMinutes =
                "제한 시간은 1분 이상이어야 합니다.";
        }

        if (Object.keys(validationErrors).length > 0) {
            setCreateFieldErrors(validationErrors);
            setCreateError("입력한 내용을 다시 확인해 주세요.");
            return;
        }

        setIsCreating(true);

        try {
            await createStudyRoom({
                pastPaperId: Number(createForm.pastPaperId),
                title,
                description,
                capacity,
                timeLimit: Math.round(timeLimitMinutes * 60),
            });

            setIsCreateOpen(false);
            await loadCurrentStudyRoom();
            const needsListReset =
                filters.status !== "WAITING" ||
                filters.sortType !== "LATEST" ||
                page !== 0;

            if (needsListReset) {
                setFilters((current) => ({
                    ...current,
                    status: "WAITING",
                    sortType: "LATEST",
                }));
                setPage(0);
            } else {
                await loadStudyRooms();
            }
        } catch (error) {
            console.error("스터디룸 생성 실패:", error);
            const validationErrors = getValidationErrors(error);

            if (Object.keys(validationErrors).length > 0) {
                setCreateFieldErrors(validationErrors);
                setCreateError("입력한 내용을 다시 확인해 주세요.");
            } else {
                setCreateError(
                    getErrorMessage(error, "스터디룸을 생성하지 못했습니다.")
                );
            }
        } finally {
            setIsCreating(false);
        }
    };

    const openDetailModal = (room) => {
        navigate(`/study-rooms/${room.id}`);
    };

    const closeDetailModal = () => {
        navigate("/study-rooms");
    };

    const handleJoinStudyRoom = async () => {
        if (!detailRoom || isJoining) {
            return;
        }

        setIsJoining(true);
        setJoinError("");

        try {
            const response = await joinStudyRoom(detailRoom.id);
            const joinedStudyRoomId = response?.studyRoomId;

            if (!joinedStudyRoomId) {
                throw new Error(
                    "참가한 스터디룸 정보를 확인하지 못했습니다."
                );
            }

            navigate(`/study-rooms/${joinedStudyRoomId}/waiting`, {
                replace: true,
            });
        } catch (error) {
            console.error("스터디룸 참가 실패:", error);
            setJoinError(
                getErrorMessage(
                    error,
                    "스터디룸에 참가하지 못했습니다."
                )
            );

            if (
                error?.errorCode === "STUDY_004" ||
                error?.errorCode === "STUDY_005" ||
                error?.errorCode === "STUDY_006"
            ) {
                await loadStudyRoomDetail();
            }
        } finally {
            setIsJoining(false);
        }
    };

    return (
        <div className="study-page selected">
            <header className="study-topbar">
                <div className="study-topbar-left">
                    <ExamSelectDropdown
                        exams={exams}
                        selectedExamId={selectedExam?.userExamId}
                        onChange={handleSelectExam}
                        loading={isExamLoading}
                        ariaLabel="스터디 시험 선택"
                    />
                    <h1>스터디</h1>
                </div>
            </header>

            {headerActionsTarget &&
                createPortal(
                    <button
                        type="button"
                        className="study-create-button"
                        onClick={openCreateModal}
                        disabled={!selectedExam}
                    >
                        + 스터디룸 만들기
                    </button>,
                    headerActionsTarget
                )}

            {!isExamLoading && exams.length === 0 && (
                <div className="study-no-exam">
                    <strong>활성화된 시험이 없습니다.</strong>
                    <p>마이페이지에서 학습할 시험을 등록해 주세요.</p>
                </div>
            )}

            {selectedExam && (
            <section className="study-list-area" aria-label="스터디룸 목록">
                {isCurrentRoomLoading && (
                    <div
                        className="study-current-room skeleton"
                        aria-label="현재 참여 중인 스터디룸을 불러오는 중"
                    />
                )}

                {!isCurrentRoomLoading && currentRoomError && (
                    <div className="study-current-room-error">
                        <span>{currentRoomError}</span>
                        <button type="button" onClick={loadCurrentStudyRoom}>
                            다시 시도
                        </button>
                    </div>
                )}

                {!isCurrentRoomLoading &&
                    !currentRoomError &&
                    !currentStudyRoom && (
                        <section className="study-current-room empty">
                            <div className="study-current-room-label">
                                <span>현재 참여 중</span>
                                <strong>참여 없음</strong>
                            </div>
                            <div className="study-current-room-empty-copy">
                                <h2>참가 중인 스터디룸이 없어요</h2>
                                <p>
                                    아래 모집 중인 스터디룸을 살펴보거나 새로운
                                    스터디룸을 만들어 보세요.
                                </p>
                            </div>
                        </section>
                    )}

                {!isCurrentRoomLoading && currentStudyRoom && (
                <section
                    className="study-current-room"
                    aria-labelledby="study-current-room-title"
                >
                    <div className="study-current-room-label">
                        <span>현재 참여 중</span>
                        <strong>
                            {currentStudyRoom.status === "WAITING"
                                ? "대기 중"
                                : "풀이 중"}
                        </strong>
                        {currentStudyRoom.updated && (
                            <span className="study-current-updated">
                                수정됨
                            </span>
                        )}
                    </div>

                    <div className="study-current-room-copy">
                        <h2 id="study-current-room-title">
                            {currentStudyRoom.title}
                        </h2>
                        <p>{currentStudyRoom.description}</p>
                    </div>

                    <div className="study-detail-badges current">
                        <span>
                            생성 일시
                            <strong>
                                {formatDateTime(currentStudyRoom.createdAt)}
                            </strong>
                        </span>
                        {currentStudyRoom.updated &&
                            currentStudyRoom.updatedAt && (
                                <span>
                                    수정 일시
                                    <strong>
                                        {formatDateTime(
                                            currentStudyRoom.updatedAt
                                        )}
                                    </strong>
                                </span>
                            )}
                        <span>
                            시간 제한
                            <strong>
                                {Math.round(currentStudyRoom.timeLimit / 60)}분
                            </strong>
                        </span>
                        <span>
                            참여 인원
                            <strong>
                                {currentStudyRoom.memberCount} /{" "}
                                {currentStudyRoom.capacity}명
                            </strong>
                        </span>
                    </div>

                    <div className="study-current-room-detail-grid">
                        <section className="study-detail-section">
                            <div className="study-detail-section-heading">
                                <div>
                                    <span>함께 풀 기출문제</span>
                                    <h3>{currentStudyRoom.pastPaper.title}</h3>
                                </div>
                            </div>
                            <dl className="study-paper-meta">
                                <div>
                                    <dt>문항 수</dt>
                                    <dd>
                                        {
                                            currentStudyRoom.pastPaper
                                                .totalItemCount
                                        }
                                        문항
                                    </dd>
                                </div>
                                <div>
                                    <dt>시험일</dt>
                                    <dd>
                                        {formatExamDate(
                                            currentStudyRoom.pastPaper.examDate
                                        )}
                                    </dd>
                                </div>
                            </dl>
                        </section>

                        <section className="study-detail-section members">
                            <div className="study-detail-section-heading">
                                <div>
                                    <span>참가자</span>
                                    <h3>
                                        {currentStudyRoom.memberCount}명 참여 중
                                    </h3>
                                </div>
                                <span className="study-member-capacity">
                                    최대 {currentStudyRoom.capacity}명
                                </span>
                            </div>
                            <ul className="study-detail-member-list">
                                {currentStudyRoom.members.map(
                                    (member, index) => (
                                        <li
                                            key={`${member.nickname}-${index}`}
                                        >
                                            <span className="study-detail-member-avatar">
                                                {member.profileImageUrl ? (
                                                    <img
                                                        src={getProfileImageUrl(
                                                            member.profileImageUrl
                                                        )}
                                                        alt=""
                                                    />
                                                ) : (
                                                    member.nickname.slice(0, 1)
                                                )}
                                            </span>
                                            <strong>{member.nickname}</strong>
                                            <span
                                                className={`study-member-role ${member.role?.toLowerCase()}`}
                                            >
                                                {member.role === "HOST"
                                                    ? "방장"
                                                    : "멤버"}
                                            </span>
                                        </li>
                                    )
                                )}
                            </ul>
                        </section>
                    </div>
                </section>
                )}

                <div className="study-filter-bar">
                    <div className="study-filter-controls">
                        <StudyFilterDropdown
                            label="상태"
                            value={filters.status}
                            options={STATUS_OPTIONS}
                            onChange={(value) =>
                                handleFilterChange("status", value)
                            }
                        />
                        <StudyFilterDropdown
                            label="정렬"
                            value={filters.sortType}
                            options={SORT_OPTIONS}
                            onChange={(value) =>
                                handleFilterChange("sortType", value)
                            }
                        />
                    </div>
                </div>

                {listError && (
                    <div className="study-inline-message error">
                        <span>{listError}</span>
                        <button
                            type="button"
                            onClick={() => loadStudyRooms()}
                        >
                            다시 시도
                        </button>
                    </div>
                )}

                <div className="study-room-grid">
                    {isListLoading &&
                        Array.from({ length: 3 }).map((_, index) => (
                            <div
                                className="study-room-card skeleton"
                                key={index}
                                aria-hidden="true"
                            />
                        ))}

                    {!isListLoading &&
                        rooms.map((room) => (
                            <article className="study-room-card" key={room.id}>
                                <div className="study-room-card-top">
                                    <div className="study-room-card-heading">
                                        <span
                                            className={`study-status ${room.status?.toLowerCase()}`}
                                        >
                                            {STATUS_OPTIONS.find(
                                                (option) =>
                                                    option.value === room.status
                                            )?.label || room.status}
                                        </span>
                                        <span className="study-paper-title">
                                            {room.pastPaperTitle}
                                        </span>
                                    </div>
                                </div>

                                <div className="study-room-card-copy">
                                    <h3>{room.title}</h3>
                                    <p>{room.description}</p>
                                    <div className="study-room-chips">
                                        <span className="study-info-badge">
                                            생성 일시:{" "}
                                            <strong>
                                                {formatDateTime(room.createdAt)}
                                            </strong>
                                        </span>
                                        <span className="study-info-badge">
                                            시간 제한:{" "}
                                            <strong>
                                                {Math.round(
                                                    room.timeLimit / 60
                                                )}
                                                분
                                            </strong>
                                        </span>
                                        <span className="study-info-badge">
                                            모집 인원:{" "}
                                            <strong>{room.memberCount}</strong>
                                            <span>/ {room.capacity}명</span>
                                        </span>
                                    </div>
                                </div>

                                <div className="study-room-card-footer">
                                    <div className="study-host">
                                        <span className="study-host-avatar">
                                            {room.host?.profileImageUrl ? (
                                                <img
                                                    src={getProfileImageUrl(
                                                        room.host.profileImageUrl
                                                    )}
                                                    alt=""
                                                />
                                            ) : (
                                                room.host?.nickname?.slice(0, 1) ||
                                                "A"
                                            )}
                                        </span>
                                        <span>
                                            <small>방장</small>
                                            <strong>
                                                {room.host?.nickname || "사용자"}
                                            </strong>
                                        </span>
                                    </div>

                                    <button
                                        type="button"
                                        className="study-detail-button"
                                        onClick={() => openDetailModal(room)}
                                    >
                                        상세 보기
                                    </button>
                                </div>
                            </article>
                        ))}
                </div>

                {!isListLoading && !listError && rooms.length === 0 && (
                    <div className="study-empty-state">
                        <strong>조건에 맞는 스터디룸이 없습니다.</strong>
                        <p>첫 번째 스터디룸을 만들어 보세요.</p>
                    </div>
                )}

                {pageInfo.totalPages > 0 && (
                    <nav className="study-pagination" aria-label="목록 페이지">
                        <button
                            type="button"
                            className="pagination-arrow"
                            aria-label="이전 페이지"
                            disabled={
                                pageInfo.page === 0 ||
                                pageInfo.first ||
                                isListLoading
                            }
                            onClick={() => setPage((current) => current - 1)}
                        >
                            &lt;
                        </button>
                        {getVisiblePages(
                            pageInfo.page,
                            pageInfo.totalPages
                        ).map((pageNumber) => (
                            <button
                                type="button"
                                key={pageNumber}
                                className={
                                    pageNumber === pageInfo.page
                                        ? "pagination-page active"
                                        : "pagination-page"
                                }
                                aria-label={`${pageNumber + 1}페이지`}
                                aria-current={
                                    pageNumber === pageInfo.page
                                        ? "page"
                                        : undefined
                                }
                                disabled={isListLoading}
                                onClick={() => setPage(pageNumber)}
                            >
                                {pageNumber + 1}
                            </button>
                        ))}
                        <button
                            type="button"
                            className="pagination-arrow"
                            aria-label="다음 페이지"
                            disabled={pageInfo.last || isListLoading}
                            onClick={() => setPage((current) => current + 1)}
                        >
                            &gt;
                        </button>
                    </nav>
                )}
            </section>
            )}

            {isCreateOpen && (
                <div
                    className="study-modal-backdrop"
                    role="presentation"
                    onMouseDown={(event) => {
                        if (event.target === event.currentTarget) {
                            closeCreateModal();
                        }
                    }}
                >
                    <section
                        className="study-modal study-create-modal"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="study-create-title"
                    >
                        <button
                            type="button"
                            className="study-modal-close"
                            aria-label="닫기"
                            onClick={closeCreateModal}
                        >
                            ×
                        </button>

                        <div className="study-modal-heading">
                            <span>새로운 스터디</span>
                            <h2 id="study-create-title">스터디룸 만들기</h2>
                            <p>
                                함께 풀 기출문제와 진행 방식을 정해 주세요.
                            </p>
                        </div>

                        <form
                            className="study-create-form"
                            onSubmit={handleCreateSubmit}
                            noValidate
                        >
                            <label className="study-form-field full">
                                <span>기출문제</span>
                                <select
                                    name="pastPaperId"
                                    value={createForm.pastPaperId}
                                    onChange={handleCreateFormChange}
                                    disabled={isPastPaperLoading}
                                    aria-invalid={Boolean(
                                        createFieldErrors.pastPaperId
                                    )}
                                >
                                    <option value="">
                                        {isPastPaperLoading
                                            ? "기출문제를 불러오는 중..."
                                            : "함께 풀 기출문제를 선택해 주세요"}
                                    </option>
                                    {pastPapers.map((paper) => (
                                        <option
                                            key={paper.pastPaperId}
                                            value={paper.pastPaperId}
                                        >
                                            {getPastPaperTitle(paper)}
                                        </option>
                                    ))}
                                </select>
                                {createFieldErrors.pastPaperId && (
                                    <em className="study-field-error">
                                        {createFieldErrors.pastPaperId}
                                    </em>
                                )}
                            </label>

                            <label className="study-form-field full">
                                <span>제목</span>
                                <input
                                    name="title"
                                    value={createForm.title}
                                    placeholder="예: SQLD 주말 실전 스터디"
                                    onChange={handleCreateFormChange}
                                    aria-invalid={Boolean(
                                        createFieldErrors.title
                                    )}
                                />
                                <small
                                    className={
                                        createForm.title.length > 100
                                            ? "over-limit"
                                            : ""
                                    }
                                >
                                    {createForm.title.length}/100
                                </small>
                                {createFieldErrors.title && (
                                    <em className="study-field-error">
                                        {createFieldErrors.title}
                                    </em>
                                )}
                            </label>

                            <label className="study-form-field full">
                                <span>설명</span>
                                <textarea
                                    name="description"
                                    value={createForm.description}
                                    placeholder="스터디 진행 방식과 함께할 멤버에게 전할 내용을 적어 주세요."
                                    onChange={handleCreateFormChange}
                                    aria-invalid={Boolean(
                                        createFieldErrors.description
                                    )}
                                />
                                <small
                                    className={
                                        createForm.description.length > 500
                                            ? "over-limit"
                                            : ""
                                    }
                                >
                                    {createForm.description.length}/500
                                </small>
                                {createFieldErrors.description && (
                                    <em className="study-field-error">
                                        {createFieldErrors.description}
                                    </em>
                                )}
                            </label>

                            <div className="study-form-row">
                                <label className="study-form-field">
                                    <span>정원</span>
                                    <select
                                        name="capacity"
                                        value={createForm.capacity}
                                        onChange={handleCreateFormChange}
                                        aria-invalid={Boolean(
                                            createFieldErrors.capacity
                                        )}
                                    >
                                        {[2, 3, 4, 5].map((capacity) => (
                                            <option
                                                key={capacity}
                                                value={capacity}
                                            >
                                                {capacity}명
                                            </option>
                                        ))}
                                    </select>
                                    {createFieldErrors.capacity && (
                                        <em className="study-field-error">
                                            {createFieldErrors.capacity}
                                        </em>
                                    )}
                                </label>

                                <label className="study-form-field">
                                    <span>제한 시간</span>
                                    <div className="study-time-input">
                                        <input
                                            type="number"
                                            name="timeLimitMinutes"
                                            value={createForm.timeLimitMinutes}
                                            onChange={handleCreateFormChange}
                                            aria-invalid={Boolean(
                                                createFieldErrors.timeLimitMinutes
                                            )}
                                        />
                                        <span>분</span>
                                    </div>
                                    {createFieldErrors.timeLimitMinutes && (
                                        <em className="study-field-error">
                                            {createFieldErrors.timeLimitMinutes}
                                        </em>
                                    )}
                                </label>
                            </div>

                            {createError && (
                                <p className="study-form-error">{createError}</p>
                            )}

                            {!createForm.pastPaperId && (
                                <p className="study-submit-hint">
                                    기출문제를 선택하면 스터디룸을 생성할 수
                                    있어요.
                                </p>
                            )}

                            <div className="study-modal-actions">
                                <button
                                    type="button"
                                    className="secondary"
                                    onClick={closeCreateModal}
                                    disabled={isCreating}
                                >
                                    취소
                                </button>
                                <button
                                    type="submit"
                                    className="primary"
                                    disabled={
                                        isCreating ||
                                        isPastPaperLoading ||
                                        !createForm.pastPaperId
                                    }
                                >
                                    {isCreating ? "만드는 중..." : "생성"}
                                </button>
                            </div>
                        </form>
                    </section>
                </div>
            )}

            {studyRoomId && (
                <div
                    className="study-modal-backdrop detail"
                    role="presentation"
                    onMouseDown={(event) => {
                        if (event.target === event.currentTarget) {
                            closeDetailModal();
                        }
                    }}
                >
                    <section
                        className="study-modal study-detail-modal"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="study-detail-title"
                    >
                        <button
                            type="button"
                            className="study-modal-close"
                            aria-label="닫기"
                            onClick={closeDetailModal}
                        >
                            ×
                        </button>

                        {isDetailLoading && (
                            <div
                                className="study-detail-loading"
                                aria-label="스터디룸 상세 정보를 불러오는 중"
                            >
                                <span />
                                <span />
                                <span />
                            </div>
                        )}

                        {!isDetailLoading && detailError && (
                            <div className="study-detail-error">
                                <strong>상세 정보를 불러오지 못했습니다.</strong>
                                <p>{detailError}</p>
                                <button
                                    type="button"
                                    onClick={loadStudyRoomDetail}
                                >
                                    다시 시도
                                </button>
                            </div>
                        )}

                        {!isDetailLoading && detailRoom && (
                            <>
                                <div className="study-detail-header">
                                    <div className="study-detail-heading-row">
                                        <span
                                            className={`study-status ${detailRoom.status?.toLowerCase()}`}
                                        >
                                            {STATUS_OPTIONS.find(
                                                (option) =>
                                                    option.value ===
                                                    detailRoom.status
                                            )?.label || detailRoom.status}
                                        </span>
                                        {detailRoom.updated && (
                                            <span className="study-detail-updated">
                                                수정됨
                                            </span>
                                        )}
                                    </div>
                                    <h2 id="study-detail-title">
                                        {detailRoom.title}
                                    </h2>
                                    <p>{detailRoom.description}</p>
                                </div>

                                <div className="study-detail-badges">
                                    <span>
                                        생성 일시
                                        <strong>
                                            {formatDateTime(
                                                detailRoom.createdAt
                                            )}
                                        </strong>
                                    </span>
                                    {detailRoom.updated &&
                                        detailRoom.updatedAt && (
                                            <span>
                                                수정 일시
                                                <strong>
                                                    {formatDateTime(
                                                        detailRoom.updatedAt
                                                    )}
                                                </strong>
                                            </span>
                                        )}
                                    <span>
                                        시간 제한
                                        <strong>
                                            {Math.round(
                                                detailRoom.timeLimit / 60
                                            )}
                                            분
                                        </strong>
                                    </span>
                                    <span>
                                        모집 인원
                                        <strong>
                                            {detailRoom.memberCount} /{" "}
                                            {detailRoom.capacity}명
                                        </strong>
                                    </span>
                                </div>

                                <section className="study-detail-section">
                                    <div className="study-detail-section-heading">
                                        <div>
                                            <span>함께 풀 기출문제</span>
                                            <h3>
                                                {detailRoom.pastPaper.title}
                                            </h3>
                                        </div>
                                    </div>
                                    <dl className="study-paper-meta">
                                        <div>
                                            <dt>문항 수</dt>
                                            <dd>
                                                {
                                                    detailRoom.pastPaper
                                                        .totalItemCount
                                                }
                                                문항
                                            </dd>
                                        </div>
                                        <div>
                                            <dt>시험일</dt>
                                            <dd>
                                                {formatExamDate(
                                                    detailRoom.pastPaper
                                                        .examDate
                                                )}
                                            </dd>
                                        </div>
                                    </dl>
                                </section>

                                <section className="study-detail-section members">
                                    <div className="study-detail-section-heading">
                                        <div>
                                            <span>참가자</span>
                                            <h3>
                                                {detailRoom.memberCount}명 참여
                                                중
                                            </h3>
                                        </div>
                                        <span className="study-member-capacity">
                                            최대 {detailRoom.capacity}명
                                        </span>
                                    </div>
                                    <ul className="study-detail-member-list">
                                        {detailRoom.members.map(
                                            (member, index) => (
                                                <li
                                                    key={`${member.nickname}-${index}`}
                                                >
                                                    <span className="study-detail-member-avatar">
                                                        {member.profileImageUrl ? (
                                                            <img
                                                                src={getProfileImageUrl(
                                                                    member.profileImageUrl
                                                                )}
                                                                alt=""
                                                            />
                                                        ) : (
                                                            member.nickname.slice(
                                                                0,
                                                                1
                                                            )
                                                        )}
                                                    </span>
                                                    <strong>
                                                        {member.nickname}
                                                    </strong>
                                                    <span
                                                        className={`study-member-role ${member.role?.toLowerCase()}`}
                                                    >
                                                        {member.role === "HOST"
                                                            ? "방장"
                                                            : "멤버"}
                                                    </span>
                                                </li>
                                            )
                                        )}
                                    </ul>
                                </section>

                                <div className="study-detail-actions">
                                    <div>
                                        <strong>
                                            {detailRoom.capacity -
                                                detailRoom.memberCount}
                                            자리 남았어요
                                        </strong>
                                        <span>
                                            참가 후 대기방으로 이동합니다.
                                        </span>
                                    </div>
                                    <button
                                        type="button"
                                        className="study-join-button"
                                        onClick={handleJoinStudyRoom}
                                        disabled={
                                            isJoining ||
                                            (detailRoom.status !== "WAITING" &&
                                                detailRoom.status !==
                                                    "FEEDBACK") ||
                                            detailRoom.memberCount >=
                                                detailRoom.capacity
                                        }
                                    >
                                        {isJoining
                                            ? "참가 중..."
                                            : detailRoom.memberCount >=
                                        detailRoom.capacity
                                            ? "모집 완료"
                                            : detailRoom.status === "SOLVING"
                                              ? "풀이 중"
                                            : "스터디 참가"}
                                    </button>
                                </div>
                                {joinError && (
                                    <p
                                        className="study-join-error"
                                        role="alert"
                                    >
                                        {joinError}
                                    </p>
                                )}
                            </>
                        )}

                    </section>
                </div>
            )}
        </div>
    );
}

export default StudyRoomPage;
