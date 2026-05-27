import { useNavigate } from "react-router-dom";
import "./MainPage.css";

const examInfo = {
    examName: "SQLD",
    examFullName: "SQL 개발자",
    currentTopic: "데이터 모델의 이해",
};

function MainPage() {
    const navigate = useNavigate();

    const handleStartConcept = () => {
        navigate("/learning");
    };

    const handleStartProblem = () => {
        // 문제풀이 화면이 만들어지면 경로 변경
        // navigate("/problems");
        console.log("문제 풀이 시작");
    };

    return (
        <div className="main-page">
            <header className="main-header">
                <div className="logo">Aha!</div>

                <div className="header-right">
                    <nav className="main-nav">
                        <button className="active" type="button">
                            홈
                        </button>

                        <button
                            type="button"
                            onClick={() => navigate("/learning")}
                        >
                            개념 학습
                        </button>

                        <button type="button">
                            문제집
                        </button>

                        <button type="button">
                            오답노트
                        </button>
                    </nav>

                    <button className="icon-button" type="button">
                        ⌂
                    </button>

                    <button className="icon-button" type="button">
                        🔔
                    </button>
                </div>
            </header>

            <main className="main-content">
                <section className="hero-section">
                    <p className="hero-kicker">
                        {examInfo.examName} · {examInfo.examFullName} 학습 메인
                    </p>

                    <h1>
                        어떻게 학습을 시작할까요?

                    </h1>

                    <p className="hero-description">
                        개념을 먼저 정리하거나, 문제를 풀면서 필요한 개념을 확인할 수 있어요.
                    </p>
                    <h1>확정xxxxxxxxxxxxxxxx 수정 필요!!!!!!!!!!!!!</h1>
                </section>

                <section className="start-card-section">
                    <article className="start-card concept-card">
                        <div className="card-content">
                            <div className="card-icon orange">Ⅱ</div>

                            <span className="card-type">개념 중심 학습</span>

                            <h2>개념 학습부터 시작</h2>

                            <p>
                                {examInfo.examName} 핵심 개념을 목차별로 학습하고,
                                AI 도우미와 확인 문제로 이해도를 점검해요.
                            </p>

                            <div className="tag-list concept-tags">
                                <span>개념 설명</span>
                                <span>AI 도우미</span>
                                <span>확인 문제</span>
                            </div>

                            <div className="preview-box concept-preview">
                                <div className="preview-sidebar">
                                    <span>{examInfo.examName}</span>
                                    <strong>개념 목차</strong>
                                    <p>핵심 개념</p>
                                    <p>관련 용어</p>
                                </div>

                                <div className="preview-content">
                                    <h4>Chapter 01</h4>
                                    <h3>{examInfo.currentTopic}</h3>

                                    <div className="concept-preview-line" />

                                    <p>데이터 모델이란?</p>
                                </div>

                                <div className="ai-box">
                                    <span>AI 도우미</span>
                                    <i />
                                    <i />
                                </div>
                            </div>
                        </div>

                        <button
                            className="primary-button"
                            type="button"
                            onClick={handleStartConcept}
                        >
                            개념 학습 시작 →
                        </button>
                    </article>

                    <article className="start-card problem-card">
                        <div className="card-content">
                            <div className="card-icon blue">✓</div>

                            <span className="card-type">문제 중심 학습</span>

                            <h2>문제 풀이부터 시작</h2>

                            <p>
                                문제를 먼저 풀고, 해설과 오답노트를 통해
                                부족한 개념을 바로 복습해요.
                            </p>

                            <div className="tag-list problem-tags">
                                <span>문제 풀이</span>
                                <span>해설 확인</span>
                                <span>오답 정리</span>
                            </div>

                            <div className="preview-box problem-preview">
                                <div className="problem-list">
                                    <span />
                                    <span />
                                    <span />
                                    <span />
                                </div>

                                <div className="problem-content">
                                    <div className="problem-top-line" />

                                    <p>다음 중 데이터 모델링 설명으로 옳은 것은?</p>

                                    <ul>
                                        <li>보기 내용을 선택하세요.</li>
                                        <li>정답을 고르고 해설을 확인해요.</li>
                                        <li>오답은 자동으로 기록돼요.</li>
                                    </ul>
                                </div>
                            </div>
                        </div>

                        <button
                            className="primary-button"
                            type="button"
                            onClick={handleStartProblem}
                        >
                            문제 풀이 시작 →
                        </button>
                    </article>
                </section>
            </main>
        </div>
    );
}

export default MainPage;