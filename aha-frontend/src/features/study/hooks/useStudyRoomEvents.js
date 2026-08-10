import { useEffect, useRef } from "react";
import { createStompClient } from "../../../common/websocket/stompClient.js";

const STUDY_ROOM_STARTED = "STUDY_ROOM_STARTED";
const STUDY_ROOM_MEMBER_READY_UPDATED =
    "STUDY_ROOM_MEMBER_READY_UPDATED";

export const useStudyRoomEvents = ({
    studyRoomId,
    onConnected,
    onStarted,
    onReadyUpdated,
    onError,
}) => {
    const onConnectedRef = useRef(onConnected);
    const onStartedRef = useRef(onStarted);
    const onReadyUpdatedRef = useRef(onReadyUpdated);
    const onErrorRef = useRef(onError);

    useEffect(() => {
        onConnectedRef.current = onConnected;
        onStartedRef.current = onStarted;
        onReadyUpdatedRef.current = onReadyUpdated;
        onErrorRef.current = onError;
    }, [onConnected, onStarted, onReadyUpdated, onError]);

    useEffect(() => {
        if (!studyRoomId) {
            return undefined;
        }

        const client = createStompClient();
        let subscription;

        client.onConnect = () => {
            subscription = client.subscribe(
                `/topic/study-rooms/${studyRoomId}/events`,
                (message) => {
                    try {
                        const event = JSON.parse(message.body);

                        if (event.type === STUDY_ROOM_STARTED) {
                            onStartedRef.current?.(event);
                        }

                        if (
                            event.type ===
                            STUDY_ROOM_MEMBER_READY_UPDATED
                        ) {
                            onReadyUpdatedRef.current?.(event);
                        }
                    } catch (error) {
                        onErrorRef.current?.(error);
                    }
                }
            );

            onConnectedRef.current?.();
        };

        client.onStompError = (frame) => {
            onErrorRef.current?.(
                new Error(frame.headers?.message || "스터디룸 이벤트 연결 오류")
            );
        };

        client.onWebSocketError = (error) => {
            onErrorRef.current?.(error);
        };

        client.activate();

        return () => {
            subscription?.unsubscribe();
            void client.deactivate();
        };
    }, [studyRoomId]);
};
