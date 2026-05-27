import "./MyPage.css";

function MyPage({ onLogout }) {
    return (
        <div className="mypage">
            <header className="mypage-header">
                <h1>마이페이지</h1>
            </header>

            <main className="mypage-content">
                <section className="mypage-card">
                    <h2>내 정보</h2>
                    <p>사용자 정보를 확인하고 관리할 수 있는 화면입니다.</p>
                </section>
            </main>
        </div>
    );
}

export default MyPage;