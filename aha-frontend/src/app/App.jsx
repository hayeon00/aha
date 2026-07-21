import {
    Routes,
    Route,
    Navigate,
    useNavigate,
} from "react-router-dom";

import LoginPage from "../features/auth/pages/LoginPage.jsx";
import OAuthCallbackPage from "../features/auth/pages/OAuthCallbackPage.jsx";
import SignupPage from "../features/auth/pages/SignupPage.jsx";
import AiLearningPage from "../features/ailearn/pages/AiLearningPage.jsx";
import MainPage from "../features/home/pages/MainPage.jsx";
import MyPage from "../features/user/pages/MyPage.jsx";
import WorkbookPage from "../features/workbook/pages/WorkbookPage.jsx";
import WorkbookAttemptPage from "../features/workbook/pages/WorkbookAttemptPage.jsx";
import WorkbookResultPage from "../features/workbook/pages/WorkbookResultPage.jsx";
import MainLayout from "../common/layouts/MainLayout.jsx";

import { useAuth } from "../features/auth/context/useAuth.js";

function App() {
    const navigate = useNavigate();

    const {
        isLoggedIn,
        isAuthInitialized,
        login,
        logout,
    } = useAuth();

    const handleLoginSuccess = (accessToken) => {
        login(accessToken);

        navigate("/main", {
            replace: true,
        });
    };

    const handleLogout = async () => {
        try {
            await logout();

            navigate("/login", {
                replace: true,
            });
        } catch (error) {
            console.error("로그아웃 실패:", error);

            navigate("/login", {
                replace: true,
            });
        }
    };

    if (!isAuthInitialized) {
        return (
            <main
                style={{
                    minHeight: "100vh",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                }}
            >
                <p>로그인 상태를 확인하고 있습니다.</p>
            </main>
        );
    }

    return (
        <Routes>
            <Route
                path="/"
                element={
                    <Navigate
                        to={
                            isLoggedIn
                                ? "/main"
                                : "/login"
                        }
                        replace
                    />
                }
            />

            <Route
                path="/login"
                element={
                    isLoggedIn ? (
                        <Navigate
                            to="/main"
                            replace
                        />
                    ) : (
                        <LoginPage
                            onLoginSuccess={
                                handleLoginSuccess
                            }
                            onMoveSignup={() =>
                                navigate("/signup")
                            }
                        />
                    )
                }
            />

            <Route
                path="/oauth/callback"
                element={
                    <OAuthCallbackPage
                        onLoginSuccess={
                            handleLoginSuccess
                        }
                    />
                }
            />

            <Route
                path="/signup"
                element={
                    isLoggedIn ? (
                        <Navigate
                            to="/main"
                            replace
                        />
                    ) : (
                        <SignupPage
                            onMoveLogin={() =>
                                navigate("/login")
                            }
                        />
                    )
                }
            />

            <Route
                element={
                    isLoggedIn ? (
                        <MainLayout
                            onLogout={
                                handleLogout
                            }
                        />
                    ) : (
                        <Navigate
                            to="/login"
                            replace
                        />
                    )
                }
            >
                <Route
                    path="/main"
                    element={<MainPage />}
                />

                <Route
                    path="/learning"
                    element={<AiLearningPage />}
                />

                <Route
                    path="/problems"
                    element={<WorkbookPage />}
                />

                <Route
                    path="/problems/:workbookId/attempts/:attemptId"
                    element={
                        <WorkbookAttemptPage />
                    }
                />

                <Route
                    path="/problems/:workbookId/attempts/:attemptId/result"
                    element={<WorkbookResultPage />}
                />
                <Route
                    path="/mypage"
                    element={<MyPage />}
                />
            </Route>

            <Route
                path="*"
                element={
                    <Navigate
                        to={
                            isLoggedIn
                                ? "/main"
                                : "/login"
                        }
                        replace
                    />
                }
            />
        </Routes>
    );
}

export default App;