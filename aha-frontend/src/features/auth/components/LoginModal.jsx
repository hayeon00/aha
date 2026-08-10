import { useEffect, useState } from "react";
import { login } from "../api/authApi.js";
import "./LoginModal.css";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/\/+$/, "");

export default function LoginModal({ open, onClose, onLoginSuccess, onMoveSignup }) {
    const [form, setForm] = useState({ email: "", password: "" });
    const [message, setMessage] = useState("");
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (!open) return undefined;
        const closeOnEscape = (event) => event.key === "Escape" && onClose();
        document.addEventListener("keydown", closeOnEscape);
        document.body.style.overflow = "hidden";
        return () => {
            document.removeEventListener("keydown", closeOnEscape);
            document.body.style.overflow = "";
        };
    }, [open, onClose]);

    if (!open) return null;

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            setSubmitting(true);
            setMessage("");
            const result = await login(form);
            const token = result.data?.accessToken;
            if (!token) throw new Error("로그인 응답을 확인할 수 없습니다.");
            onLoginSuccess(token);
            onClose();
        } catch (error) {
            setMessage(error.response?.data?.message || error.message || "이메일과 비밀번호를 확인해주세요.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="auth-modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
            <section className="auth-modal" role="dialog" aria-modal="true" aria-labelledby="login-title">
                <button className="auth-modal-close" type="button" onClick={onClose} aria-label="닫기">×</button>
                <div className="auth-modal-brand"><img src="/brand/aha-mark.png" alt="" /><span>Aha</span></div>
                <header><h2 id="login-title">다시 만나 반가워요</h2><p>로그인하고 나만의 자격증 학습을 이어가세요.</p></header>
                <form onSubmit={handleSubmit}>
                    <label>이메일<input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="name@example.com" autoFocus required /></label>
                    <label>비밀번호<input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder="비밀번호를 입력하세요" required /></label>
                    {message && <p className="auth-modal-error" role="alert">{message}</p>}
                    <button className="auth-modal-submit" disabled={submitting}>{submitting ? "로그인 중..." : "로그인"}</button>
                </form>
                <div className="auth-modal-divider"><span>또는</span></div>
                <div className="auth-socials">
                    <button type="button" onClick={() => window.location.assign(`${API_BASE_URL}/oauth2/authorization/google`)}><GoogleLogo />Google로 계속하기</button>
                    <button className="kakao" type="button" onClick={() => window.location.assign(`${API_BASE_URL}/oauth2/authorization/kakao`)}><KakaoLogo />카카오로 계속하기</button>
                </div>
                <p className="auth-modal-signup">아직 계정이 없으신가요? <button type="button" onClick={onMoveSignup}>회원가입</button></p>
            </section>
        </div>
    );
}

const GoogleLogo = () => (
    <svg className="social-logo" viewBox="0 0 24 24" aria-hidden="true">
        <path fill="#4285F4" d="M21.6 12.2c0-.7-.1-1.4-.2-2H12v3.9h5.4a4.6 4.6 0 0 1-2 3v2.6h3.3c1.9-1.8 2.9-4.4 2.9-7.5Z" />
        <path fill="#34A853" d="M12 22c2.7 0 5-.9 6.7-2.3l-3.3-2.6c-.9.6-2.1 1-3.4 1a5.9 5.9 0 0 1-5.5-4.1H3.1v2.7A10 10 0 0 0 12 22Z" />
        <path fill="#FBBC05" d="M6.5 14a6 6 0 0 1 0-3.9V7.4H3.1a10 10 0 0 0 0 9.3L6.5 14Z" />
        <path fill="#EA4335" d="M12 6a5.4 5.4 0 0 1 3.8 1.5l2.9-2.9A9.7 9.7 0 0 0 12 2a10 10 0 0 0-8.9 5.4l3.4 2.7A5.9 5.9 0 0 1 12 6Z" />
    </svg>
);

const KakaoLogo = () => (
    <svg className="social-logo kakao-logo" viewBox="0 0 24 24" aria-hidden="true">
        <path fill="currentColor" d="M12 3C6.5 3 2 6.5 2 10.8c0 2.8 1.9 5.2 4.7 6.6l-1.2 4.1c-.1.4.3.7.6.5l4.8-3.3 1.1.1c5.5 0 10-3.5 10-7.9S17.5 3 12 3Z" />
    </svg>
);
