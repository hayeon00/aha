package com.aha.domain.concept.dto.response;

import com.aha.domain.concept.entity.Concept;
import com.aha.domain.concept.entity.ConceptCard;

import java.util.List;

public record ConceptDetailResponse(
        Long conceptId,
        String code,
        String title,
        String summary,
        List<CardResponse> cards
) {
    public static ConceptDetailResponse of(Concept concept, List<ConceptCard> cards) {
        return new ConceptDetailResponse(
                concept.getId(),
                concept.getCode(),
                concept.getTitle(),
                concept.getSummary(),
                cards.stream().map(CardResponse::from).toList()
        );
    }

    public record CardResponse(
            Long cardId,
            String title,
            String body,
            int displayOrder
    ) {
        public static CardResponse from(ConceptCard card) {
            return new CardResponse(
                    card.getId(),
                    card.getTitle(),
                    card.getBody(),
                    card.getDisplayOrder()
            );
        }
    }
}