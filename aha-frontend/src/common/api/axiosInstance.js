import axios from "axios";
import {
    clearAccessToken,
    getAccessToken,
    setAccessToken,
} from "../../features/auth/store/authTokenStore";

const axiosInstance = axios.create({
    baseURL: "http://localhost:8080",
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

let isRefreshing = false;
let refreshSubscribers = [];

const subscribeTokenRefresh = (callback) => {
    refreshSubscribers.push(callback);
};

const onTokenRefreshed = (newAccessToken) => {
    refreshSubscribers.forEach((callback) => {
        callback(newAccessToken);
    });

    refreshSubscribers = [];
};

axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (
            error.response?.status !== 401 ||
            originalRequest._retry ||
            originalRequest.url?.includes("/api/v1/auth/login") ||
            originalRequest.url?.includes("/api/v1/auth/reissue") ||
            originalRequest.url?.includes("/api/v1/auth/oauth/exchange")
        ) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        if (isRefreshing) {
            return new Promise((resolve) => {
                subscribeTokenRefresh((newAccessToken) => {
                    originalRequest.headers.Authorization =
                        `Bearer ${newAccessToken}`;

                    resolve(axiosInstance(originalRequest));
                });
            });
        }

        isRefreshing = true;

        try {
            const response = await axios.post(
                "http://localhost:8080/api/v1/auth/reissue",
                null,
                {
                    withCredentials: true,
                },
            );

            const newAccessToken =
                response.data.data.accessToken;

            setAccessToken(newAccessToken);
            onTokenRefreshed(newAccessToken);

            originalRequest.headers.Authorization =
                `Bearer ${newAccessToken}`;

            return axiosInstance(originalRequest);
        } catch (refreshError) {
            clearAccessToken();
            refreshSubscribers = [];

            return Promise.reject(refreshError);
        } finally {
            isRefreshing = false;
        }
    },
);

export default axiosInstance;