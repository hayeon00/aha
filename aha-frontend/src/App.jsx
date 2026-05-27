import { Routes, Route, Navigate, useNavigate } from "react-router-dom";
import LoginPage from "./pages/auth/LoginPage.jsx";
import SignupPage from "./pages/auth/SignupPage.jsx";
import AiLearning from "./pages/ailearn/AiLearning.jsx";
import MainPage from "./pages/MainPage";
import MyPage from "./pages/user/MyPage.jsx";
import MainLayout from "./layouts/MainLayout";
import { logout } from "./api/authApi";

function App() {
    const navigate = useNavigate();

    const isLoggedIn = Boolean(localStorage.getItem("accessToken"));

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

    return (
        <Routes>
            <Route
                path="/"
                element={
                    isLoggedIn ? (
                        <Navigate to="/main" replace />
                    ) : (
                        <Navigate to="/login" replace />
                    )
                }
            />

            <Route
                path="/login"
                element={
                    isLoggedIn ? (
                        <Navigate to="/main" replace />
                    ) : (
                        <LoginPage
                            onLoginSuccess={() =>
                                navigate("/main", { replace: true })
                            }
                            onMoveSignup={() => navigate("/signup")}
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
                            onMoveLogin={() => navigate("/login")}
                        />
                    )
                }
            />

            <Route
                element={
                    isLoggedIn ? (
                        <MainLayout onLogout={handleLogout} />
                    ) : (
                        <Navigate to="/login" replace />
                    )
                }
            >
                <Route path="/main" element={<MainPage />} />
                <Route path="/learning" element={<AiLearning />} />
                <Route path="/mypage" element={<MyPage />} />
            </Route>

            <Route
                path="*"
                element={
                    isLoggedIn ? (
                        <Navigate to="/main" replace />
                    ) : (
                        <Navigate to="/login" replace />
                    )
                }
            />
        </Routes>
    );
}

export default App;