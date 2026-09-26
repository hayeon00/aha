package com.aha.domain.concept.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "concept_card",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_card_concept_order",
                        columnNames = {"concept_id", "display_order"}
                ),
                @UniqueConstraint(
                        name = "uk_card_id_concept",
                        columnNames = {"id", "concept_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConceptCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concept_id", nullable = false)
    private Concept concept;

    @Column(nullable = false, length = 150)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "body", nullable = false, columnDefinition = "json")
    private String body;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}