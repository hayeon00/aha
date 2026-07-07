package com.aha.domain.ailearn.document.repository;

import com.aha.domain.ailearn.document.entity.UserLearningContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserLearningContentRepository extends JpaRepository<UserLearningContent, Long> {

    Optional<UserLearningContent> findByUserExam_IdAndExamScopeNode_Id( Long userExamId, Long examScopeNodeId );

    @Query("""
    select content
    from UserLearningContent content
    join fetch content.userExam userExam
    join fetch content.examScopeNode scopeNode
    where userExam.id = :userExamId
      and userExam.user.id = :userId
      and scopeNode.id = :examScopeNodeId
    """)
    Optional<UserLearningContent>
    findByUserIdAndUserExamIdAndExamScopeNodeId(
            @Param("userId") Long userId,
            @Param("userExamId") Long userExamId,
            @Param("examScopeNodeId") Long examScopeNodeId
    );

}