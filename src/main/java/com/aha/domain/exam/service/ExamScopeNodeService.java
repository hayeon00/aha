package com.aha.domain.exam.service;

import com.aha.domain.exam.dto.ExamScopeNodeResponse;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamScopeNodeService {

    private final ExamScopeNodeRepository examScopeNodeRepository;

    public List<ExamScopeNodeResponse> getSyllabusTree(Long examVersionId) {
        List<ExamScopeNode> nodes =
                examScopeNodeRepository.findActiveNodesByExamVersionId(examVersionId);

        Map<Long, ExamScopeNodeResponse> responseMap = new LinkedHashMap<>();
        List<ExamScopeNodeResponse> roots = new ArrayList<>();

        for (ExamScopeNode node : nodes) {
            responseMap.put(node.getId(), ExamScopeNodeResponse.from(node));
        }

        for (ExamScopeNode node : nodes) {
            ExamScopeNodeResponse current = responseMap.get(node.getId());

            if (node.getParent() == null) {
                roots.add(current);
                continue;
            }

            ExamScopeNodeResponse parent = responseMap.get(node.getParent().getId());

            if (parent != null) {
                parent.children().add(current);
            }
        }

        return roots;
    }
}