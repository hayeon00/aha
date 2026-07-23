import {
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";

import { AuthContext } from "./AuthContext.js";

import {
    clearAccessToken,
    setAccessToken,
} from "../store/authTokenStore.js";

import {
    logout as logoutApi,
    reissue,
} from "../api/authApi.js";

import {
    AUTH_EXPIRED_EVENT,
} from "../../../common/api/axiosInstance.js";

let authInitializationPromise = null;

const requestInitialAccessToken = () => {
    if (!authInitializationPromise) {
        authInitializationPromise = reissue()
            .then((response) => {
                if (!response) {
                    return null;
                }

                const accessToken =
                    response?.data?.accessToken
                    ?? response?.data?.data?.accessToken
                    ?? response?.accessToken
                    ?? null;

                return accessToken;
            })
            .finally(() => {
                authInitializationPromise = null;
            });
    }

    return authInitializationPromise;
};

export default function AuthProvider({
                                         children,
                                     }) {
    const [
        accessToken,
        setAccessTokenState,
    ] = useState(null);

    const [
        isAuthInitialized,
        setIsAuthInitialized,
    ] = useState(false);

    const login = useCallback((token) => {
        setAccessToken(token);
        setAccessTokenState(token);
    }, []);

    const clearAuth = useCallback(() => {
        clearAccessToken();
        setAccessTokenState(null);
    }, []);

    const logout = useCallback(async () => {
        try {
            await logoutApi();
        } finally {
            clearAuth();
        }
    }, [clearAuth]);

    useEffect(() => {
        let cancelled = false;

        const initializeAuth = async () => {
            const isOAuthCallback =
                window.location.pathname
                === "/oauth/callback";

            if (isOAuthCallback) {
                if (!cancelled) {
                    setIsAuthInitialized(true);
                }

                return;
            }

            try {
                const token =
                    await requestInitialAccessToken();

                if (cancelled) {
                    return;
                }

                if (token) {
                    setAccessToken(token);
                    setAccessTokenState(token);
                } else {
                    clearAuth();
                }
            } catch {
                if (cancelled) {
                    return;
                }

                /*
                 * Refresh Token이 없는 경우는
                 * 정상적인 비로그인 상태로 처리합니다.
                 */
                clearAuth();
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
    }, [clearAuth]);

    useEffect(() => {
        const handleAuthExpired = () => {
            clearAuth();
        };

        window.addEventListener(
            AUTH_EXPIRED_EVENT,
            handleAuthExpired,
        );

        return () => {
            window.removeEventListener(
                AUTH_EXPIRED_EVENT,
                handleAuthExpired,
            );
        };
    }, [clearAuth]);

    const value = useMemo(
        () => ({
            accessToken,
            isLoggedIn: Boolean(accessToken),
            isAuthInitialized,
            login,
            logout,
            clearAuth,
        }),
        [
            accessToken,
            isAuthInitialized,
            login,
            logout,
            clearAuth,
        ],
    );

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}
