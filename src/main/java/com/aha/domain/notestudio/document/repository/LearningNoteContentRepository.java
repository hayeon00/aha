package com.aha.domain.notestudio.document.repository;

import com.aha.domain.notestudio.document.entity.LearningNoteContent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningNoteContentRepository
        extends JpaRepository<LearningNoteContent, Long> {

    Optional<LearningNoteContent> findByLearningNote_IdAndExamScopeNode_Id(
            Long learningNoteId, Long examScopeNodeId);

    @EntityGraph(attributePaths = "examScopeNode")
    List<LearningNoteContent> findAllByLearningNote_ProcessingGroup_IdOrderByExamScopeNode_DisplayOrderAsc(
            Long processingGroupId);
}
