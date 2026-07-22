import { useEffect, useRef, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { getMyInfo } from "../../features/user/api/userApi.js";
import ExamOnboardingModal from "../../features/exam/components/ExamOnboardingModal.jsx";
import "./MainLayout.css";

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

function MainLayout({ onLogout }) {
    const navigate = useNavigate();
    const location = useLocation();
    const profileMenuRef = useRef(null);

    const [userInfo, setUserInfo] = useState(null);
    const [isUserInfoLoading, setIsUserInfoLoading] = useState(true);
    const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
    const [isExamOnboardingOpen, setIsExamOnboardingOpen] = useState(false);

    useEffect(() => {
        let isMounted = true;

        const fetchMyInfo = async () => {
            try {
                const response = await getMyInfo();
                const myInfo = getApiData(response);

                if (!isMounted) {
                    return;
                }

                setUserInfo(myInfo);
                setIsExamOnboardingOpen(myInfo?.examOnboardingCompleted === false);
            } catch (error) {
                if (!isMounted) {
                    return;
                }

                console.error("로그인 사용자 정보 조회 실패:", error);
                setUserInfo(null);
            } finally {
                if (isMounted) {
                    setIsUserInfoLoading(false);
                }
            }
        };

        queueMicrotask(() => {
            fetchMyInfo();
        });

        return () => {
            isMounted = false;
        };
    }, []);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (
                profileMenuRef.current &&
                !profileMenuRef.current.contains(event.target)
            ) {
                setIsProfileMenuOpen(false);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);


    const isActive = (path) => {
        return (
            location.pathname === path ||
            location.pathname.startsWith(`${path}/`)
        );
    };

    const handleLogout = async () => {
        setIsProfileMenuOpen(false);

        if (onLogout) {
            await onLogout();
            return;
        }

        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");

        navigate("/login", {
            replace: true,
        });
    };

    const handleMoveMyPage = () => {
        setIsProfileMenuOpen(false);
        navigate("/mypage");
    };

    const handlePasswordChange = () => {
        setIsProfileMenuOpen(false);
        alert("비밀번호 변경 기능은 준비 중입니다.");
    };

    const getDisplayName = () => {
        if (isUserInfoLoading) {
            return "확인 중";
        }

        return userInfo?.nickname || userInfo?.name || "사용자";
    };

    const getProfileImageUrl = () => {
        const imageUrl = userInfo?.profileImageUrl;

        if (!imageUrl) {
            return null;
        }

        if (
            imageUrl.startsWith("http://") ||
            imageUrl.startsWith("https://")
        ) {
            return imageUrl;
        }

        return `${API_BASE_URL}${imageUrl}`;
    };

    const profileImageUrl = getProfileImageUrl();

    const handleExamOnboardingComplete = () => {
        setIsExamOnboardingOpen(false);
        setUserInfo((current) => current
            ? { ...current, examOnboardingCompleted: true }
            : current);
        navigate("/main", { replace: true });
    };

    return (
        <div className="app-layout">
            {/* 로그인 성공 후 /main 진입 시 서버의 examOnboardingCompleted 값이 false인 경우에만 노출됩니다. */}
            <ExamOnboardingModal
                open={isExamOnboardingOpen}
                onComplete={handleExamOnboardingComplete}
            />
            <aside className="side-menu">
                <button
                    type="button"
                    className="side-logo"
                    onClick={() => navigate("/main")}
                >
                    Aha
                </button>

                <nav className="side-nav">
                    <button
                        type="button"
                        className={isActive("/main") ? "active" : ""}
                        onClick={() => navigate("/main")}
                    >
                        <span className="nav-icon">
                            <svg
                                width="18"
                                height="18"
                                viewBox="0 0 24 24"
                                fill="none"
                                aria-hidden="true"
                            >
                                <path
                                    d="M4 10.5L12 4L20 10.5V20H15V14H9V20H4V10.5Z"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinejoin="round"
                                />
                            </svg>
                        </span>
                        <span>학습 홈</span>
                    </button>

                    <button
                        type="button"
                        className={isActive("/learning") ? "active" : ""}
                        onClick={() => navigate("/learning")}
                    >
                        <span className="nav-icon">
                            <svg
                                width="18"
                                height="18"
                                viewBox="0 0 24 24"
                                fill="none"
                                aria-hidden="true"
                            >
                                <path
                                    d="M5 5.5C5 4.67 5.67 4 6.5 4H20V18.5H6.5C5.67 18.5 5 19.17 5 20V5.5Z"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinejoin="round"
                                />
                                <path
                                    d="M5 20C5 19.17 5.67 18.5 6.5 18.5H20"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                />
                            </svg>
                        </span>
                        <span>개념 학습</span>
                    </button>

                    <button
                        type="button"
                        className={isActive("/problems") ? "active" : ""}
                        onClick={() => navigate("/problems")}
                    >
                        <span className="nav-icon">
                            <svg
                                width="18"
                                height="18"
                                viewBox="0 0 24 24"
                                fill="none"
                                aria-hidden="true"
                            >
                                <path
                                    d="M8 4H16L19 7V20H5V4H8Z"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinejoin="round"
                                />
                                <path
                                    d="M9 11H15"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                />
                                <path
                                    d="M9 15H14"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                />
                            </svg>
                        </span>
                        <span>문제집</span>
                    </button>

                    <button
                        type="button"
                        className={isActive("/wrong-notes") ? "active" : ""}
                        onClick={() => navigate("/wrong-notes")}
                    >
                        <span className="nav-icon">
                            <svg
                                width="18"
                                height="18"
                                viewBox="0 0 24 24"
                                fill="none"
                                aria-hidden="true"
                            >
                                <path
                                    d="M7 4H17C18.1 4 19 4.9 19 6V20L12 17L5 20V6C5 4.9 5.9 4 7 4Z"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinejoin="round"
                                />
                            </svg>
                        </span>
                        <span>오답노트</span>
                    </button>

                    <button
                        type="button"
                        className={isActive("/mypage") ? "active" : ""}
                        onClick={() => navigate("/mypage")}
                    >
                        <span className="nav-icon">
                            <svg
                                width="18"
                                height="18"
                                viewBox="0 0 24 24"
                                fill="none"
                                aria-hidden="true"
                            >
                                <path
                                    d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12Z"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                />
                                <path
                                    d="M5 20C6.2 16.9 8.6 15.4 12 15.4C15.4 15.4 17.8 16.9 19 20"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                />
                            </svg>
                        </span>
                        <span>마이페이지</span>
                    </button>
                </nav>

                <div className="side-bottom">
                    <button type="button" className="guide-button">
                        이용 가이드
                        <span>›</span>
                    </button>
                </div>
            </aside>

            <div className="main-wrap">
                <header className="top-actions">
                    <button
                        type="button"
                        className="notification-button"
                        aria-label="알림"
                    >
                        <svg
                            width="17"
                            height="17"
                            viewBox="0 0 24 24"
                            fill="none"
                            aria-hidden="true"
                        >
                            <path
                                d="M18 9C18 6 16 3.8 13.2 3.2C13.1 2.5 12.6 2 12 2C11.4 2 10.9 2.5 10.8 3.2C8 3.8 6 6 6 9V13.5L4.5 16V17H19.5V16L18 13.5V9Z"
                                stroke="currentColor"
                                strokeWidth="2"
                                strokeLinejoin="round"
                            />
                            <path
                                d="M10 20C10.5 20.6 11.2 21 12 21C12.8 21 13.5 20.6 14 20"
                                stroke="currentColor"
                                strokeWidth="2"
                                strokeLinecap="round"
                            />
                        </svg>
                    </button>

                    <div className="top-profile-area" ref={profileMenuRef}>
                        <button
                            type="button"
                            className={
                                isProfileMenuOpen
                                    ? "user-profile-button active"
                                    : "user-profile-button"
                            }
                            aria-label="프로필 메뉴 열기"
                            aria-expanded={isProfileMenuOpen}
                            onClick={() =>
                                setIsProfileMenuOpen((prev) => !prev)
                            }
                        >
                            <span className="user-profile-image">
                                {profileImageUrl ? (
                                    <img
                                        src={profileImageUrl}
                                        alt={`${getDisplayName()} 프로필`}
                                        onError={(event) => {
                                            event.currentTarget.style.display =
                                                "none";
                                        }}
                                    />
                                ) : (
                                    <svg
                                        width="17"
                                        height="17"
                                        viewBox="0 0 24 24"
                                        fill="none"
                                        aria-hidden="true"
                                    >
                                        <path
                                            d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12Z"
                                            stroke="currentColor"
                                            strokeWidth="2"
                                        />
                                        <path
                                            d="M5 20C6.2 16.9 8.6 15.4 12 15.4C15.4 15.4 17.8 16.9 19 20"
                                            stroke="currentColor"
                                            strokeWidth="2"
                                            strokeLinecap="round"
                                        />
                                    </svg>
                                )}
                            </span>

                            <span className="user-profile-name">
                                {getDisplayName()}님
                            </span>

                            <span className="profile-menu-caret">⌄</span>
                        </button>

                        {isProfileMenuOpen && (
                            <div className="profile-dropdown">
                                <button
                                    type="button"
                                    className="profile-dropdown-item"
                                    onClick={handleMoveMyPage}
                                >
                                    마이페이지
                                </button>

                                <button
                                    type="button"
                                    className="profile-dropdown-item"
                                    onClick={handlePasswordChange}
                                >
                                    비밀번호 변경
                                </button>

                                <div className="profile-dropdown-divider" />

                                <button
                                    type="button"
                                    className="profile-dropdown-item logout"
                                    onClick={handleLogout}
                                >
                                    로그아웃
                                </button>
                            </div>
                        )}
                    </div>
                </header>

                <main className="layout-content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

export default MainLayout;
