import { useState } from "react";
import { login } from "../api/authApi.js";
import { setAccessToken } from "../store/authTokenStore.js";
import "./LoginPage.css";

function LoginPage({ onLoginSuccess, onMoveSignup }) {
    const [form, setForm] = useState({
        email: "",
        password: "",
    });

    const [message, setMessage] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (event) => {
        const { name, value } = event.target;

        setForm((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        try {
            setMessage("");
            setIsSubmitting(true);

            const result = await login(form);

            const accessToken =
                result.data?.accessToken;

            if (!accessToken) {
                throw new Error(
                    "로그인 응답에 Access Token이 없습니다."
                );
            }

            onLoginSuccess(accessToken);
        } catch (error) {
            console.error("로그인 실패:", error);

            const errorMessage =
                error.response?.data?.message ??
                "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.";

            setMessage(errorMessage);
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleGoogleLogin = () => {
        window.location.href =
            "http://localhost:8080/oauth2/authorization/google";
    };

    const handleKakaoLogin = () => {
        window.location.href =
            "http://localhost:8080/oauth2/authorization/kakao";
    };

    return (
        <main className="login-page">
            <div className="login-container">
                <section className="login-visual-section">
                    <div className="login-visual-content">
                        <div className="visual-badge">
                            AI 기반 자격증 학습 플랫폼
                        </div>

                        <h1 className="login-brand">
                            <span className="brand-highlight-text">
                                공부를 더 똑똑하게
                            </span>
                            ,{" "}
                            <span className="brand-logo-text">
                                Aha
                            </span>
                        </h1>

                        <p className="login-subtitle">
                            개념 학습부터 문제 풀이, 오답 복습까지
                            <br />
                            하나의 흐름으로 이어지는 학습을 경험해보세요.
                        </p>

                        <div
                            className="study-illustration"
                            aria-hidden="true"
                        >
                            <div className="plant">
                                <div className="leaf leaf-left" />
                                <div className="leaf leaf-right" />
                                <div className="stem" />
                                <div className="pot" />
                            </div>

                            <div className="sparkle sparkle-one">
                                ✦
                            </div>
                            <div className="sparkle sparkle-two">
                                ✦
                            </div>

                            <div className="laptop">
                                <div className="laptop-screen">
                                    <div className="check-row">
                                        <span>✓</span>
                                        <i />
                                    </div>
                                    <div className="check-row">
                                        <span>✓</span>
                                        <i />
                                    </div>
                                    <div className="check-row">
                                        <span>✓</span>
                                        <i />
                                    </div>
                                </div>
                                <div className="laptop-base" />
                            </div>

                            <div className="book">
                                <div className="book-left">
                                    <i />
                                    <i />
                                    <i />
                                </div>
                                <div className="book-right">
                                    <i />
                                    <i />
                                    <i />
                                </div>
                            </div>

                            <div className="pen" />
                        </div>
                    </div>
                </section>

                <section className="login-form-section">
                    <div className="login-form-card">
                        <div className="login-form-header">
                            <h2>로그인</h2>
                            <p>
                                계정으로 로그인하고 학습을 이어가세요.
                            </p>
                        </div>

                        <form
                            className="login-form"
                            onSubmit={handleSubmit}
                        >
                            <label className="login-input-wrap">
                                <span
                                    className="input-icon"
                                    aria-hidden="true"
                                >
                                    <svg
                                        width="18"
                                        height="18"
                                        viewBox="0 0 24 24"
                                        fill="none"
                                    >
                                        <path
                                            d="M4.75 6.75H19.25V17.25H4.75V6.75Z"
                                            stroke="currentColor"
                                            strokeWidth="1.7"
                                            strokeLinejoin="round"
                                        />
                                        <path
                                            d="M5.25 7.25L12 12.25L18.75 7.25"
                                            stroke="currentColor"
                                            strokeWidth="1.7"
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                        />
                                    </svg>
                                </span>

                                <input
                                    name="email"
                                    type="email"
                                    value={form.email}
                                    onChange={handleChange}
                                    placeholder="이메일"
                                    autoComplete="email"
                                    required
                                />
                            </label>

                            <label className="login-input-wrap">
                                <span
                                    className="input-icon"
                                    aria-hidden="true"
                                >
                                    <svg
                                        width="18"
                                        height="18"
                                        viewBox="0 0 24 24"
                                        fill="none"
                                    >
                                        <path
                                            d="M7.75 10.25V8.4C7.75 5.95 9.55 4.25 12 4.25C14.45 4.25 16.25 5.95 16.25 8.4V10.25"
                                            stroke="currentColor"
                                            strokeWidth="1.7"
                                            strokeLinecap="round"
                                        />
                                        <path
                                            d="M6.75 10.25H17.25C18.08 10.25 18.75 10.92 18.75 11.75V18.25C18.75 19.08 18.08 19.75 17.25 19.75H6.75C5.92 19.75 5.25 19.08 5.25 18.25V11.75C5.25 10.92 5.92 10.25 6.75 10.25Z"
                                            stroke="currentColor"
                                            strokeWidth="1.7"
                                            strokeLinejoin="round"
                                        />
                                        <path
                                            d="M12 14.25V15.75"
                                            stroke="currentColor"
                                            strokeWidth="1.7"
                                            strokeLinecap="round"
                                        />
                                    </svg>
                                </span>

                                <input
                                    name="password"
                                    type={
                                        showPassword
                                            ? "text"
                                            : "password"
                                    }
                                    value={form.password}
                                    onChange={handleChange}
                                    placeholder="비밀번호"
                                    autoComplete="current-password"
                                    required
                                />

                                <button
                                    className="password-toggle"
                                    type="button"
                                    onClick={() =>
                                        setShowPassword(
                                            (prev) => !prev
                                        )
                                    }
                                >
                                    {showPassword ? "숨김" : "보기"}
                                </button>
                            </label>

                            <div className="login-options">
                                <label className="login-remember">
                                    <input type="checkbox" />
                                    <span>로그인 상태 유지</span>
                                </label>

                                <button
                                    type="button"
                                    className="find-password"
                                >
                                    비밀번호 찾기
                                </button>
                            </div>

                            {message && (
                                <p className="login-message">
                                    {message}
                                </p>
                            )}

                            <button
                                className="login-submit-button"
                                type="submit"
                                disabled={isSubmitting}
                            >
                                {isSubmitting
                                    ? "로그인 중..."
                                    : "로그인"}
                            </button>
                        </form>

                        <div className="login-divider">
                            <span />
                            <p>또는</p>
                            <span />
                        </div>

                        <div className="social-login-group">
                            <button
                                className="google-login-button"
                                type="button"
                                onClick={handleGoogleLogin}
                            >
                                <span
                                    className="google-icon"
                                    aria-hidden="true"
                                >
                                    <svg
                                        width="22"
                                        height="22"
                                        viewBox="0 0 24 24"
                                    >
                                        <path
                                            d="M21.6 12.23C21.6 11.5 21.53 10.8 21.4 10.12H12.2V13.9H17.47C17.24 15.12 16.55 16.16 15.52 16.85V19.3H18.68C20.53 17.6 21.6 15.08 21.6 12.23Z"
                                            fill="#4285F4"
                                        />
                                        <path
                                            d="M12.2 21.8C14.84 21.8 17.06 20.93 18.68 19.3L15.52 16.85C14.65 17.43 13.54 17.78 12.2 17.78C9.65 17.78 7.49 16.06 6.72 13.75H3.45V16.28C5.06 19.48 8.37 21.8 12.2 21.8Z"
                                            fill="#34A853"
                                        />
                                        <path
                                            d="M6.72 13.75C6.52 13.17 6.41 12.55 6.41 11.9C6.41 11.25 6.52 10.63 6.72 10.05V7.52H3.45C2.78 8.85 2.4 10.34 2.4 11.9C2.4 13.46 2.78 14.95 3.45 16.28L6.72 13.75Z"
                                            fill="#FBBC05"
                                        />
                                        <path
                                            d="M12.2 6.02C13.64 6.02 14.93 6.52 15.95 7.49L18.75 4.69C17.05 3.11 14.84 2.15 12.2 2.15C8.37 2.15 5.06 4.32 3.45 7.52L6.72 10.05C7.49 7.74 9.65 6.02 12.2 6.02Z"
                                            fill="#EA4335"
                                        />
                                    </svg>
                                </span>
                                Google로 계속하기
                            </button>

                            <button
                                className="kakao-login-button"
                                type="button"
                                onClick={handleKakaoLogin}
                            >
                                <span
                                    className="kakao-icon"
                                    aria-hidden="true"
                                >
                                    <svg
                                        width="22"
                                        height="22"
                                        viewBox="0 0 24 24"
                                        fill="none"
                                    >
                                        <path
                                            d="M12 4C7.03 4 3 7.18 3 11.1C3 13.62 4.66 15.84 7.16 17.1L6.35 20.04C6.27 20.34 6.61 20.58 6.86 20.4L10.34 18.04C10.88 18.14 11.43 18.2 12 18.2C16.97 18.2 21 15.02 21 11.1C21 7.18 16.97 4 12 4Z"
                                            fill="#191919"
                                        />
                                    </svg>
                                </span>
                                카카오로 계속하기
                            </button>
                        </div>

                        <div className="login-links">
                            <span>아직 계정이 없으신가요?</span>
                            <button
                                type="button"
                                onClick={onMoveSignup}
                            >
                                회원가입
                            </button>
                        </div>
                    </div>
                </section>
            </div>

            <footer className="login-footer">
                <p>© 2024 Aha. All rights reserved.</p>

                <nav>
                    <button type="button">이용약관</button>
                    <button type="button">
                        개인정보 처리방침
                    </button>
                    <button type="button">고객센터</button>
                </nav>
            </footer>
        </main>
    );
}

export default LoginPage;