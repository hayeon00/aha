package com.aha.domain.pastpaper.controller;

import com.aha.domain.pastpaper.dto.response.PastPaperResponseDto;
import com.aha.domain.pastpaper.service.PastPaperService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PastPaperController {

    private final PastPaperService pastPaperService;

    @GetMapping("/exam-versions/{versionId}/past-papers")
    public ResponseEntity<ApiResponse<List<PastPaperResponseDto>>> getPastPapers(
        @PathVariable("versionId") Long versionId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){

        return ResponseEntity.ok()
            .body(ApiResponse.success(200,"복원 기출 목록 조회 성공",pastPaperService.getPastPapers(versionId,userDetails)));
    }

}
