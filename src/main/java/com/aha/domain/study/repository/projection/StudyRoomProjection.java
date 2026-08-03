package com.aha.domain.study.repository.projection;

import com.aha.domain.study.entity.StudyRoom;

public interface StudyRoomProjection {

    StudyRoom getStudyRoom();
    String getHostNickname();
    String getHostProfileImageUrl();
    String getExamName();
    int getPastPaperYear();
    int getPastPaperRoundNo();
    long getMemberCount();
}
