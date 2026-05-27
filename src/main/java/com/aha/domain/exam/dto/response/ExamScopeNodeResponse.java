package com.aha.domain.exam.dto.response;

import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.entity.ScopeNodeType;

import java.util.ArrayList;
import java.util.List;

public record ExamScopeNodeResponse(
        Long id,
        String code,
        Long parentId,
        ScopeNodeType nodeType,
        Integer depth,
        String title,
        boolean isLeaf,
        Integer displayOrder,
        List<ExamScopeNodeResponse> children
) {

    public static ExamScopeNodeResponse from(ExamScopeNode node) {
        return new ExamScopeNodeResponse(
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