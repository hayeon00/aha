import { Outlet, useLocation, useNavigate } from "react-router-dom";
import "./MainLayout.css";

function MainLayout({ onLogout }) {
    const navigate = useNavigate();
    const location = useLocation();

    const isActive = (path) => {
        return location.pathname === path || location.pathname.startsWith(`${path}/`);
    };

    const handleLogout = () => {
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
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
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
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
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
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
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
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
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
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
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
                    <button type="button" aria-label="알림">
                        <svg width="17" height="17" viewBox="0 0 24 24" fill="none">
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

                    <button
                        type="button"
                        aria-label="마이페이지"
                        onClick={() => navigate("/mypage")}
                    >
                        <svg width="17" height="17" viewBox="0 0 24 24" fill="none">
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
                    </button>
                </header>

                <main className="layout-content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

export default MainLayout;