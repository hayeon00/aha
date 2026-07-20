import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { exchangeOAuthCode } from "../api/authApi.js";
import "./OAuthCallbackPage.css";

function OAuthCallbackPage({ onLoginSuccess }) {
    const navigate = useNavigate();
    const hasProcessed = useRef(false);

    useEffect(() => {
        if (hasProcessed.current) {
            return;
        }

        hasProcessed.current = true;

        const exchangeCode = async () => {
            const params =
                new URLSearchParams(
                    window.location.search
                );

            const code =
                params.get("code");

            window.history.replaceState(
                null,
                "",
                window.location.pathname
            );

            if (!code) {
                alert(
                    "소셜 로그인 인증 코드를 찾을 수 없습니다."
                );

                navigate("/login", {
                    replace: true,
                });

                return;
            }

            try {
                const result = await exchangeOAuthCode(code);

                const accessToken =
                    result.data?.accessToken;

                if (!accessToken) {
                    throw new Error(
                        "토큰 교환 응답에 Access Token이 없습니다."
                    );
                }

                onLoginSuccess(accessToken);

            } catch (error) {
                console.error(
                    "OAuth 인증 코드 교환 실패:",
                    error
                );

                alert(
                    "소셜 로그인 처리에 실패했습니다. 다시 시도해주세요."
                );

                navigate("/login", {
                    replace: true,
                });
            }
        };

        exchangeCode();
    }, [navigate, onLoginSuccess]);

    return (
        <main
            className="oauth-callback-page"
            aria-live="polite"
        >
            <div
                className="oauth-callback-loading"
                aria-hidden="true"
            />

            <p>로그인 정보를 확인하고 있습니다.</p>
        </main>
    );
}

export default OAuthCallbackPage;