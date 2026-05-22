import { useState } from "react";
import { signup } from "../api/authApi";

function SignupPage({ onMoveLogin }) {
    const [form, setForm] = useState({
        email: "",
        password: "",
        name: "",
        nickname: "",
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

            await signup(form);

            setMessage("회원가입이 완료되었습니다. 로그인해주세요.");
        } catch (error) {
            console.error(error);
            setMessage("회원가입에 실패했습니다. 입력값이나 서버 응답을 확인해주세요.");
        }
    };

    return (
        <main className="auth-page">
            <section className="auth-card">
                <p className="eyebrow">Aha Learning Platform</p>
                <h1>회원가입</h1>
                <p className="auth-description">
                    SQLD 개념학습과 AI 도우미를 사용하기 위해 계정을 생성합니다.
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

                    <label>
                        이름
                        <input
                            name="name"
                            value={form.name}
                            onChange={handleChange}
                            placeholder="홍길동"
                            required
                        />
                    </label>

                    <label>
                        닉네임
                        <input
                            name="nickname"
                            value={form.nickname}
                            onChange={handleChange}
                            placeholder="길동"
                            required
                        />
                    </label>

                    <button type="submit">회원가입</button>
                </form>

                {message && <p className="auth-message">{message}</p>}

                <button className="link-button" type="button" onClick={onMoveLogin}>
                    이미 계정이 있나요? 로그인하기
                </button>
            </section>
        </main>
    );
}

export default SignupPage;