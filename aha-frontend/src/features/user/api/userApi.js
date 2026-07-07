import axiosInstance from "../../../common/api/axiosInstance.js";

export const getMyInfo = async () => {
    const response = await axiosInstance.get("/api/v1/users/me");

    return response.data;
};

export const updateProfile = async (profileData) => {
    const response = await axiosInstance.patch(
        "/api/v1/users/me/profile",
        profileData
    );

    return response.data;
};

export const updateProfileImage = async (profileImage) => {
    const formData = new FormData();

    formData.append("profileImage", profileImage);

    const response = await axiosInstance.patch(
        "/api/v1/users/me/profile-image",
        formData
    );

    return response.data;
};