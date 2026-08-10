package com.aha.domain.study.event.handler;

import com.aha.domain.study.event.application.StudyRoomReadyUpdatedApplicationEvent;
import com.aha.domain.study.event.payload.StudyRoomReadyUpdatedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StudyRoomReadyUpdatedEventHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
        StudyRoomReadyUpdatedApplicationEvent event
    ){

        String destination =
            "/topic/study-rooms/%d/events"
                .formatted(event.studyRoomId());

        messagingTemplate.convertAndSend(
            destination,
            StudyRoomReadyUpdatedPayload.from(event)
        );
    }
}
