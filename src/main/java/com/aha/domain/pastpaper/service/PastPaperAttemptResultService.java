package com.aha.domain.pastpaper.service;

import com.aha.domain.exam.entity.ExamPart;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.exam.enums.ExamScopeNodeType;
import com.aha.domain.exam.repository.ExamScopeNodeRepository;
import com.aha.domain.pastpaper.aggregation.PartResultAggregator;
import com.aha.domain.pastpaper.aggregation.SectionResultAggregator;
import com.aha.domain.pastpaper.aggregation.TotalResultAggregator;
import com.aha.domain.pastpaper.entity.PastPaperAttempt;
import com.aha.domain.pastpaper.entity.UserAnswer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PastPaperAttemptResultService {

    private final ExamScopeNodeRepository examScopeNodeRepository;

    public TotalResultAggregator gradeAttempt(PastPaperAttempt attempt, List<UserAnswer> userAnswers){
        userAnswers.forEach(UserAnswer::requestGrading);

        List<Long> partIds = attempt.getPastPaper().getExamVersion().getParts().stream().map(
            ExamPart::getId).toList();
        List<ExamScopeNode> sectionScopeNodes = examScopeNodeRepository.findAllByExamPart_IdInAndNodeTypeAndIsActiveTrueOrderByDisplayOrderAsc(
            partIds, ExamScopeNodeType.SECTION);
        Map<Long, SectionResultAggregator> sectionAggregatorMap =
            sectionScopeNodes.stream()
                .collect(Collectors.toMap(
                    ExamScopeNode::getId,
                    SectionResultAggregator::from,
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
                ));

        userAnswers.forEach(ua -> {
            Long sectionNodeId = ua.getProblem().getExamScopeNode().findSectionNode().getId();
            sectionAggregatorMap.get(sectionNodeId).aggregate(ua);
        });

        List<SectionResultAggregator> sectionAggregators = sectionAggregatorMap.values().stream()
            .toList();

        Map<Long, PartResultAggregator> partAggregatorMap =
            new LinkedHashMap<>();

        sectionAggregatorMap.values().forEach(sectionAggregator -> {
            Long partId = sectionAggregator.getPart().getId();

            PartResultAggregator partAggregator =
                partAggregatorMap.computeIfAbsent(
                    partId,
                    ignored -> PartResultAggregator.from(
                        sectionAggregator.getPart()
                    )
                );

            partAggregator.aggregate(sectionAggregator);
        });

        TotalResultAggregator totalAggregator = TotalResultAggregator.create(attempt.getPastPaper()
            .getExamVersion());
        partAggregatorMap.values().forEach(totalAggregator::aggregate);
        totalAggregator.evaluate();

        return totalAggregator;
    }
}
