import { useState } from "react";
import { login } from "../api/authApi";

function LoginPage({ onLoginSuccess, onMoveSignup }) {
    const [form, setForm] = useState({
        email: "",
        password: "",
    });

    const [message, setMessage] = useState("");

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

            const result = await login(form);

            const accessToken = result.data.accessToken;
            const refreshToken = result.data.refreshToken;

            localStorage.setItem("accessToken", accessToken);
            localStorage.setItem("refreshToken", refreshToken);

            onLoginSuccess();
        } catch (error) {
            console.error(error);
            setMessage("로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.");
        }
    };

    return (
        <main className="auth-page">
            <section className="auth-card">
                <p className="eyebrow">Aha Learning Platform</p>
                <h1>로그인</h1>
                <p className="auth-description">
                    로그인 후 개념학습 화면으로 이동합니다.
                </p>

                <form className="auth-form" onSubmit={handleSubmit}>
                    <label>
                        이메일
                        <input
                            name="email"
                            type="email"
                            value={form.email}
                            onChange={handleChange}
                            placeholder="test@test.com"
                            required
                        />
                    </label>

                    <label>
                        비밀번호
                        <input
                            name="password"
                            type="password"
                            value={form.password}
                            onChange={handleChange}
                            placeholder="비밀번호"
                            required
                        />
                    </label>

                    <button type="submit">로그인</button>
                </form>

                {message && <p className="auth-message error">{message}</p>}

                <button className="link-button" type="button" onClick={onMoveSignup}>
                    계정이 없나요? 회원가입하기
                </button>
            </section>
        </main>
    );
}

export default LoginPage;