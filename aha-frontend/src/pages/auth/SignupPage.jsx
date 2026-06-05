import { useState } from "react";
import { signup } from "../../api/auth/authApi.jsx";
import "./SignupPage.css";

function SignupPage({ onMoveLogin }) {
    const [step, setStep] = useState(1);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isSignupComplete, setIsSignupComplete] = useState(false);
    const [message, setMessage] = useState("");

    const [form, setForm] = useState({
        email: "",
        nickname: "",
        password: "",
        passwordConfirm: "",
        agreeTerms: false,
        agreePrivacy: false,
    });

    const handleChange = (event) => {
        const { name, value, type, checked } = event.target;

        setForm((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const handleNextStep = () => {
        setMessage("");

        if (step === 1) {
            if (
                !form.email ||
                !form.nickname ||
                !form.password ||
                !form.passwordConfirm
            ) {
                setMessage("계정 정보를 모두 입력해주세요.");
                return;
            }

            if (form.password.length < 8) {
                setMessage("비밀번호는 최소 8자 이상이어야 합니다.");
                return;
            }

            if (form.password !== form.passwordConfirm) {
                setMessage("비밀번호가 서로 일치하지 않습니다.");
                return;
            }
        }

        setStep((prev) => prev + 1);
    };

    const handlePrevStep = () => {
        setMessage("");
        setStep((prev) => prev - 1);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (!form.agreeTerms || !form.agreePrivacy) {
            setMessage("필수 약관에 동의해주세요.");
            return;
        }

        try {
            setIsSubmitting(true);
            setMessage("");

            const signupData = {
                email: form.email,
                password: form.password,
                nickname: form.nickname,
            };

            await signup(signupData);

            setIsSignupComplete(true);
            setStep(3);
        } catch (error) {
            console.error(error);
            console.log("회원가입 실패 응답:", error.response?.data);

            setMessage(
                error.response?.data?.message ||
                "회원가입에 실패했습니다. 입력 정보를 다시 확인해주세요."
            );
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <main className="signup-page">
            <section className="signup-card">
                <header className="signup-header">
                    <button
                        className="signup-wordmark"
                        type="button"
                        onClick={onMoveLogin}
                    >
                        Aha
                    </button>

                    <h1>회원가입</h1>
                    <p>계정을 만들고 나만의 학습을 시작하세요.</p>
                </header>

                <div className="signup-stepper">
                    <div className={step >= 1 ? "active" : ""}>
                        <span>1</span>
                        <p>회원 정보</p>
                    </div>

                    <i />

                    <div className={step >= 2 ? "active" : ""}>
                        <span>2</span>
                        <p>약관 동의</p>
                    </div>

                    <i />

                    <div className={step >= 3 ? "active" : ""}>
                        <span>3</span>
                        <p>완료</p>
                    </div>
                </div>

                {isSignupComplete ? (
                    <div className="signup-complete-content">
                        <div className="complete-visual">
                            <span className="complete-sparkle complete-sparkle-left">
                                ✦
                            </span>

                            <div className="complete-check">✓</div>

                            <span className="complete-sparkle complete-sparkle-right">
                                ✦
                            </span>
                        </div>

                        <h2>회원가입이 완료됐어요!</h2>

                        <p>
                            이제 Aha에서 나에게 맞는 학습을 시작할 수 있어요.
                        </p>

                        <button
                            className="complete-start-button"
                            type="button"
                            onClick={onMoveLogin}
                        >
                            로그인하러 가기
                        </button>

                        <div className="signup-login-link complete-login-link">
                            <span>이미 계정이 있으신가요?</span>
                            <button type="button" onClick={onMoveLogin}>
                                로그인
                            </button>
                        </div>
                    </div>
                ) : (
                    <>
                        <form className="signup-form" onSubmit={handleSubmit}>
                            {step === 1 && (
                                <div className="signup-content account-content">
                                    <label className="signup-input-wrap">
                                        <span
                                            className="signup-input-icon"
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
                                        />
                                    </label>

                                    <label className="signup-input-wrap">
                                        <span
                                            className="signup-input-icon"
                                            aria-hidden="true"
                                        >
                                            <svg
                                                width="18"
                                                height="18"
                                                viewBox="0 0 24 24"
                                                fill="none"
                                            >
                                                <path
                                                    d="M12 12.25C14.07 12.25 15.75 10.57 15.75 8.5C15.75 6.43 14.07 4.75 12 4.75C9.93 4.75 8.25 6.43 8.25 8.5C8.25 10.57 9.93 12.25 12 12.25Z"
                                                    stroke="currentColor"
                                                    strokeWidth="1.7"
                                                />
                                                <path
                                                    d="M5.25 19.25C6.2 16.95 8.55 15.5 12 15.5C15.45 15.5 17.8 16.95 18.75 19.25"
                                                    stroke="currentColor"
                                                    strokeWidth="1.7"
                                                    strokeLinecap="round"
                                                />
                                            </svg>
                                        </span>

                                        <input
                                            name="nickname"
                                            type="text"
                                            value={form.nickname}
                                            onChange={handleChange}
                                            placeholder="닉네임"
                                            autoComplete="nickname"
                                        />
                                    </label>

                                    <label className="signup-input-wrap">
                                        <span
                                            className="signup-input-icon"
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
                                            type="password"
                                            value={form.password}
                                            onChange={handleChange}
                                            placeholder="비밀번호"
                                            autoComplete="new-password"
                                        />
                                    </label>

                                    <label className="signup-input-wrap">
                                        <span
                                            className="signup-input-icon"
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
                                                    d="M10.1 15.15L11.35 16.4L14 13.75"
                                                    stroke="currentColor"
                                                    strokeWidth="1.7"
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                />
                                            </svg>
                                        </span>

                                        <input
                                            name="passwordConfirm"
                                            type="password"
                                            value={form.passwordConfirm}
                                            onChange={handleChange}
                                            placeholder="비밀번호 확인"
                                            autoComplete="new-password"
                                        />
                                    </label>
                                </div>
                            )}

                            {step === 2 && (
                                <div className="signup-content terms-content clean-terms-content">
                                    <div className="terms-intro">
                                        <div className="terms-icon">🎯</div>

                                        <h2>가입 준비가 거의 끝났어요</h2>

                                        <p>
                                            서비스 이용을 위해 필수 약관에
                                            동의해주세요.
                                        </p>
                                    </div>

                                    <div className="terms-agree-box">
                                        <label className="terms-agree-row">
                                            <input
                                                name="agreeTerms"
                                                type="checkbox"
                                                checked={form.agreeTerms}
                                                onChange={handleChange}
                                            />

                                            <span>
                                                이용약관에 동의합니다.
                                                <b>필수</b>
                                            </span>
                                        </label>

                                        <label className="terms-agree-row">
                                            <input
                                                name="agreePrivacy"
                                                type="checkbox"
                                                checked={form.agreePrivacy}
                                                onChange={handleChange}
                                            />

                                            <span>
                                                개인정보 처리방침에 동의합니다.
                                                <b>필수</b>
                                            </span>
                                        </label>
                                    </div>
                                </div>
                            )}

                            {message && (
                                <p className="signup-message">{message}</p>
                            )}

                            <div className="signup-actions">
                                {step > 1 && (
                                    <button
                                        className="signup-prev-button"
                                        type="button"
                                        onClick={handlePrevStep}
                                    >
                                        이전
                                    </button>
                                )}

                                {step < 2 ? (
                                    <button
                                        className="signup-next-button"
                                        type="button"
                                        onClick={handleNextStep}
                                    >
                                        다음
                                    </button>
                                ) : (
                                    <button
                                        className="signup-submit-button"
                                        type="submit"
                                        disabled={isSubmitting}
                                    >
                                        {isSubmitting
                                            ? "가입 중..."
                                            : "회원가입 완료"}
                                    </button>
                                )}
                            </div>
                        </form>

                        <div className="signup-login-link">
                            <span>이미 계정이 있으신가요?</span>
                            <button type="button" onClick={onMoveLogin}>
                                로그인
                            </button>
                        </div>
                    </>
                )}
            </section>

            <footer className="signup-footer">
                <p>© 2024 Aha. All rights reserved.</p>

                <nav>
                    <button type="button">이용약관</button>
                    <button type="button">개인정보 처리방침</button>
                    <button type="button">고객센터</button>
                </nav>
            </footer>
        </main>
    );
}

export default SignupPage;