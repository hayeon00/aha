import axios from "axios";
import {
    clearAccessToken,
    getAccessToken,
    setAccessToken,
} from "../../features/auth/store/authTokenStore";

export const AUTH_EXPIRED_EVENT = "auth-expired";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL;

if (!API_BASE_URL) {
    throw new Error(
        "VITE_API_BASE_URL 환경변수가 설정되지 않았습니다.",
    );
}

const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true,
});

const publicAxios = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true,
});

axiosInstance.interceptors.request.use(
    (config) => {
        const accessToken = getAccessToken();

        if (accessToken) {
            config.headers.Authorization =
                `Bearer ${accessToken}`;
        }

        return config;
    },
    (error) => Promise.reject(error),
);

let refreshPromise = null;

const requestNewAccessToken = () => {
    if (!refreshPromise) {
        refreshPromise = publicAxios
            .post("/api/v1/auth/reissue")
            .then((response) => {
                const newAccessToken =
                    response.data?.data?.accessToken;

                if (!newAccessToken) {
                    throw new Error(
                        "재발급 응답에 Access Token이 없습니다.",
                    );
                }

                setAccessToken(newAccessToken);

                return newAccessToken;
            })
            .catch((error) => {
                clearAccessToken();

                window.dispatchEvent(
                    new CustomEvent(AUTH_EXPIRED_EVENT),
                );

                throw error;
            })
            .finally(() => {
                refreshPromise = null;
            });
    }

    return refreshPromise;
};

const isRefreshExcludedRequest = (url = "") => {
    return (
        url.includes("/api/v1/auth/login") ||
        url.includes("/api/v1/auth/signup") ||
        url.includes("/api/v1/auth/reissue") ||
        url.includes("/api/v1/auth/logout") ||
        url.includes("/api/v1/auth/oauth/exchange")
    );
};

axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (
            !originalRequest ||
            error.response?.status !== 401 ||
            originalRequest._retry ||
            isRefreshExcludedRequest(originalRequest.url)
        ) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        try {
            const newAccessToken =
                await requestNewAccessToken();

            originalRequest.headers =
                originalRequest.headers ?? {};

            originalRequest.headers.Authorization =
                `Bearer ${newAccessToken}`;

            return axiosInstance(originalRequest);
        } catch (refreshError) {
            return Promise.reject(refreshError);
        }
    },
);

export default axiosInstance;