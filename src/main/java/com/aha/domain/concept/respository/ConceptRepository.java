package com.aha.domain.concept.respository;

import com.aha.domain.concept.entity.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConceptRepository extends JpaRepository<Concept, Long> {

    @Query("""
        select c
        from Concept c
        join fetch c.examScopeNode s
        where c.examVersion.id = :examVersionId
        order by s.id, c.displayOrder, c.id
        """)
    List<Concept> findAllWithScopeByExamVersionId(
            @Param("examVersionId") Long examVersionId
    );

    List<Concept> findByExamVersion_IdAndExamScopeNode_IdOrderByDisplayOrderAscIdAsc(
            Long examVersionId,
            Long examScopeNodeId
    );
}
