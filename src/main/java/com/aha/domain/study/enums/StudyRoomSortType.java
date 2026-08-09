package com.aha.domain.study.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
@Getter
public enum StudyRoomSortType {
    LATEST(
        Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
        )
    ),
    OLDEST(
        Sort.by(
            Sort.Order.asc("createdAt"),
            Sort.Order.asc("id")
        )
    ),

    MOST_MEMBERS(
        Sort.by(
            Sort.Order.desc("memberCount"),
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
        )
    );

    private final Sort sort;


}
