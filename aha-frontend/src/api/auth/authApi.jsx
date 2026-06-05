import axiosInstance from "../axiosInstance.js";

export const signup = async (signupData) => {
    const requestData = {
        email: signupData.email,
        password: signupData.password,
        name: signupData.name,
        nickname: signupData.nickname,
    };

    const response = await axiosInstance.post("/api/v1/auth/signup", requestData);
    return response.data;
};

export const login = async (loginData) => {
    const response = await axiosInstance.post("/api/v1/auth/login", loginData);
    return response.data;
};

export const logout = async () => {
    const response = await axiosInstance.post("/api/v1/auth/logout");
    return response.data;
};

export const reissue = async (refreshToken) => {
    const response = await axiosInstance.post("/api/v1/auth/reissue", {
        refreshToken,
    });

    return response.data;
};