import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMyInfo, updateProfile, updateProfileImage } from "../api/userApi.js";
import { addUserExams, getUserExams, updateUserExamHidden } from "../../exam/api/userExamApi.js";
import { getExams } from "../../exam/api/examApi.js";
import "./MyPage.css";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const getApiData = (response) => {
    if (!response) {
        return null;
    }

    if (response.data?.data !== undefined) {
        return response.data.data;
    }

    if (response.data !== undefined) {
        return response.data;
    }

    return response;
};

function MyPage() {
    const navigate = useNavigate();
    const profileImageInputRef = useRef(null);

    const [userInfo, setUserInfo] = useState(null);
    const [userExams, setUserExams] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [message, setMessage] = useState("");
    const [updatingExamId, setUpdatingExamId] = useState(null);
    const [isExamModalOpen, setIsExamModalOpen] = useState(false);
    const [availableExams, setAvailableExams] = useState([]);
    const [selectedNewExamIds, setSelectedNewExamIds] = useState([]);
    const [isExamCatalogLoading, setIsExamCatalogLoading] = useState(false);
    const [isAddingExams, setIsAddingExams] = useState(false);

    const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);
    const [profileForm, setProfileForm] = useState({
        name: "",
        nickname: "",
    });
    const [isProfileSaving, setIsProfileSaving] = useState(false);
    const [isImageUploading, setIsImageUploading] = useState(false);

    const isUnauthorizedError = (error) => {
        return error.response?.status === 401;
    };

    const handleUnauthorized = useCallback(() => {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");

        navigate("/login", { replace: true });
    }, [navigate]);

    const fetchMyPageData = useCallback(async () => {
        setIsLoading(true);
        setMessage("");

        try {
            const [myInfoResponse, userExamResponse] = await Promise.all([
                getMyInfo(),
                getUserExams(),
            ]);

            const myInfo = getApiData(myInfoResponse);
            const exams = getApiData(userExamResponse);

            setUserInfo(myInfo);
            setUserExams(Array.isArray(exams) ? exams : []);
        } catch (error) {
            console.error("마이페이지 정보 조회 실패:", error);

            if (isUnauthorizedError(error)) {
                handleUnauthorized();
                return;
            }

            setUserInfo(null);
            setUserExams([]);
            setMessage("마이페이지 정보를 불러오지 못했습니다.");
        } finally {
            setIsLoading(false);
        }
    }, [handleUnauthorized]);

    useEffect(() => {
        queueMicrotask(() => {
            fetchMyPageData();
        });
    }, [fetchMyPageData]);

    const profileInitial = useMemo(() => {
        const baseName =
            userInfo?.name ||
            userInfo?.nickname ||
            userInfo?.email ||
            "A";

        return baseName.charAt(0).toUpperCase();
    }, [userInfo]);

    const profileImageSrc = useMemo(() => {
        if (!userInfo?.profileImageUrl) {
            return null;
        }

        if (userInfo.profileImageUrl.startsWith("http")) {
            return userInfo.profileImageUrl;
        }

        return `${API_BASE_URL}${userInfo.profileImageUrl}`;
    }, [userInfo]);

    const formatLastStudiedAt = (value) => {
        if (!value) {
            return "-";
        }

        const date = new Date(value);
        const now = new Date();

        const isToday =
            date.getFullYear() === now.getFullYear() &&
            date.getMonth() === now.getMonth() &&
            date.getDate() === now.getDate();

        const yesterday = new Date(now);
        yesterday.setDate(now.getDate() - 1);

        const isYesterday =
            date.getFullYear() === yesterday.getFullYear() &&
            date.getMonth() === yesterday.getMonth() &&
            date.getDate() === yesterday.getDate();

        const time = date.toLocaleTimeString("ko-KR", {
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
        });

        if (isToday) {
            return `오늘 ${time}`;
        }

        if (isYesterday) {
            return `어제 ${time}`;
        }

        return date.toLocaleDateString("ko-KR", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
        });
    };

    const formatCreatedAt = (value) => {
        if (!value) {
            return "-";
        }

        const date = new Date(value);

        return date.toLocaleDateString("ko-KR", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
        });
    };

    const handleToggleExam = async (targetExam) => {
        if (updatingExamId) {
            return;
        }

        const nextHidden = !targetExam.hidden;
        const previousExams = userExams;

        setUpdatingExamId(targetExam.userExamId);
        setMessage("");

        setUserExams((prev) =>
            prev.map((exam) =>
                exam.userExamId === targetExam.userExamId
                    ? { ...exam, hidden: nextHidden }
                    : exam
            )
        );

        try {
            const response = await updateUserExamHidden(
                targetExam.userExamId,
                nextHidden
            );

            const updatedExam = getApiData(response);

            if (updatedExam) {
                setUserExams((prev) =>
                    prev.map((exam) =>
                        exam.userExamId === updatedExam.userExamId
                            ? updatedExam
                            : exam
                    )
                );
            }
        } catch (error) {
            console.error("시험 표시 설정 변경 실패:", error);

            if (isUnauthorizedError(error)) {
                handleUnauthorized();
                return;
            }

            setUserExams(previousExams);
            setMessage("시험 표시 설정 변경에 실패했습니다.");
        } finally {
            setUpdatingExamId(null);
        }
    };

    const openExamModal = async () => {
        setIsExamModalOpen(true);
        setSelectedNewExamIds([]);
        setIsExamCatalogLoading(true);
        setMessage("");

        try {
            const response = await getExams();
            const supportedExams = getApiData(response);
            const registeredExamIds = new Set(userExams.map((exam) => exam.examId));

            setAvailableExams(
                Array.isArray(supportedExams)
                    ? supportedExams.filter((exam) =>
                        exam.activeVersionId && !registeredExamIds.has(exam.id)
                    )
                    : []
            );
        } catch (error) {
            console.error("추가 가능한 시험 조회 실패:", error);
            if (isUnauthorizedError(error)) {
                handleUnauthorized();
                return;
            }
            setAvailableExams([]);
            setMessage("추가 가능한 시험을 불러오지 못했습니다.");
        } finally {
            setIsExamCatalogLoading(false);
        }
    };

    const closeExamModal = () => {
        if (isAddingExams) return;
        setIsExamModalOpen(false);
    };

    const toggleNewExam = (examId) => {
        setSelectedNewExamIds((current) => current.includes(examId)
            ? current.filter((id) => id !== examId)
            : [...current, examId]);
    };

    const handleAddExams = async () => {
        if (selectedNewExamIds.length === 0 || isAddingExams) return;

        try {
            setIsAddingExams(true);
            setMessage("");
            const response = await addUserExams(selectedNewExamIds);
            const addedExams = getApiData(response);

            if (Array.isArray(addedExams)) {
                setUserExams((current) => [...current, ...addedExams]
                    .sort((a, b) => a.examId - b.examId));
            }

            setIsExamModalOpen(false);
            setSelectedNewExamIds([]);
        } catch (error) {
            console.error("내 시험 추가 실패:", error);
            if (isUnauthorizedError(error)) {
                handleUnauthorized();
                return;
            }
            setMessage(error.response?.data?.message ?? "시험을 추가하지 못했습니다.");
        } finally {
            setIsAddingExams(false);
        }
    };

    const openProfileModal = () => {
        setProfileForm({
            name: userInfo?.name || "",
            nickname: userInfo?.nickname || "",
        });
        setIsProfileModalOpen(true);
        setMessage("");
    };

    const closeProfileModal = () => {
        if (isProfileSaving) {
            return;
        }

        setIsProfileModalOpen(false);
    };

    const handleProfileFormChange = (event) => {
        const { name, value } = event.target;

        setProfileForm((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleProfileSubmit = async (event) => {
        event.preventDefault();

        const trimmedName = profileForm.name.trim();
        const trimmedNickname = profileForm.nickname.trim();

        if (!trimmedName || !trimmedNickname) {
            setMessage("이름과 닉네임을 모두 입력해주세요.");
            return;
        }

        try {
            setIsProfileSaving(true);
            setMessage("");

            const response = await updateProfile({
                name: trimmedName,
                nickname: trimmedNickname,
            });

            const updatedUserInfo = getApiData(response);

            if (updatedUserInfo) {
                setUserInfo(updatedUserInfo);
            }

            setIsProfileModalOpen(false);
        } catch (error) {
            console.error("프로필 정보 수정 실패:", error);

            if (isUnauthorizedError(error)) {
                handleUnauthorized();
                return;
            }

            setMessage("프로필 정보 수정에 실패했습니다.");
        } finally {
            setIsProfileSaving(false);
        }
    };

    const handleProfileImageButtonClick = () => {
        if (isImageUploading) {
            return;
        }

        profileImageInputRef.current?.click();
    };

    const handleProfileImageChange = async (event) => {
        const file = event.target.files?.[0];

        if (!file) {
            return;
        }

        try {
            setIsImageUploading(true);
            setMessage("");

            const response = await updateProfileImage(file);
            const updatedUserInfo = getApiData(response);

            if (updatedUserInfo) {
                setUserInfo(updatedUserInfo);
            }
        } catch (error) {
            console.error("프로필 이미지 수정 실패:", error);

            if (isUnauthorizedError(error)) {
                handleUnauthorized();
                return;
            }

            setMessage("프로필 이미지 수정에 실패했습니다.");
        } finally {
            setIsImageUploading(false);
            event.target.value = "";
        }
    };


    if (isLoading) {
        return (
            <main className="mypage">
                <div className="mypage-loading-card">
                    <div className="mypage-spinner" />
                    <p>마이페이지 정보를 불러오는 중입니다...</p>
                </div>
            </main>
        );
    }

    return (
        <main className="mypage">
            <section className="mypage-shell">
                <header className="mypage-header">
                    <div className="mypage-heading-copy">
                        <span className="mypage-kicker">계정 및 학습 설정</span>
                        <h1>마이페이지</h1>
                    </div>
                </header>

                {message && (
                    <div className="mypage-message">
                        {message}
                    </div>
                )}

                <section className="mypage-card exam-setting-card">
                    <div className="section-title-row">
                        <div>
                            <h2>내 시험 관리</h2>
                            <p>선택한 시험을 관리하고 학습 화면 표시 여부를 설정하세요.</p>
                        </div>

                        <button type="button" className="exam-add-button" onClick={openExamModal}>
                            <span aria-hidden="true">+</span> 시험 추가
                        </button>
                    </div>

                    <div className="my-exam-grid">
                        {userExams.length === 0 ? (
                            <div className="my-exam-empty">
                                <span aria-hidden="true">＋</span>
                                <strong>아직 선택한 시험이 없어요</strong>
                                <p>준비할 시험을 추가하고 Aha 학습을 시작해보세요.</p>
                                <button type="button" onClick={openExamModal}>시험 선택하기</button>
                            </div>
                        ) : (
                            userExams.map((exam) => (
                                <div
                                    className={exam.hidden ? "my-exam-card is-inactive" : "my-exam-card"}
                                    key={exam.userExamId}
                                >
                                    <div className="my-exam-card-top">
                                        <span className="my-exam-symbol" aria-hidden="true">
                                            {(exam.examCode || exam.examName || "A").slice(0, 2)}
                                        </span>
                                        <span className={exam.hidden ? "status-badge muted" : "status-badge"}>
                                            {exam.hidden ? "비활성" : "활성"}
                                        </span>
                                    </div>

                                    <div className="my-exam-copy">
                                        <strong>{exam.examName || exam.examCode}</strong>
                                        <span>{exam.versionName || "최신 시험 버전"}</span>
                                    </div>

                                    <div className="my-exam-card-footer">
                                        <span>최근 학습 {formatLastStudiedAt(exam.lastStudiedAt)}</span>
                                        <button
                                            type="button"
                                            className={exam.hidden ? "exam-toggle" : "exam-toggle active"}
                                            aria-label={`${exam.examName} ${exam.hidden ? "활성화" : "비활성화"}`}
                                            aria-pressed={!exam.hidden}
                                            disabled={updatingExamId === exam.userExamId}
                                            onClick={() => handleToggleExam(exam)}
                                        >
                                            <span />
                                        </button>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </section>

                <section className="mypage-bottom-grid">
                    <section className="mypage-card profile-card">
                        <div className="section-title-row">
                            <div>
                                <h2>프로필 정보</h2>
                            </div>

                            <button
                                type="button"
                                className="edit-button"
                                aria-label="프로필 수정"
                                onClick={openProfileModal}
                            >
                                <svg
                                    width="18"
                                    height="18"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                >
                                    <path
                                        d="M4 20H8L18.5 9.5C19.6 8.4 19.6 6.6 18.5 5.5C17.4 4.4 15.6 4.4 14.5 5.5L4 16V20Z"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinejoin="round"
                                    />
                                    <path
                                        d="M13.5 6.5L17.5 10.5"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                </svg>
                            </button>
                        </div>

                        <div className="profile-content">
                            <div className="profile-avatar-wrap">
                                <button
                                    type="button"
                                    className={
                                        isImageUploading
                                            ? "profile-avatar-button uploading"
                                            : "profile-avatar-button"
                                    }
                                    onClick={handleProfileImageButtonClick}
                                    disabled={isImageUploading}
                                    aria-label="프로필 이미지 변경"
                                >
                                    {profileImageSrc ? (
                                        <img
                                            src={profileImageSrc}
                                            alt="프로필 이미지"
                                        />
                                    ) : (
                                        <span>{profileInitial}</span>
                                    )}

                                    <em>
                                        {isImageUploading ? "업로드 중" : "변경"}
                                    </em>
                                </button>

                                <input
                                    ref={profileImageInputRef}
                                    type="file"
                                    accept="image/jpeg,image/png,image/webp"
                                    className="profile-image-input"
                                    onChange={handleProfileImageChange}
                                />

                                <strong>
                                    {userInfo?.name ||
                                        userInfo?.nickname ||
                                        "사용자"}
                                </strong>
                            </div>

                            <div className="profile-info-list">
                                <div className="profile-info-item">
                                    <span>이메일</span>
                                    <strong>{userInfo?.email || "-"}</strong>
                                </div>

                                <div className="profile-info-item">
                                    <span>닉네임</span>
                                    <strong>{userInfo?.nickname || "-"}</strong>
                                </div>

                                <div className="profile-info-item">
                                    <span>회원가입일</span>
                                    <strong>
                                        {formatCreatedAt(userInfo?.createdAt)}
                                    </strong>
                                </div>
                            </div>
                        </div>
                    </section>

                    <section className="mypage-card account-card">
                        <div className="section-title-row">
                            <div>
                                <h2>계정 관리</h2>
                            </div>
                        </div>

                        <div className="account-menu">
                            <button
                                type="button"
                                className="account-menu-row"
                                onClick={() =>
                                    setMessage("비밀번호 변경 기능은 준비 중입니다.")
                                }
                            >
            <span className="account-menu-left">
                <em className="menu-icon">
                    <svg
                        width="17"
                        height="17"
                        viewBox="0 0 24 24"
                        fill="none"
                    >
                        <path
                            d="M7 11V8C7 5.2 9.2 3 12 3C14.8 3 17 5.2 17 8V11"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinecap="round"
                        />
                        <path
                            d="M6 11H18V20H6V11Z"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinejoin="round"
                        />
                    </svg>
                </em>
                비밀번호 변경
            </span>
                                <span className="menu-arrow">›</span>
                            </button>

                            <button
                                type="button"
                                className="account-menu-row danger"
                                onClick={() =>
                                    setMessage("회원 탈퇴 기능은 준비 중입니다.")
                                }
                            >
                                <span className="account-menu-left">
                                    <em className="menu-icon danger-icon">
                                        <svg
                                            width="17"
                                            height="17"
                                            viewBox="0 0 24 24"
                                            fill="none"
                                        >
                                            <path
                                                d="M5 7H19"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                            />
                                            <path
                                                d="M10 11V17"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                            />
                                            <path
                                                d="M14 11V17"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                            />
                                            <path
                                                d="M8 7L9 4H15L16 7"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinejoin="round"
                                            />
                                            <path
                                                d="M7 7L8 20H16L17 7"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinejoin="round"
                                            />
                                        </svg>
                                    </em>
                                    회원 탈퇴
                                </span>
                                <span className="menu-arrow">›</span>
                            </button>
                        </div>
                    </section>
                </section>
            </section>

            {isExamModalOpen && (
                <div className="profile-modal-backdrop" onClick={closeExamModal}>
                    <section className="exam-manager-modal" onClick={(event) => event.stopPropagation()} role="dialog" aria-modal="true" aria-labelledby="exam-manager-title">
                        <header className="profile-modal-header">
                            <div>
                                <h2 id="exam-manager-title">시험 추가</h2>
                                <p>새롭게 학습할 시험을 선택해주세요. 복수 선택도 가능해요.</p>
                            </div>
                            <button type="button" className="profile-modal-close" onClick={closeExamModal} aria-label="닫기">×</button>
                        </header>

                        {isExamCatalogLoading ? (
                            <div className="exam-catalog-loading"><div className="mypage-spinner" /><span>시험 목록을 불러오는 중이에요.</span></div>
                        ) : availableExams.length === 0 ? (
                            <div className="exam-catalog-empty"><strong>추가 가능한 시험이 없어요</strong><span>현재 지원하는 시험을 모두 추가했습니다.</span></div>
                        ) : (
                            <div className="exam-catalog-grid">
                                {availableExams.map((exam) => {
                                    const selected = selectedNewExamIds.includes(exam.id);
                                    return (
                                        <button key={exam.id} type="button" className={selected ? "exam-catalog-card selected" : "exam-catalog-card"} onClick={() => toggleNewExam(exam.id)} aria-pressed={selected}>
                                            <span className="exam-catalog-symbol" aria-hidden="true">{exam.code.slice(0, 2)}</span>
                                            <span><strong>{exam.name}</strong><small>{exam.versionName || "최신 버전"}</small></span>
                                            <i aria-hidden="true">✓</i>
                                        </button>
                                    );
                                })}
                            </div>
                        )}

                        <div className="exam-manager-actions">
                            <button type="button" className="modal-cancel-button" onClick={closeExamModal} disabled={isAddingExams}>취소</button>
                            <button type="button" className="modal-save-button" onClick={handleAddExams} disabled={selectedNewExamIds.length === 0 || isAddingExams}>
                                {isAddingExams ? "추가 중..." : `${selectedNewExamIds.length || ""}${selectedNewExamIds.length ? "개 " : ""}추가하기`}
                            </button>
                        </div>
                    </section>
                </div>
            )}

            {isProfileModalOpen && (
                <div
                    className="profile-modal-backdrop"
                    onClick={closeProfileModal}
                >
                    <section
                        className="profile-modal"
                        onClick={(event) => event.stopPropagation()}
                    >
                        <header className="profile-modal-header">
                            <div>
                                <h2>프로필 수정</h2>
                                <p>이름과 닉네임을 변경할 수 있습니다.</p>
                            </div>

                            <button
                                type="button"
                                className="profile-modal-close"
                                onClick={closeProfileModal}
                                aria-label="닫기"
                            >
                                ×
                            </button>
                        </header>

                        <form
                            onSubmit={handleProfileSubmit}
                            className="profile-modal-form"
                        >
                            <label>
                                <span>이름</span>
                                <input
                                    name="name"
                                    value={profileForm.name}
                                    onChange={handleProfileFormChange}
                                    maxLength={50}
                                    placeholder="이름을 입력해주세요"
                                />
                            </label>

                            <label>
                                <span>닉네임</span>
                                <input
                                    name="nickname"
                                    value={profileForm.nickname}
                                    onChange={handleProfileFormChange}
                                    maxLength={50}
                                    placeholder="닉네임을 입력해주세요"
                                />
                            </label>

                            <div className="profile-modal-actions">
                                <button
                                    type="button"
                                    className="modal-cancel-button"
                                    onClick={closeProfileModal}
                                    disabled={isProfileSaving}
                                >
                                    취소
                                </button>

                                <button
                                    type="submit"
                                    className="modal-save-button"
                                    disabled={isProfileSaving}
                                >
                                    {isProfileSaving ? "저장 중..." : "저장하기"}
                                </button>
                            </div>
                        </form>
                    </section>
                </div>
            )}
        </main>
    );
}

export default MyPage;
