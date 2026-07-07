import {
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useNavigate } from "react-router-dom";
import { logout as requestLogout } from "../api/authApi.js";
import { AUTH_EXPIRED_EVENT } from "../../../common/api/axiosInstance.js";
import { AuthContext } from "./AuthContext.js";

export function AuthProvider({ children }) {
    const navigate = useNavigate();

    const [isAuthenticated, setIsAuthenticated] = useState(() => {
        return Boolean(localStorage.getItem("accessToken"));
    });

    const login = useCallback(({ accessToken, refreshToken }) => {
        localStorage.setItem("accessToken", accessToken);

        if (refreshToken) {
            localStorage.setItem("refreshToken", refreshToken);
        }

        setIsAuthenticated(true);
    }, []);

    const clearAuth = useCallback(() => {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        setIsAuthenticated(false);
    }, []);

    const logout = useCallback(async () => {
        try {
            await requestLogout();
        } catch (error) {
            console.error("로그아웃 API 요청 실패:", error);
        } finally {
            clearAuth();
            navigate("/login", { replace: true });
        }
    }, [clearAuth, navigate]);

    useEffect(() => {
        const handleAuthExpired = () => {
            clearAuth();
            navigate("/login", { replace: true });
        };

        window.addEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired);

        return () => {
            window.removeEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired);
        };
    }, [clearAuth, navigate]);

    const value = useMemo(
        () => ({
            isAuthenticated,
            login,
            logout,
            clearAuth,
        }),
        [isAuthenticated, login, logout, clearAuth]
    );

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}