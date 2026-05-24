package com.aha.domain.ailearn.session.controller;


import com.aha.domain.ailearn.session.dto.request.ConceptProblemSubmitRequest;
import com.aha.domain.ailearn.session.dto.request.LearningSessionCreateRequest;
import com.aha.domain.ailearn.session.dto.response.ConceptProblemListResponse;
import com.aha.domain.ailearn.session.dto.response.ConceptProblemSubmitResponse;
import com.aha.domain.ailearn.session.dto.response.LearningSessionCreateResponse;
import com.aha.domain.ailearn.session.service.ConceptProblemService;
import com.aha.domain.ailearn.session.service.LearningSessionService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/learning/sessions")
@RequiredArgsConstructor
public class LearningSessionController {

    private final LearningSessionService learningSessionService;
    private final ConceptProblemService conceptProblemService;

    @PostMapping
    public ApiResponse<LearningSessionCreateResponse> createSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LearningSessionCreateRequest request
    ) {
        LearningSessionCreateResponse response =
                learningSessionService.createSession(userDetails.getId(), request);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "학습 세션 생성에 성공했습니다.",
                response
        );
    }


    @GetMapping("/{learningSessionId}/concept-problems")
    public ApiResponse<ConceptProblemListResponse> getConceptProblems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long learningSessionId
    ) {
        ConceptProblemListResponse response =
                conceptProblemService.getConceptProblems(
                        userDetails.getId(),
                        learningSessionId
                );

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "개념확인 문제 조회에 성공했습니다.",
                response
        );
    }


    @PostMapping("/{learningSessionId}/concept-problems/submit")
    public ApiResponse<ConceptProblemSubmitResponse> submitConceptProblems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long learningSessionId,
            @Valid @RequestBody ConceptProblemSubmitRequest request
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인 사용자 정보가 없습니다. Authorization 헤더를 확인해주세요.");
        }

        ConceptProblemSubmitResponse response =
                conceptProblemService.submitConceptProblems(
                        userDetails.getId(),
                        learningSessionId,
                        request
                );

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "개념확인 문제 채점에 성공했습니다.",
                response
        );
    }

    @GetMapping("/{learningSessionId}/concept-problems/result")
    public ApiResponse<ConceptProblemSubmitResponse> getSubmittedConceptProblemResult(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long learningSessionId
    ) {

        ConceptProblemSubmitResponse response =
                conceptProblemService.getSubmittedConceptProblemResult(
                        userDetails.getId(),
                        learningSessionId
                );

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "개념확인 문제 풀이 결과 조회에 성공했습니다.",
                response
        );
    }



}