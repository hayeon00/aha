package com.aha.domain.exam.service;

import com.aha.domain.exam.dto.response.ExamScopeNodeResponseDto;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.enums.ExamScopeNodeType;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamScopeNodeService {

    private static final List<ExamScopeNodeType> USER_VISIBLE_NODE_TYPES =
            List.of(
                    ExamScopeNodeType.SECTION,
                    ExamScopeNodeType.TOPIC
            );

    private final ExamScopeNodeRepository examScopeNodeRepository;

    public List<ExamScopeNodeResponseDto> getSyllabusTree(Long examVersionId) {
        List<ExamScopeNode> nodes =
                examScopeNodeRepository
                        .findActiveNodesByExamVersionIdAndNodeTypes(
                                examVersionId,
                                USER_VISIBLE_NODE_TYPES
                        );

        Map<Long, ExamScopeNodeResponseDto> responseMap =
                new LinkedHashMap<>();

        List<ExamScopeNodeResponseDto> roots =
                new ArrayList<>();

        for (ExamScopeNode node : nodes) {
            responseMap.put(
                    node.getId(),
                    ExamScopeNodeResponseDto.from(node)
            );
        }

        for (ExamScopeNode node : nodes) {
            ExamScopeNodeResponseDto current =
                    responseMap.get(node.getId());

            if (node.getParent() == null) {
                roots.add(current);
                continue;
            }

            ExamScopeNodeResponseDto parent =
                    responseMap.get(node.getParent().getId());

            if (parent != null) {
                parent.children().add(current);
            }
        }

        return roots;
    }
}