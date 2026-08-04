import { useParams } from "react-router-dom";
import "./StudyRoomWaitingPage.css";

function StudyRoomWaitingPage() {
    const { studyRoomId } = useParams();

    return (
        <main className="study-waiting-page">
            <section className="study-waiting-placeholder">
                <span>STUDY ROOM #{studyRoomId}</span>
                <h1>스터디 대기방</h1>
                <p>
                    참가가 완료되었습니다. 대기방 기능은 다음 작업에서
                    연결합니다.
                </p>
            </section>
        </main>
    );
}

export default StudyRoomWaitingPage;
