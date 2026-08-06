import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
    changeStudyRoomHost,
    getStudyRoom,
    kickStudyRoomMember,
    leaveStudyRoom,
    updateStudyRoomReady,
} from "../api/studyRoomApi.js";
import "./StudyRoomWaitingPage.css";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const ERROR_MESSAGES = {
    STUDY_003: "스터디룸을 찾을 수 없습니다.",
    STUDY_005: "취소된 스터디룸입니다.",
    STUDY_007: "이미 풀이가 시작된 스터디룸입니다.",
    STUDY_008: "이 스터디룸에 참여하고 있지 않습니다.",
    STUDY_010: "이미 피드백이 시작된 스터디룸입니다.",
    STUDY_011: "방장만 사용할 수 있는 기능입니다.",
    STUDY_012: "방장은 자신을 강퇴할 수 없습니다.",
    STUDY_013: "대상 멤버를 찾을 수 없습니다.",
    STUDY_014: "방장 본인에게 권한을 위임할 수 없습니다.",
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

const getErrorMessage = (error, fallback) =>
    ERROR_MESSAGES[error?.errorCode] ||
    error?.response?.data?.message ||
    error?.message ||
    fallback;

const formatDate = (value) => {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    const pad = (number) => String(number).padStart(2, "0");

    return `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(
        date.getDate()
    )} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

function DashboardIcon({ name }) {
    const paths = {
        file: (
            <>
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z" />
                <path d="M14 2v6h6" />
                <path d="M8 13h8M8 17h6" />
            </>
        ),
        clock: (
            <>
                <circle cx="12" cy="12" r="9" />
                <path d="M12 7v5l3 2" />
            </>
        ),
        calendar: (
            <>
                <rect width="18" height="18" x="3" y="4" rx="2" />
                <path d="M16 2v4M8 2v4M3 10h18" />
            </>
        ),
    };

    return (
        <svg
            className="waiting-info-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
        >
            {paths[name]}
        </svg>
    );
}

function MemberAvatar({ member }) {
    const imageUrl = getProfileImageUrl(member.profileImageUrl);

    return (
        <span className="waiting-member-avatar" aria-hidden="true">
            {imageUrl ? (
                <img src={imageUrl} alt="" />
            ) : (
                member.nickname?.slice(0, 1) || "?"
            )}
        </span>
    );
}

function StudyRoomWaitingPage() {
    const navigate = useNavigate();
    const { studyRoomId } = useParams();
    const [room, setRoom] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");
    const [actionError, setActionError] = useState("");
    const [pendingAction, setPendingAction] = useState("");

    const loadRoom = useCallback(async () => {
        setError("");

        try {
            const data = await getStudyRoom(studyRoomId);
            setRoom(data);
        } catch (requestError) {
            setError(
                getErrorMessage(
                    requestError,
                    "대기방 정보를 불러오지 못했습니다."
                )
            );
        }
    }, [studyRoomId]);

    useEffect(() => {
        let active = true;

        getStudyRoom(studyRoomId)
            .then((data) => {
                if (active) {
                    setRoom(data);
                    setError("");
                }
            })
            .catch((requestError) => {
                if (active) {
                    setError(
                        getErrorMessage(
                            requestError,
                            "대기방 정보를 불러오지 못했습니다."
                        )
                    );
                }
            })
            .finally(() => {
                if (active) {
                    setIsLoading(false);
                }
            });

        return () => {
            active = false;
        };
    }, [studyRoomId]);

    const me = useMemo(
        () => room?.members?.find((member) => member.me),
        [room]
    );
    const isHost = me?.role === "HOST";
    const allReady =
        Boolean(room?.members?.length) &&
        room.members.every((member) => member.ready);

    const runAction = async (actionKey, action, fallback) => {
        if (pendingAction) {
            return;
        }

        setPendingAction(actionKey);
        setActionError("");

        try {
            await action();
            await loadRoom();
        } catch (requestError) {
            setActionError(getErrorMessage(requestError, fallback));
        } finally {
            setPendingAction("");
        }
    };

    const handleReady = () =>
        runAction(
            "ready",
            () => updateStudyRoomReady(!me.ready),
            "준비 상태를 변경하지 못했습니다."
        );

    const handleLeave = async () => {
        if (!window.confirm("스터디룸에서 나가시겠습니까?")) {
            return;
        }

        setPendingAction("leave");
        setActionError("");
        try {
            await leaveStudyRoom(room.id);
            navigate("/study-rooms", { replace: true });
        } catch (requestError) {
            setActionError(
                getErrorMessage(requestError, "스터디룸에서 나가지 못했습니다.")
            );
            setPendingAction("");
        }
    };

    const handleKick = (member) => {
        if (!window.confirm(`${member.nickname} 님을 강퇴하시겠습니까?`)) {
            return;
        }

        return runAction(
            `kick-${member.memberId}`,
            () => kickStudyRoomMember(room.id, member.memberId),
            "멤버를 강퇴하지 못했습니다."
        );
    };

    const handleDelegate = (member) => {
        if (
            !window.confirm(
                `${member.nickname} 님에게 방장 권한을 위임하시겠습니까?`
            )
        ) {
            return;
        }

        return runAction(
            `host-${member.memberId}`,
            () => changeStudyRoomHost(room.id, member.memberId),
            "방장 권한을 위임하지 못했습니다."
        );
    };

    if (isLoading) {
        return (
            <main className="study-waiting-page">
                <section className="waiting-state-card">대기방을 불러오는 중입니다.</section>
            </main>
        );
    }

    if (error || !room || !me) {
        return (
            <main className="study-waiting-page">
                <section className="waiting-state-card error">
                    <strong>{error || "대기방에 참여한 사용자만 입장할 수 있습니다."}</strong>
                    <div>
                        <button type="button" onClick={() => navigate("/study-rooms")}>
                            목록으로
                        </button>
                        {error && (
                            <button type="button" onClick={loadRoom}>
                                다시 시도
                            </button>
                        )}
                    </div>
                </section>
            </main>
        );
    }

    return (
        <main className="study-waiting-page">
            <div className="waiting-room-shell">
                <header className="waiting-room-header">
                    <button
                        type="button"
                        className="waiting-back-button"
                        onClick={() => navigate("/study-rooms")}
                    >
                        스터디 목록
                    </button>
                    <div className="waiting-heading-copy">
                        <div className="waiting-heading-badges">
                            <span className="waiting-status-badge">대기 중</span>
                            {room.updated && <span>수정됨</span>}
                        </div>
                        <h1>{room.title}</h1>
                        <p>{room.description}</p>
                    </div>
                    <div className="waiting-room-count">
                        <strong>{room.memberCount}</strong>
                        <span>/ {room.capacity}명</span>
                        <small>참여 중</small>
                    </div>
                </header>

                <section className="waiting-overview">
                    <div className="waiting-paper-card">
                        <span className="waiting-section-label">함께 풀 기출문제</span>
                        <h2>{room.pastPaper?.title}</h2>
                        <dl>
                            <div>
                                <DashboardIcon name="file" />
                                <span>
                                    <dt>문항 수</dt>
                                    <dd>{room.pastPaper?.totalItemCount}문항</dd>
                                </span>
                            </div>
                            <div>
                                <DashboardIcon name="clock" />
                                <span>
                                    <dt>제한 시간</dt>
                                    <dd>{Math.round(room.timeLimit / 60)}분</dd>
                                </span>
                            </div>
                            <div>
                                <DashboardIcon name="calendar" />
                                <span>
                                    <dt>생성 일시</dt>
                                    <dd>{formatDate(room.createdAt)}</dd>
                                </span>
                            </div>
                        </dl>
                    </div>

                    <aside className="waiting-my-card">
                        <span className="waiting-section-label">내 준비 상태</span>
                        <div className="waiting-my-profile">
                            <MemberAvatar member={me} />
                                    <div>
                                        <strong>{me.nickname}</strong>
                                        <span className={`waiting-role-badge ${isHost ? "host" : ""}`}>
                                            {isHost ? "방장" : "멤버"}
                                        </span>
                            </div>
                        </div>
                        <button
                            type="button"
                            className={`waiting-ready-button ${me.ready ? "ready" : ""}`}
                            onClick={handleReady}
                            disabled={Boolean(pendingAction)}
                        >
                            {pendingAction === "ready"
                                ? "변경 중..."
                                : me.ready
                                  ? "준비 취소"
                                  : "준비 완료"}
                        </button>
                        <p>
                            {me.ready
                                ? "준비가 완료되었습니다."
                                : "모든 참가자가 준비하면 풀이를 시작할 수 있습니다."}
                        </p>
                    </aside>
                </section>

                <section className="waiting-members-section">
                    <div className="waiting-section-heading">
                        <div>
                            <span className="waiting-section-label">참가자</span>
                            <h2>함께 기다리는 멤버</h2>
                        </div>
                        <span className={`waiting-ready-summary ${allReady ? "complete" : ""}`}>
                            준비 {room.members.filter((member) => member.ready).length} /{" "}
                            {room.memberCount}
                        </span>
                    </div>

                    <ul className="waiting-member-list">
                        {room.members.map((member) => (
                            <li key={member.memberId}>
                                <div className="waiting-member-identity">
                                    <MemberAvatar member={member} />
                                    <div>
                                        <div className="waiting-member-name">
                                            <strong>{member.nickname}</strong>
                                            {member.me && <span className="waiting-me-badge">나</span>}
                                        </div>
                                        <span
                                            className={`waiting-role-badge ${
                                                member.role === "HOST" ? "host" : ""
                                            }`}
                                        >
                                            {member.role === "HOST" ? "방장" : "멤버"}
                                        </span>
                                    </div>
                                </div>

                                <div className="waiting-member-controls">
                                    <span
                                        className={`waiting-member-ready ${member.ready ? "ready" : ""}`}
                                    >
                                        {member.ready ? "준비 완료" : "준비 전"}
                                    </span>
                                    {isHost && !member.me && (
                                        <div className="waiting-host-actions">
                                            <button
                                                type="button"
                                                onClick={() => handleDelegate(member)}
                                                disabled={Boolean(pendingAction)}
                                            >
                                                {pendingAction === `host-${member.memberId}`
                                                    ? "위임 중..."
                                                    : "방장 위임"}
                                            </button>
                                            <button
                                                type="button"
                                                className="danger"
                                                onClick={() => handleKick(member)}
                                                disabled={Boolean(pendingAction)}
                                            >
                                                {pendingAction === `kick-${member.memberId}`
                                                    ? "강퇴 중..."
                                                    : "강퇴"}
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </li>
                        ))}
                    </ul>
                </section>

                <footer className="waiting-room-footer">
                    <div>
                        <strong>
                            {allReady
                                ? "모든 참가자가 준비되었습니다."
                                : "아직 준비하지 않은 참가자가 있습니다."}
                        </strong>
                        <span>
                            풀이 시작 기능은 백엔드 API가 준비되면 연결됩니다.
                        </span>
                    </div>
                    {isHost ? (
                        <button
                            type="button"
                            className="waiting-start-button"
                            disabled
                            title="풀이 시작 API 준비 후 사용할 수 있습니다."
                        >
                            풀이 시작
                        </button>
                    ) : (
                        <button
                            type="button"
                            className="waiting-leave-button"
                            onClick={handleLeave}
                            disabled={Boolean(pendingAction)}
                        >
                            {pendingAction === "leave" ? "나가는 중..." : "스터디 나가기"}
                        </button>
                    )}
                </footer>

                {actionError && (
                    <p className="waiting-action-error" role="alert">
                        {actionError}
                    </p>
                )}
            </div>
        </main>
    );
}

export default StudyRoomWaitingPage;
