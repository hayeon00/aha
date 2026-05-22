import axios from "axios";

const axiosInstance = axios.create({
    baseURL: "http://localhost:8080",
});

axiosInstance.interceptors.request.use((config) => {
    try {
        const token = window.localStorage.getItem("accessToken");

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
    } catch (error) {
        console.warn("localStorage 접근이 차단되었습니다.", error);
    }

    return config;
});

export default axiosInstance;