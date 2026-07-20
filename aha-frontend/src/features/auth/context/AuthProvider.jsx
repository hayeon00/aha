import {
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { AuthContext } from "./AuthContext";
import {
    clearAccessToken,
    setAccessToken,
} from "../store/authTokenStore";
import {
    logout as logoutApi,
    reissue,
} from "../api/authApi";

let authInitializationPromise = null;

const requestInitialAccessToken = async () => {
    if (!authInitializationPromise) {
        authInitializationPromise = reissue()
            .then((response) => {
                const token =
                    response.data?.accessToken;

                if (!token) {
                    throw new Error(
                        "재발급 응답에 Access Token이 없습니다."
                    );
                }

                return token;
            })
            .finally(() => {
                authInitializationPromise = null;
            });
    }

    return authInitializationPromise;
};

export default function AuthProvider({ children }) {
    const [accessToken, setAccessTokenState] =
        useState(null);

    const [isAuthInitialized, setIsAuthInitialized] =
        useState(false);

    const login = useCallback((token) => {
        setAccessToken(token);
        setAccessTokenState(token);
    }, []);

    const logout = useCallback(async () => {
        try {
            await logoutApi();
        } finally {
            clearAccessToken();
            setAccessTokenState(null);
        }
    }, []);

    useEffect(() => {
        let cancelled = false;

        const initializeAuth = async () => {
            try {
                const token =
                    await requestInitialAccessToken();

                if (!cancelled) {
                    setAccessToken(token);
                    setAccessTokenState(token);
                }
            } catch (error) {
                /*
                 * 최초 방문처럼 Refresh Token 쿠키가 없는 경우
                 * 401은 정상적인 비로그인 상태로 처리합니다.
                 */
                if (!cancelled) {
                    clearAccessToken();
                    setAccessTokenState(null);
                }
            } finally {
                if (!cancelled) {
                    setIsAuthInitialized(true);
                }
            }
        };

        initializeAuth();

        return () => {
            cancelled = true;
        };
    }, []);

    const value = useMemo(
        () => ({
            accessToken,
            isLoggedIn: Boolean(accessToken),
            isAuthInitialized,
            login,
            logout,
        }),
        [
            accessToken,
            isAuthInitialized,
            login,
            logout,
        ],
    );

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}