import axios from "axios";

const API_BASE_URL = "http://localhost:8080";

export const getMyInfo = () => {
    const accessToken = localStorage.getItem("accessToken");

    return axios.get(`${API_BASE_URL}/api/users/me`, {
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });
};