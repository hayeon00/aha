import axiosInstance from "../../../common/api/axiosInstance";

export const signup = async (request) => {
    const response = await axiosInstance.post(
        "/api/v1/auth/signup",
        request,
    );

    return response.data;
};

export const login = async (request) => {
    const response = await axiosInstance.post(
        "/api/v1/auth/login",
        request,
        {
            withCredentials: true,
        },
    );

    return response.data;
};

export const reissue = async () => {
    const response = await axiosInstance.post(
        "/api/v1/auth/reissue",
        null,
        {
            withCredentials: true,
        },
    );

    if (response.status === 204) {
        return null;
    }

    return response.data;
};


export const exchangeOAuthCode = async (code) => {
    const response = await axiosInstance.post(
        "/api/v1/auth/oauth/exchange",
        {
            code,
        },
        {
            withCredentials: true,
        },
    );

    return response.data;
};

export const logout = async () => {
    const response = await axiosInstance.post(
        "/api/v1/auth/logout",
        null,
        {
            withCredentials: true,
        },
    );

    return response.data;
};
