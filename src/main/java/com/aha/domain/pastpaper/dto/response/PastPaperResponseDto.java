package com.aha.domain.pastpaper.dto.response;

import com.aha.domain.exam.entity.Exam;
import com.aha.domain.pastpaper.entity.PastPaper;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PastPaperResponseDto(

    long pastPaperId,
    long totalItemCount,
    boolean reviewed,
    String title,
    long year,
    long roundNo,
    long timeLimit,
    LocalDate examDate
) {

    public static PastPaperResponseDto of(PastPaper paper,Exam exam) {
        String examName = exam.getName();

        int year = paper.getYear();
        int roundNo = paper.getRoundNo();

        return PastPaperResponseDto.builder()
            .pastPaperId(paper.getId())
            .totalItemCount(paper.getTotalItemCount())
            .reviewed(paper.isReviewed())
            .title(createTitle(examName, year, roundNo))
            .year(year)
            .roundNo(roundNo)
            .timeLimit(paper.getTimeLimit())
            .examDate(paper.getExamDate())
            .build();
    }

    private static String createTitle(String examName, int year, int roundNo) {
        return "%s %d년 %d회차 시험".formatted(examName, year, roundNo);
    }
}
