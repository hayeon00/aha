package com.aha.domain.exam.service;

import com.aha.domain.exam.dto.response.ExamScopeNodeResponseDto;
import com.aha.domain.exam.entity.ExamScopeNode;
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

    private final ExamScopeNodeRepository examScopeNodeRepository;

    public List<ExamScopeNodeResponseDto> getSyllabusTree(Long examVersionId) {
        List<ExamScopeNode> nodes =
                examScopeNodeRepository.findActiveNodesByExamVersionId(examVersionId);

        Map<Long, ExamScopeNodeResponseDto> responseMap = new LinkedHashMap<>();
        List<ExamScopeNodeResponseDto> roots = new ArrayList<>();

        for (ExamScopeNode node : nodes) {
            responseMap.put(node.getId(), ExamScopeNodeResponseDto.from(node));
        }

        for (ExamScopeNode node : nodes) {
            ExamScopeNodeResponseDto current = responseMap.get(node.getId());

            if (node.getParent() == null) {
                roots.add(current);
                continue;
            }

            ExamScopeNodeResponseDto parent = responseMap.get(node.getParent().getId());

            if (parent != null) {
                parent.children().add(current);
            }
        }

        return roots;
    }
}