import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMyInfo } from "../api/userApi";
import "./MainPage.css";


function MainPage({ onLogout }) {
    const navigate = useNavigate();

    const [user, setUser] = useState({
        nickname: "사용자",
        email: "",
    });

    const [isProfileOpen, setIsProfileOpen] = useState(false);

    useEffect(() => {
        const fetchMyInfo = async () => {
            try {
                const result = await getMyInfo();

                setUser({
                    nickname: result.data.data.nickname,
                    email: result.data.data.email,
                });
            } catch (error) {
                console.error("내 정보 조회 실패:", error);

                if (error.response?.status === 401) {
                    localStorage.removeItem("accessToken");
                    localStorage.removeItem("refreshToken");
                    navigate("/login", { replace: true });
                }
            }
        };

        fetchMyInfo();
    }, [navigate]);

    return (
        <div className="main-page">
            <main className="main-content">
                    <h1>홈화면</h1>
            </main>
        </div>
    );
}

export default MainPage;