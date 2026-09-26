package com.aha.domain.concept.entity;

import com.aha.domain.pastpaper.entity.Problem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "problem_concept",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_problem_concept",
                columnNames = {"problem_id", "concept_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemConcept {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concept_id", nullable = false)
    private Concept concept;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concept_card_id")
    private ConceptCard conceptCard;
}