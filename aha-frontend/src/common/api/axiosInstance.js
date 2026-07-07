import axios from "axios";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ||
    "http://localhost:8080";

export const AUTH_EXPIRED_EVENT = "auth-expired";

const axiosInstance = axios.create({
    baseURL: API_BASE_URL,
    timeout: 30000,
});

let refreshPromise = null;

const clearAuth = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
};

const notifyAuthExpired = () => {
    window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
};

const reissueToken = async () => {
    const refreshToken = localStorage.getItem("refreshToken");

    if (!refreshToken) {
        throw new Error("refreshToken이 없습니다.");
    }

    const response = await axios.post(
        `${API_BASE_URL}/api/v1/auth/reissue`,
        {
            refreshToken,
        }
    );

    const responseData = response.data?.data || response.data;

    const newAccessToken = responseData?.accessToken;
    const newRefreshToken = responseData?.refreshToken;

    if (!newAccessToken) {
        throw new Error("새 accessToken이 없습니다.");
    }

    localStorage.setItem("accessToken", newAccessToken);

    if (newRefreshToken) {
        localStorage.setItem("refreshToken", newRefreshToken);
    }

    return newAccessToken;
};

axiosInstance.interceptors.request.use(
    (config) => {
        const accessToken = localStorage.getItem("accessToken");

        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (
            error.response?.status !== 401 ||
            originalRequest?._retry
        ) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        try {
            if (!refreshPromise) {
                refreshPromise = reissueToken().finally(() => {
                    refreshPromise = null;
                });
            }

            const newAccessToken = await refreshPromise;

            originalRequest.headers = originalRequest.headers || {};
            originalRequest.headers.Authorization =
                `Bearer ${newAccessToken}`;

            return axiosInstance(originalRequest);
        } catch (refreshError) {
            clearAuth();
            notifyAuthExpired();

            return Promise.reject(refreshError);
        }
    }
);

export default axiosInstance;