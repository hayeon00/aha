import { Routes, Route, Navigate, useNavigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import AiLearning from "./pages/AiLearning";
import MainPage from "./pages/MainPage";
import MyPage from "./pages/MyPage";
import { logout } from "./api/authApi";

function App() {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await logout();
        } catch (error) {
            console.error("로그아웃 실패:", error);
        } finally {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            navigate("/login");
        }
    };

    return (
        <Routes>
            <Route path="/" element={<Navigate to="/main" replace />} />

            <Route
                path="/login"
                element={
                    <LoginPage
                        onLoginSuccess={() => navigate("/main")}
                        onMoveSignup={() => navigate("/signup")}
                    />
                }
            />

            <Route
                path="/signup"
                element={
                    <SignupPage
                        onMoveLogin={() => navigate("/login")}
                    />
                }
            />

            <Route
                path="/main"
                element={<MainPage onLogout={handleLogout} />}
            />

            <Route
                path="/learning"
                element={<AiLearning onLogout={handleLogout} />}
            />

            <Route
                path="/mypage"
                element={<MyPage onLogout={handleLogout} />}
            />

            <Route path="*" element={<Navigate to="/main" replace />} />
        </Routes>
    );
}

export default App;