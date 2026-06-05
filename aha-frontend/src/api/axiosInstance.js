import axios from "axios";

const axiosInstance = axios.create({
    baseURL: "http://localhost:8080",
    headers: {
        "Content-Type": "application/json",
    },
});

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

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                const refreshToken = localStorage.getItem("refreshToken");

                if (!refreshToken) {
                    throw new Error("refreshToken 없음");
                }

                const response = await axios.post(
                    "http://localhost:8080/api/v1/auth/reissue",
                    {
                        refreshToken,
                    }
                );

                const newAccessToken = response.data.data.accessToken;
                const newRefreshToken = response.data.data.refreshToken;

                localStorage.setItem("accessToken", newAccessToken);
                localStorage.setItem("refreshToken", newRefreshToken);

                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

                return axiosInstance(originalRequest);
            } catch (reissueError) {
                localStorage.removeItem("accessToken");
                localStorage.removeItem("refreshToken");

                window.location.href = "/login";

                return Promise.reject(reissueError);
            }
        }

        return Promise.reject(error);
    }
);

export default axiosInstance;