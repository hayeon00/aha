package com.aha.domain.workbook.aggregation;

import java.util.List;
import lombok.Builder;

@Builder
public record AttemptStatJson(

    List<AttemptPartResult> partResults
) {
    public static AttemptStatJson create(List<AttemptPartResult> partResults){
        return AttemptStatJson.builder()
            .partResults(partResults)
            .build();
    }
}
