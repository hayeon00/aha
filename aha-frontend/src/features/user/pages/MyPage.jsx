import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMyInfo, updateProfile, updateProfileImage } from "../api/userApi.js";
import { getUserExams } from "../../exam/api/userExamApi.js";
import "./MyPage.css";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

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

        navigate("/main", { replace: true });
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
        const baseName = userInfo?.name || userInfo?.nickname || userInfo?.email || "A";
        return baseName.charAt(0).toUpperCase();
    }, [userInfo]);

    const profileImageSrc = useMemo(() => {
        if (!userInfo?.profileImageUrl) return null;
        if (userInfo.profileImageUrl.startsWith("http")) return userInfo.profileImageUrl;
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
        if (!isImageUploading) profileImageInputRef.current?.click();
    };

    const handleProfileImageChange = async (event) => {
        const file = event.target.files?.[0];
        if (!file) return;
        try {
            setIsImageUploading(true);
            setMessage("");
            const response = await updateProfileImage(file);
            const updatedUserInfo = getApiData(response);
            if (updatedUserInfo) setUserInfo(updatedUserInfo);
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
                <button type="button" className="mypage-back-button" onClick={() => navigate(-1)} aria-label="이전 화면으로 돌아가기">
                    <span aria-hidden="true">←</span> 이전 화면
                </button>
                {message && (
                    <div className="mypage-message">
                        {message}
                    </div>
                )}

                <section className="mypage-card exam-setting-card">
                    <div className="section-title-row">
                        <div>
                            <h2>학습 중인 자격증</h2>
                            <p>메인에서 선택한 자격증과 최근 학습 기록을 확인할 수 있어요.</p>
                        </div>
                        <span className="exam-count">{userExams.length}개 학습 중</span>
                    </div>

                    <div className="my-exam-grid">
                        {userExams.length === 0 ? (
                            <div className="my-exam-empty">
                                <span aria-hidden="true">⌁</span>
                                <strong>아직 학습 중인 자격증이 없어요</strong>
                                <p>메인 화면에서 준비할 자격증을 선택해 주세요.</p>
                                <button type="button" onClick={() => navigate("/main")}>지원 자격증 둘러보기</button>
                            </div>
                        ) : (
                            userExams.map((exam) => (
                                <button
                                    type="button"
                                    className="my-exam-card"
                                    key={exam.userExamId}
                                    onClick={() => {
                                        sessionStorage.setItem("activeUserExamId", String(exam.userExamId));
                                        sessionStorage.setItem("activeExamName", exam.examName || exam.examCode || "자격증 학습");
                                        navigate(`/learning-home?userExamId=${exam.userExamId}`);
                                    }}
                                >
                                    <div className="my-exam-card-top">
                                        <span className="my-exam-symbol" aria-hidden="true">
                                            {(exam.examCode || exam.examName || "A").slice(0, 2)}
                                        </span>
                                        <span className="exam-open-arrow">↗</span>
                                    </div>

                                    <div className="my-exam-copy">
                                        <strong>{exam.examName || exam.examCode}</strong>
                                        <span>{exam.versionName || "최신 시험 버전"}</span>
                                    </div>

                                    <div className="my-exam-card-footer">
                                        <span>최근 학습</span>
                                        <strong>{formatLastStudiedAt(exam.lastStudiedAt)}</strong>
                                    </div>
                                </button>
                            ))
                        )}
                    </div>
                </section>

                <section className="mypage-bottom-grid">
                    <section className="mypage-card profile-card">
                        <div className="profile-hero-content">
                            <button
                                type="button"
                                className={isImageUploading ? "profile-avatar-button uploading" : "profile-avatar-button"}
                                onClick={handleProfileImageButtonClick}
                                disabled={isImageUploading}
                                aria-label="프로필 이미지 변경"
                            >
                                {profileImageSrc ? <img src={profileImageSrc} alt="프로필 이미지" /> : <span>{profileInitial}</span>}
                                <em>{isImageUploading ? "업로드 중" : "변경"}</em>
                            </button>
                            <input
                                ref={profileImageInputRef}
                                type="file"
                                accept="image/jpeg,image/png,image/webp"
                                className="profile-image-input"
                                onChange={handleProfileImageChange}
                            />
                            <div className="profile-hero-copy">
                                <h2>{userInfo?.name || userInfo?.nickname || "사용자"}</h2>
                                <p>{userInfo?.email || "-"}</p>
                                <span>가입일 {formatCreatedAt(userInfo?.createdAt)}</span>
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
                    </section>

                    <section className="mypage-card account-card">
                        <div className="section-title-row">
                            <div>
                                <h2>계정 관리</h2>
                                <p>보안 설정과 계정 상태를 관리하세요.</p>
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
