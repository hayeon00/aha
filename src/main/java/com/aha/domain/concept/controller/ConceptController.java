package com.aha.domain.concept.controller;

import com.aha.domain.concept.dto.response.ConceptDetailResponse;
import com.aha.domain.concept.dto.response.ConceptListItemResponse;
import com.aha.domain.concept.service.ConceptService;
import com.aha.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/concepts")
public class ConceptController {

    private final ConceptService conceptService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConceptListItemResponse>>> getConcepts(
            @RequestParam Long examVersionId,
            @RequestParam(required = false) Long examScopeNodeId
    ) {
        List<ConceptListItemResponse> response =
                conceptService.getConcepts(examVersionId, examScopeNodeId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "핵심 개념 목록 조회에 성공했습니다.",
                        response
                )
        );
    }

    @GetMapping("/{conceptId}")
    public ResponseEntity<ApiResponse<ConceptDetailResponse>> getConcept(
            @PathVariable Long conceptId
    ) {
        ConceptDetailResponse response = conceptService.getConcept(conceptId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "핵심 개념 상세 조회에 성공했습니다.",
                        response
                )
        );
    }
}
