package com.aha.domain.exam.dto.response;

import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.enums.ExamScopeNodeType;

import java.util.ArrayList;
import java.util.List;

public record ExamScopeNodeResponseDto(
        Long id,
        String code,
        Long parentId,
        ExamScopeNodeType nodeType,
        Integer depth,
        String title,
        boolean isLeaf,
        Integer displayOrder,
        List<ExamScopeNodeResponseDto> children
) {

    public static ExamScopeNodeResponseDto from(ExamScopeNode node) {
        return new ExamScopeNodeResponseDto(
                node.getId(),
                node.getCode(),
                node.getParent() != null ? node.getParent().getId() : null,
                node.getNodeType(),
                node.getDepth(),
                node.getTitle(),
                node.isLeaf(),
                node.getDisplayOrder(),
                new ArrayList<>()
        );
    }
}