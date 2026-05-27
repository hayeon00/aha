import { useEffect, useRef, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { getMyInfo } from "../api/userApi";
import "./MainLayout.css";

function MainLayout({ onLogout }) {
    const navigate = useNavigate();
    const location = useLocation();
    const dropdownRef = useRef(null);

    const [user, setUser] = useState({
        nickname: "사용자",
        email: "",
    });

    const [isProfileOpen, setIsProfileOpen] = useState(false);

    const getPageTitle = () => {
        if (location.pathname.startsWith("/learning")) {
            return "SQLD · 개념학습";
        }

        if (location.pathname.startsWith("/main")) {
            return "SQLD · 학습 메인";
        }

        if (location.pathname.startsWith("/mypage")) {
            return "마이페이지";
        }

        return "";
    };

    const isActivePath = (path) => {
        return location.pathname.startsWith(path);
    };

    useEffect(() => {
        const fetchMyInfo = async () => {
            try {
                const result = await getMyInfo();

                setUser({
                    nickname: result.data.data.nickname,
                    email: result.data.data.email,
                });
            } catch (error) {
                console.error("내 정보 조회 실패:", error);

                if (error.response?.status === 401) {
                    localStorage.removeItem("accessToken");
                    localStorage.removeItem("refreshToken");
                    navigate("/login", { replace: true });
                }
            }
        };

        fetchMyInfo();
    }, [navigate]);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (
                dropdownRef.current &&
                !dropdownRef.current.contains(event.target)
            ) {
                setIsProfileOpen(false);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);

        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    const handleMoveMyPage = () => {
        setIsProfileOpen(false);
        navigate("/mypage");
    };

    const handleLogoutClick = () => {
        setIsProfileOpen(false);

        if (onLogout) {
            onLogout();
            return;
        }

        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        navigate("/login", { replace: true });
    };

    return (
        <div className="app-layout">
            <header className="main-header">
                <div className="header-left">
                    <button
                        className="logo-button"
                        type="button"
                        onClick={() => navigate("/main")}
                    >
                        Aha!
                    </button>

                    {getPageTitle() && (
                        <span className="header-page-title">
                            {getPageTitle()}
                        </span>
                    )}
                </div>

                <div className="header-right">
                    <nav className="main-nav">
                        <button
                            className={isActivePath("/main") ? "active" : ""}
                            type="button"
                            onClick={() => navigate("/main")}
                        >
                            홈
                        </button>

                        <button
                            className={isActivePath("/learning") ? "active" : ""}
                            type="button"
                            onClick={() => navigate("/learning")}
                        >
                            개념 학습
                        </button>

                        <button type="button">
                            문제집
                        </button>

                        <button type="button">
                            오답노트
                        </button>
                    </nav>

                    <span className="header-divider" />

                    <div className="profile-menu-wrap" ref={dropdownRef}>
                        <button
                            className="nickname-button"
                            type="button"
                            onClick={() => setIsProfileOpen((prev) => !prev)}
                        >
                            {user.nickname}님
                            <span className="nickname-arrow">
                                {isProfileOpen ? "▲" : "▼"}
                            </span>
                        </button>

                        {isProfileOpen && (
                            <div className="profile-dropdown">
                                <button
                                    type="button"
                                    onClick={handleMoveMyPage}
                                >
                                    마이페이지
                                </button>

                                <button
                                    type="button"
                                    onClick={handleLogoutClick}
                                >
                                    로그아웃
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </header>

            <main className="layout-content">
                <Outlet context={{ user }} />
            </main>
        </div>
    );
}

export default MainLayout;