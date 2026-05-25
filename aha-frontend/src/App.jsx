import { Routes, Route, Navigate, useNavigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import AiLearning from "./pages/AiLearning";
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
            <Route path="/" element={<Navigate to="/learning" replace />} />

            <Route
                path="/login"
                element={
                    <LoginPage
                        onLoginSuccess={() => navigate("/learning")}
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
                path="/learning"
                element={
                    <AiLearning onLogout={handleLogout} />
                }
            />

            <Route path="*" element={<Navigate to="/learning" replace />} />
        </Routes>
    );
}

export default App;