import { useState } from "react";
import AiLearning from "./pages/AiLearning.jsx";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import "./App.css";

function getStoredToken() {
    try {
        return window.localStorage.getItem("accessToken");
    } catch (error) {
        console.warn("localStorage 접근이 차단되었습니다.", error);
        return null;
    }
}

function removeStoredToken() {
    try {
        window.localStorage.removeItem("accessToken");
    } catch (error) {
        console.warn("localStorage 삭제가 차단되었습니다.", error);
    }
}

function App() {
    const [page, setPage] = useState(() => {
        const token = getStoredToken();
        return token ? "home" : "login";
    });

    const handleLogout = () => {
        removeStoredToken();
        setPage("login");
    };

    if (page === "signup") {
        return <SignupPage onMoveLogin={() => setPage("login")} />;
    }

    if (page === "login") {
        return (
            <LoginPage
                onLoginSuccess={() => setPage("home")}
                onMoveSignup={() => setPage("signup")}
            />
        );
    }

    return <AiLearning onLogout={handleLogout} />;
}

export default App;