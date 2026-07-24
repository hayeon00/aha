import { useCallback } from "react";
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
import PastPaperListPage from "../features/pastpaper/pages/PastPaperListPage.jsx";
import PastPaperAttemptPage from "../features/pastpaper/pages/PastPaperAttemptPage.jsx";
import PastPaperResultPage from "../features/pastpaper/pages/PastPaperResultPage.jsx";
import PastPaperExplanationPage from "../features/pastpaper/pages/PastPaperExplanationPage.jsx";
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

    const handleLoginSuccess = useCallback(
        (accessToken) => {
            login(accessToken);

            navigate("/main", {
                replace: true,
            });
        },
        [login, navigate],
    );

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
                    path="/past-papers"
                    element={<PastPaperListPage />}
                />

                <Route
                    path="/past-papers/:pastPaperId/attempts/:attemptId"
                    element={<PastPaperAttemptPage />}
                />

                <Route
                    path="/past-papers/:pastPaperId/attempts/:attemptId/result"
                    element={<PastPaperResultPage />}
                />
                <Route
                    path="/past-papers/:pastPaperId/attempts/:attemptId/explanation"
                    element={<PastPaperExplanationPage />}
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
