import {Routes, Route, Navigate, useNavigate,} from "react-router-dom";
import { useState } from "react";
import LoginPage from "../features/auth/pages/LoginPage.jsx";
import SignupPage from "../features/auth/pages/SignupPage.jsx";
import AiLearningPage from "../features/ailearn/pages/AiLearningPage.jsx";
import MainPage from "../features/home/pages/MainPage.jsx";
import MyPage from "../features/user/pages/MyPage.jsx";
import MainLayout from "../common/layouts/MainLayout.jsx";
import { logout } from "../features/auth/api/authApi.js";

const isAccessTokenValid = () => {
    const token = localStorage.getItem("accessToken");

    if (!token) {
        return false;
    }

    try {
        const payload = JSON.parse(
            atob(token.split(".")[1])
        );

        const currentTime = Math.floor(Date.now() / 1000);

        if (!payload.exp || payload.exp <= currentTime) {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            return false;
        }

        return true;
    } catch (error) {
        console.error("토큰 확인 실패:", error);
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        return false;
    }
};

function App() {
    const navigate = useNavigate();

    const [isLoggedIn, setIsLoggedIn] = useState(
        () => isAccessTokenValid()
    );

    const handleLoginSuccess = () => {
        setIsLoggedIn(true);
        navigate("/main", { replace: true });
    };

    const handleLogout = async () => {
        try {
            await logout();
        } catch (error) {
            console.error("로그아웃 실패:", error);
        } finally {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");

            setIsLoggedIn(false);
            navigate("/login", { replace: true });
        }
    };

    return (
        <Routes>
            <Route
                path="/"
                element={
                    <Navigate
                        to={isLoggedIn ? "/main" : "/login"}
                        replace
                    />
                }
            />

            <Route
                path="/login"
                element={
                    isLoggedIn ? (
                        <Navigate to="/main" replace />
                    ) : (
                        <LoginPage
                            onLoginSuccess={handleLoginSuccess}
                            onMoveSignup={() =>
                                navigate("/signup")
                            }
                        />
                    )
                }
            />

            <Route
                path="/signup"
                element={
                    isLoggedIn ? (
                        <Navigate to="/main" replace />
                    ) : (
                        <SignupPage
                            onMoveLogin={() =>
                                navigate("/login")
                            }
                        />
                    )
                }
            />

            <Route
                element={
                    isLoggedIn ? (
                        <MainLayout
                            onLogout={handleLogout}
                        />
                    ) : (
                        <Navigate
                            to="/login"
                            replace
                        />
                    )
                }
            >
                <Route
                    path="/main"
                    element={<MainPage />}
                />
                <Route
                    path="/learning"
                    element={<AiLearningPage />}
                />
                <Route
                    path="/mypage"
                    element={<MyPage />}
                />
            </Route>

            <Route
                path="*"
                element={
                    <Navigate
                        to={
                            isLoggedIn
                                ? "/main"
                                : "/login"
                        }
                        replace
                    />
                }
            />
        </Routes>
    );
}

export default App;