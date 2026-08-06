package com.aha.domain.study.controller;

import com.aha.domain.study.dto.request.StudyRoomHostChangeRequestDto;
import com.aha.domain.study.service.StudyRoomMemberService;
import com.aha.global.response.ApiResponse;
import com.aha.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/study-rooms")
public class StudyRoomMemberController {

    private final StudyRoomMemberService studyRoomMemberService;

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{studyRoomId}/members/me")
    public ResponseEntity<ApiResponse<Void>> leaveStudyRoom(
        @PathVariable("studyRoomId") Long studyRoomId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        studyRoomMemberService.leaveStudyRoom(studyRoomId, userDetails.getId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(204, "스터디룸 나가기 성공"));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{studyRoomId}/members/{studyRoomMemberId}")
    public ResponseEntity<ApiResponse<Void>> kickMember(
        @PathVariable("studyRoomId") Long studyRoomId,
        @PathVariable("studyRoomMemberId") Long memberId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        studyRoomMemberService.kickMember(studyRoomId, memberId, userDetails.getId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(204, "멤버 강퇴 성공"));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{studyRoomId}/host")
    public ResponseEntity<ApiResponse<Void>> changeHost(
        @PathVariable("studyRoomId") Long studyRoomId,
        @Valid @RequestBody StudyRoomHostChangeRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        studyRoomMemberService.changeHost(studyRoomId, requestDto, userDetails.getId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ApiResponse.success(204, "방장 변경 성공"));
    }
}
