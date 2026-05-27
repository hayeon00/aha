import axiosInstance from "./axiosInstance";

export const signup = async (signupData) => {
    const response = await axiosInstance.post("/api/v1/auth/signup", signupData);
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

export const getExams = async () => {
    const response = await axiosInstance.get("/api/v1/exams");
    return response.data;
};