package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.UserLearningContent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLearningContentRepository
        extends JpaRepository<UserLearningContent, Long> {

    Optional<UserLearningContent>
    findByUserExam_IdAndExamScopeNode_Id(
            Long userExamId,
            Long examScopeNodeId
    );

    @EntityGraph(attributePaths = "examScopeNode")
    List<UserLearningContent>
    findAllByUserExam_IdOrderByExamScopeNode_DisplayOrderAsc(
            Long userExamId
    );

    @EntityGraph(attributePaths = "examScopeNode")
    Optional<UserLearningContent>
    findByUserExam_IdAndExamScopeNode_IdAndUserExam_User_Id(
            Long userExamId,
            Long examScopeNodeId,
            Long userId
    );
}
