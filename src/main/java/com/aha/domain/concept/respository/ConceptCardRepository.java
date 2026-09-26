package com.aha.domain.concept.respository;

import com.aha.domain.concept.entity.ConceptCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConceptCardRepository extends JpaRepository<ConceptCard, Long> {

    List<ConceptCard> findByConcept_IdOrderByDisplayOrderAscIdAsc(Long conceptId);
}