package com.aha.domain.ailearn.session.controller;


import com.aha.domain.ailearn.conceptproblem.dto.request.ConceptProblemSubmitRequest;
import com.aha.domain.ailearn.session.dto.request.LearningSessionCreateRequest;
import com.aha.domain.ailearn.conceptproblem.dto.response.ConceptProblemListResponse;
import com.aha.domain.ailearn.conceptproblem.dto.response.ConceptProblemSubmitResponse;
import com.aha.domain.ailearn.session.dto.response.LearningSessionCreateResponse;
import com.aha.domain.ailearn.conceptproblem.service.ConceptProblemService;
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
}