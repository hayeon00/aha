package com.aha.domain.ailearn.assistant.controller;

import com.aha.domain.ailearn.assistant.dto.request.AssistantMessageRequest;
import com.aha.domain.ailearn.assistant.dto.response.AssistantMessageResponse;
import com.aha.domain.ailearn.assistant.service.LearningAssistantService;
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
public class LearningAssistantController {

    private final LearningAssistantService learningAssistantService;

    @PostMapping("/{learningSessionId}/ai-messages")
    public ApiResponse<AssistantMessageResponse> createAssistantMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long learningSessionId,
            @Valid @RequestBody AssistantMessageRequest request
    ) {

        AssistantMessageResponse response =
                learningAssistantService.createAssistantMessage(
                        userDetails.getId(),
                        learningSessionId,
                        request
                );

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AI 도우미 응답 생성에 성공했습니다.",
                response
        );
    }
}