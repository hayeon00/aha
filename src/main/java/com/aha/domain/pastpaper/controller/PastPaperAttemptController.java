package com.aha.domain.pastpaper.controller;

import com.aha.domain.pastpaper.dto.request.AnswerSaveRequestDto;
import com.aha.domain.pastpaper.dto.response.PastPaperAttemptAnswersResponseDto;
import com.aha.domain.pastpaper.dto.response.PastPaperAttemptStartResponseDto;
import com.aha.domain.pastpaper.dto.response.PastPaperItemResponseDto;
import com.aha.domain.pastpaper.dto.response.result.PastPaperAttemptResponseDto;
import com.aha.domain.pastpaper.dto.response.result.PastPaperAttemptResultResponseDto;
import com.aha.domain.pastpaper.dto.response.result.PastPaperAttemptSubmitResponseDto;
import com.aha.domain.pastpaper.enums.PastPaperAttemptStatus;
import com.aha.domain.pastpaper.service.PastPaperAttemptService;
import com.aha.domain.pastpaper.service.PastPaperItemService;
import com.aha.domain.pastpaper.service.UserAnswerService;
import com.aha.global.response.ApiResponse;
import com.aha.global.response.PageResponseDto;
import com.aha.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PastPaperAttemptController {

    private final PastPaperAttemptService pastPaperAttemptService;
    private final PastPaperItemService pastPaperItemService;
    private final UserAnswerService userAnswerService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/past-papers/{pastPaperId}/attempts")
    public ResponseEntity<ApiResponse<PastPaperAttemptStartResponseDto>> startAttempt(
        @PathVariable("pastPaperId") Long pastPaperId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok().body(ApiResponse.success(200, "풀이를 시작 성공",
            pastPaperAttemptService.getOrStartAttempt(pastPaperId, userDetails))
        );

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/past-paper-attempts/{pastPaperAttemptId}/items")
    public ResponseEntity<ApiResponse<List<PastPaperItemResponseDto>>> getItems(
        @PathVariable("pastPaperAttemptId") Long pastPaperAttemptId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok().body(ApiResponse.success(200, "문항 목록 조회 성공",
            pastPaperItemService.getItems(pastPaperAttemptId, userDetails)
        ));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/past-paper-attempts/{pastPaperAttemptId}/answers")
    public ResponseEntity<ApiResponse<PastPaperAttemptAnswersResponseDto>> getAttemptAnswers(
        @PathVariable("pastPaperAttemptId") Long pastPaperAttemptId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok().body(ApiResponse.success(200, "사용자 답안 목록 조회 성공",
            userAnswerService.getAttemptsAnswer(pastPaperAttemptId, userDetails)
        ));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/past-paper-attempts/{pastPaperAttemptId}/problems/{problemId}/answers")
    public ResponseEntity<ApiResponse<Void>> saveAnswer(
        @PathVariable("pastPaperAttemptId") Long pastPaperAttemptId,
        @PathVariable("problemId") Long problemId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody @Valid AnswerSaveRequestDto request
    ) {
        userAnswerService.saveAnswer(pastPaperAttemptId, problemId, request, userDetails);
        return ResponseEntity.ok()
            .body(ApiResponse.success(200, "사용자 답안 임시 저장 성공"));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/past-paper-attempts/{pastPaperAttemptId}/problems/{problemId}/review-mark")
    public ResponseEntity<ApiResponse<Void>> toggleMarkedForReview(
        @PathVariable("pastPaperAttemptId") Long pastPaperAttemptId,
        @PathVariable("problemId") Long problemId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userAnswerService.toggleMarkedForReview(pastPaperAttemptId, problemId, userDetails);
        return ResponseEntity.ok()
            .body(ApiResponse.success(200, "검토 여부 토글 성공"));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/past-paper-attempts/{pastPaperAttemptId}/submit")
    public ResponseEntity<ApiResponse<PastPaperAttemptSubmitResponseDto>> submitAttempt(
        @PathVariable("pastPaperAttemptId") Long pastPaperAttemptId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok()
            .body(ApiResponse.success(200, "제출 및 채점이 완료되었습니다.",
                pastPaperAttemptService.submitAttempt(pastPaperAttemptId, userDetails)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/past-paper-attempts")
    public ResponseEntity<ApiResponse<PageResponseDto<PastPaperAttemptResponseDto>>> getAttempt(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PageableDefault(
            page = 0,
            size = 10,
            sort = "startedAt",
            direction = Sort.Direction.DESC
        ) Pageable pageable,
        @RequestParam(
            name = "attemptStatus",
            defaultValue = "GRADED"
        ) PastPaperAttemptStatus status
    ) {

        return ResponseEntity.ok(ApiResponse.success(200, "풀이 목록 조회 성공",
            pastPaperAttemptService.getAttempts(userDetails, pageable, status)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/past-paper-attempts/{pastPaperAttemptId}")
    public ResponseEntity<ApiResponse<PastPaperAttemptResultResponseDto>> getResult(
        @PathVariable("pastPaperAttemptId") Long pastPaperAttemptId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok()
            .body(ApiResponse.success(200, "결과 조회 성공",
                pastPaperAttemptService.getResult(userDetails, pastPaperAttemptId)));
    }

}
