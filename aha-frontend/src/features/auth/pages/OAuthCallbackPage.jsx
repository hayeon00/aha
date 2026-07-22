import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { exchangeOAuthCode } from "../api/authApi";

const OAUTH_ERROR_MESSAGES = {
    ACCOUNT_NOT_ACTIVE:
        "정지되었거나 사용할 수 없는 계정입니다.",

    OAUTH_ACCESS_DENIED:
        "소셜 로그인 동의가 취소되었습니다.",

    OAUTH_SESSION_EXPIRED:
        "로그인 요청이 만료되었습니다. 다시 시도해주세요.",

    OAUTH_INVALID_STATE:
        "유효하지 않은 로그인 요청입니다. 다시 시도해주세요.",

    SOCIAL_EMAIL_REQUIRED:
        "카카오 또는 Google 계정의 이메일 제공에 동의해 주세요.",

    SOCIAL_ACCOUNT_LINK_REQUIRED:
        "동일한 이메일의 기존 계정이 있습니다. 기존 로그인 방식으로 로그인해 주세요.",

    OAUTH_LOGIN_FAILED:
        "소셜 로그인에 실패했습니다. 다시 시도해주세요.",
};

/*
 * 동일 OAuth code에 대한 교환 요청을 공유합니다.
 * React StrictMode에서 Effect가 두 번 실행돼도
 * 실제 exchange 요청은 한 번만 전송됩니다.
 */
const oauthExchangeRequests = new Map();

const exchangeOAuthCodeOnce = (code) => {
    const existingRequest =
        oauthExchangeRequests.get(code);

    if (existingRequest) {
        return existingRequest;
    }

    const exchangeRequest =
        exchangeOAuthCode(code).finally(() => {
            window.setTimeout(() => {
                oauthExchangeRequests.delete(code);
            }, 60_000);
        });

    oauthExchangeRequests.set(
        code,
        exchangeRequest,
    );

    return exchangeRequest;
};

const extractAccessToken = (response) => {
    return (
        response?.data?.data?.accessToken
        ?? response?.data?.accessToken
        ?? response?.accessToken
        ?? null
    );
};

export default function OAuthCallbackPage({
                                              onLoginSuccess,
                                          }) {
    const navigate = useNavigate();

    useEffect(() => {
        let cancelled = false;

        const handleOAuthCallback = async () => {
            const params =
                new URLSearchParams(
                    window.location.search,
                );

            const errorCode =
                params.get("error");

            const code =
                params.get("code");

            if (errorCode) {
                if (cancelled) {
                    return;
                }

                const message =
                    OAUTH_ERROR_MESSAGES[errorCode]
                    ?? OAUTH_ERROR_MESSAGES
                        .OAUTH_LOGIN_FAILED;

                alert(message);

                navigate("/login", {
                    replace: true,
                });

                return;
            }

            if (!code) {
                if (cancelled) {
                    return;
                }

                alert(
                    "유효하지 않은 소셜 로그인 요청입니다.",
                );

                navigate("/login", {
                    replace: true,
                });

                return;
            }

            try {
                const response =
                    await exchangeOAuthCodeOnce(code);

                if (cancelled) {
                    return;
                }

                const accessToken =
                    extractAccessToken(response);

                if (!accessToken) {
                    throw new Error(
                        "OAuth 응답에 Access Token이 없습니다.",
                    );
                }

                onLoginSuccess(accessToken);
            } catch (error) {
                if (cancelled) {
                    return;
                }

                console.error(
                    "OAuth code 교환 실패:",
                    error,
                );

                alert(
                    "소셜 로그인 처리에 실패했습니다.",
                );

                navigate("/login", {
                    replace: true,
                });
            }
        };

        handleOAuthCallback();

        return () => {
            cancelled = true;
        };
    }, [navigate, onLoginSuccess]);

    return (
        <main
            style={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
            }}
        >
            <p>소셜 로그인 처리 중입니다.</p>
        </main>
    );
}