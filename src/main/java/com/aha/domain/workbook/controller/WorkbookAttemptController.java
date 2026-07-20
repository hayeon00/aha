package com.aha.domain.workbook.controller;

import com.aha.domain.workbook.dto.request.UserAnswerRequestDto;
import com.aha.domain.workbook.dto.response.AttemptResultResponseDto;
import com.aha.domain.workbook.dto.response.AttemptSubmitResponseDto;
import com.aha.domain.workbook.dto.response.UserAnswerResponseDto;
import com.aha.domain.workbook.service.WorkbookAttemptService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workbook-attempts")
public class WorkbookAttemptController {

    private final WorkbookAttemptService workbookAttemptService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{attemptId}/answers")
    public ResponseEntity<ApiResponse<List<UserAnswerResponseDto>>> getUserAnswers(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Min(1) @PathVariable Long attemptId
    ) {
        List<UserAnswerResponseDto> response = workbookAttemptService.getUserAnswers(attemptId,
            userDetails);

        return ResponseEntity.ok().body(ApiResponse.success(200, "사용자 답안 조회 완료", response));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{attemptId}/problems/{problemId}/answers")
    public ResponseEntity<ApiResponse<Void>> saveUserAnswer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Min(1) @PathVariable Long attemptId,
        @Min(1) @PathVariable Long problemId,
        @Valid @RequestBody UserAnswerRequestDto requestDto
    ) {
        workbookAttemptService.saveUserAnswer(attemptId, problemId, userDetails,requestDto);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(204, "사용자 답안 생성 또는 변경 완료"));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{attemptId}/problems/{problemId}/check")
    public ResponseEntity<ApiResponse<Void>> checkUserAnswer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Min(1) @PathVariable Long attemptId,
        @Min(1) @PathVariable Long problemId
    ){
        workbookAttemptService.checkUserAnswer(attemptId,problemId,userDetails);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(204,"문제 토클 성공"));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<ApiResponse<AttemptSubmitResponseDto>> submitAttempt(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Min(1) @PathVariable Long attemptId
    ){
        AttemptSubmitResponseDto response = workbookAttemptService.submitAttempt(userDetails,attemptId);

        return ResponseEntity.ok().body(ApiResponse.success(200,"제출이 완료되었습니다.",response));

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{attemptId}")
    public ResponseEntity<ApiResponse<AttemptResultResponseDto>> getResult(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Min(1) @PathVariable Long attemptId
    ){
        AttemptResultResponseDto response = workbookAttemptService.getResult(userDetails,attemptId);

        return ResponseEntity.ok().body(ApiResponse.success(200,"채점 결과 조회 성공",response));
    }


}
