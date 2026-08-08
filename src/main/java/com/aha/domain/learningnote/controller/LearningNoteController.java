package com.aha.domain.learningnote.controller;

import com.aha.domain.learningnote.dto.response.LearningNoteCreateResponseDto;
import com.aha.domain.learningnote.service.LearningNoteCreateService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/learning-notes")
public class LearningNoteController {

    private final LearningNoteCreateService learningNoteCreateService;

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LearningNoteCreateResponseDto>> uploadDocument(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("userExamId") Long userExamId,
            @RequestParam("title") String title,
            @RequestPart("file") MultipartFile file
    ) {
        LearningNoteCreateResponseDto response =
                learningNoteCreateService.create(
                        user.getId(),
                        userExamId,
                        title,
                        file
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        ApiResponse.success(
                                202,
                                "문서 업로드 성공",
                                response
                        )
                );
    }






}
