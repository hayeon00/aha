package com.aha.domain.study.controller;

import com.aha.domain.study.dto.request.StudyRoomCreateRequestDto;
import com.aha.domain.study.dto.response.StudyRoomCreateResponseDto;
import com.aha.domain.study.dto.response.StudyRoomDetailResponseDto;
import com.aha.domain.study.dto.response.StudyRoomJoinResponseDto;
import com.aha.domain.study.dto.response.StudyRoomResponseDto;
import com.aha.domain.study.enums.StudyRoomSortType;
import com.aha.domain.study.enums.StudyRoomStatus;
import com.aha.domain.study.service.StudyRoomService;
import com.aha.global.response.ApiResponse;
import com.aha.global.response.PageResponseDto;
import com.aha.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/study-rooms")
    public ResponseEntity<ApiResponse<StudyRoomCreateResponseDto>> createStudyRoom(
        @Valid @RequestBody StudyRoomCreateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse.success(
                    201,
                    "스터디룸 생성 완료",
                    studyRoomService.createStudyRoom(requestDto, userDetails.getId())
                )
            );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/study-rooms")
    public ResponseEntity<ApiResponse<PageResponseDto<StudyRoomResponseDto>>> getStudyRooms(
        @RequestParam(name = "examVersionId", required = true)
        @Min(1) long examVersionId,

        @RequestParam(name = "status", defaultValue = "WAITING")
        StudyRoomStatus status,

        @RequestParam(name = "page", defaultValue = "0")
        @Min(0) int page,

        @RequestParam(name = "size", defaultValue = "10")
        @Min(1) @Max(100) int size,

        @RequestParam(name = "sortType", defaultValue = "LATEST")
        StudyRoomSortType sortType
    ) {

        return ResponseEntity.ok()
            .body(
                ApiResponse.success(
                    200,
                    "스터디룸 목록 조회 성공",
                    studyRoomService.getStudyRooms(examVersionId, status, page, size, sortType)
                )
            );
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/study-rooms/{studyRoomId}")
    public ResponseEntity<ApiResponse<StudyRoomDetailResponseDto>> getStudyRoom(
        @PathVariable(name = "studyRoomId") Long studyRoomId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok()
            .body(
                ApiResponse.success(
                    200,
                    "스터디룸 상세 조회 성공",
                    studyRoomService.getStudyRoom(studyRoomId, userDetails.getId())
                )
            );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/me/study-rooms/current")
    public ResponseEntity<ApiResponse<StudyRoomDetailResponseDto>> getCurrentStudyRoom(
        @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {

        return ResponseEntity.ok()
            .body(
                ApiResponse.success(
                    200,
                    "참가한 현재 스터디룸 조회 성공",
                    studyRoomService.getCurrentStudyRoom(customUserDetails.getId())
                )
            );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/study-rooms/{studyRoomId}/members")
    public ResponseEntity<ApiResponse<StudyRoomJoinResponseDto>> joinStudyRoom(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable("studyRoomId") Long studyRoomId
    ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse.success(
                    201,
                    "스터디룸 참가 성공",
                    studyRoomService.joinStudyRoom(customUserDetails.getId(), studyRoomId)
                )
            );
    }

}
