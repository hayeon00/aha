import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    getMyInfo,
    updateProfile,
    updateProfileImage,
} from "../../api/userApi";
import {
    getUserExams,
    updateUserExamHidden,
} from "../../api/exam/userExamApi";
import { logout } from "../../api/auth/authApi";
import "./MyPage.css";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function MyPage() {
    const navigate = useNavigate();
    const profileImageInputRef = useRef(null);

    const [userInfo, setUserInfo] = useState(null);
    const [userExams, setUserExams] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [message, setMessage] = useState("");
    const [updatingExamId, setUpdatingExamId] = useState(null);

    const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);
    const [profileForm, setProfileForm] = useState({
        name: "",
        nickname: "",
    });
    const [isProfileSaving, setIsProfileSaving] = useState(false);
    const [isImageUploading, setIsImageUploading] = useState(false);

    useEffect(() => {
        fetchMyPageData();
    }, []);

    const getApiData = (response) => {
        if (!response) return null;

        if (response.data?.data !== undefined) {
            return response.data.data;
        }

        if (response.data !== undefined) {
            return response.data;
        }

        return response;
    };

    const fetchMyPageData = async () => {
        setIsLoading(true);
        setMessage("");

        let hasError = false;

        try {
            const myInfoResponse = await getMyInfo();
            const myInfo = getApiData(myInfoResponse);
            setUserInfo(myInfo);
        } catch (error) {
            hasError = true;
            console.error("내 정보 조회 실패:", error);
        }

        try {
            const userExamResponse = await getUserExams();
            const exams = getApiData(userExamResponse);
            setUserExams(Array.isArray(exams) ? exams : []);
        } catch (error) {
            hasError = true;
            console.error("내 시험 목록 조회 실패:", error);
        } finally {
            if (hasError) {
                setMessage("일부 마이페이지 정보를 불러오지 못했습니다.");
            }

            setIsLoading(false);
        }
    };

    const profileInitial = useMemo(() => {
        const baseName =
            userInfo?.name ||
            userInfo?.nickname ||
            userInfo?.email ||
            "A";

        return baseName.charAt(0).toUpperCase();
    }, [userInfo]);

    const profileImageSrc = useMemo(() => {
        if (!userInfo?.profileImageUrl) return null;

        if (userInfo.profileImageUrl.startsWith("http")) {
            return userInfo.profileImageUrl;
        }

        return `${API_BASE_URL}${userInfo.profileImageUrl}`;
    }, [userInfo]);

    const formatLastStudiedAt = (value) => {
        if (!value) return "-";

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

        if (isToday) return `오늘 ${time}`;
        if (isYesterday) return `어제 ${time}`;

        return date.toLocaleDateString("ko-KR", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
        });
    };

    const formatCreatedAt = (value) => {
        if (!value) return "-";

        const date = new Date(value);

        return date.toLocaleDateString("ko-KR", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
        });
    };

    const handleToggleExam = async (targetExam) => {
        if (updatingExamId) return;

        const nextHidden = !targetExam.hidden;

        setUpdatingExamId(targetExam.userExamId);
        setMessage("");

        const previousExams = userExams;

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
            setUserExams(previousExams);
            setMessage("시험 표시 설정 변경에 실패했습니다.");
        } finally {
            setUpdatingExamId(null);
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
        if (isProfileSaving) return;
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
            setMessage("프로필 정보 수정에 실패했습니다.");
        } finally {
            setIsProfileSaving(false);
        }
    };

    const handleProfileImageButtonClick = () => {
        if (isImageUploading) return;
        profileImageInputRef.current?.click();
    };

    const handleProfileImageChange = async (event) => {
        const file = event.target.files?.[0];

        if (!file) return;

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
            setMessage("프로필 이미지 수정에 실패했습니다.");
        } finally {
            setIsImageUploading(false);
            event.target.value = "";
        }
    };

    const handleLogout = async () => {
        try {
            await logout();
        } catch (error) {
            console.error("로그아웃 실패:", error);
        } finally {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            navigate("/login", { replace: true });
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
                    <div>
                        <span className="mypage-kicker">MY PAGE</span>
                        <h1>마이페이지</h1>
                        <p>내 학습 설정과 계정 정보를 한 곳에서 관리하세요.</p>
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
                            <h2>학습 시험 설정</h2>
                            <p>표시할 시험을 선택하고 학습 상태를 확인할 수 있습니다.</p>
                        </div>
                    </div>

                    <div className="exam-table">
                        <div className="exam-table-head">
                            <span>시험명</span>
                            <span>학습 상태</span>
                            <span>마지막 학습</span>
                            <span>표시 설정</span>
                        </div>

                        {userExams.length === 0 ? (
                            <div className="exam-empty-row">
                                등록된 시험이 없습니다. 다시 로그인하거나 관리자에게 문의해주세요.
                            </div>
                        ) : (
                            userExams.map((exam) => (
                                <div
                                    className="exam-table-row"
                                    key={exam.userExamId}
                                >
                                    <div className="exam-title-cell">
                                        <strong>
                                            {exam.examCode || exam.examName}
                                        </strong>
                                        <span>
                                            {exam.examName || exam.versionName || "지원 시험"}
                                        </span>
                                    </div>

                                    <div>
                                        <span
                                            className={
                                                exam.hidden
                                                    ? "status-badge muted"
                                                    : "status-badge"
                                            }
                                        >
                                            {exam.hidden ? "숨김" : "학습 가능"}
                                        </span>
                                    </div>

                                    <span className="exam-date">
                                        {formatLastStudiedAt(exam.lastStudiedAt)}
                                    </span>

                                    <div className="exam-toggle-wrap">
                                        <button
                                            type="button"
                                            className={
                                                exam.hidden
                                                    ? "exam-toggle"
                                                    : "exam-toggle active"
                                            }
                                            aria-label="시험 표시 설정"
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

                    <p className="exam-guide">
                        숨긴 시험은 대시보드와 시험 선택 메뉴에 표시되지 않으며,
                        학습 기록은 삭제되지 않습니다.
                    </p>
                </section>

                <section className="mypage-bottom-grid">
                    <section className="mypage-card profile-card">
                        <div className="section-title-row">
                            <div>
                                <h2>프로필 정보</h2>
                                <p>회원님의 기본 계정 정보입니다.</p>
                            </div>

                            <button
                                type="button"
                                className="edit-button"
                                aria-label="프로필 수정"
                                onClick={openProfileModal}
                            >
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
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
                                <p>보안 설정과 계정 작업을 관리합니다.</p>
                            </div>
                        </div>

                        <div className="account-menu">
                            <button type="button" className="account-menu-row">
                                <span className="account-menu-left">
                                    <em className="menu-icon">
                                        <svg width="17" height="17" viewBox="0 0 24 24" fill="none">
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
                                className="account-menu-row"
                                onClick={handleLogout}
                            >
                                <span className="account-menu-left">
                                    <em className="menu-icon">
                                        <svg width="17" height="17" viewBox="0 0 24 24" fill="none">
                                            <path
                                                d="M10 17L15 12L10 7"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                            />
                                            <path
                                                d="M15 12H3"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                            />
                                            <path
                                                d="M21 4V20"
                                                stroke="currentColor"
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                            />
                                        </svg>
                                    </em>
                                    로그아웃
                                </span>
                                <span className="menu-arrow">›</span>
                            </button>

                            <button
                                type="button"
                                className="account-menu-row danger"
                            >
                                <span className="account-menu-left">
                                    <em className="menu-icon danger-icon">
                                        <svg width="17" height="17" viewBox="0 0 24 24" fill="none">
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

            {isProfileModalOpen && (
                <div className="profile-modal-backdrop" onClick={closeProfileModal}>
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

                        <form onSubmit={handleProfileSubmit} className="profile-modal-form">
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