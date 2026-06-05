package com.aha.domain.user.service;

import com.aha.domain.user.dto.request.UpdateProfileRequestDto;
import com.aha.domain.user.dto.response.MyInfoResponseDto;
import com.aha.domain.user.entity.User;
import com.aha.domain.user.repository.UserRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final long MAX_PROFILE_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp");

    private final UserRepository userRepository;

    @Value("${file.profile-upload-dir:uploads/profile}")
    private String profileUploadDir;

    public MyInfoResponseDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return MyInfoResponseDto.from(user);
    }

    @Transactional
    public MyInfoResponseDto updateProfile(
            Long userId,
            UpdateProfileRequestDto request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (userRepository.existsByNicknameAndIdNot(request.nickname(), userId)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        user.updateProfileInfo(
                request.name(),
                request.nickname()
        );

        return MyInfoResponseDto.from(user);
    }


    @Transactional
    public MyInfoResponseDto updateProfileImage(
            Long userId,
            MultipartFile profileImage
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (profileImage == null || profileImage.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
        }

        if (profileImage.getSize() > MAX_PROFILE_IMAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
        }

        String originalFileName = profileImage.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank() || !originalFileName.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
        }

        String extension = originalFileName
                .substring(originalFileName.lastIndexOf(".") + 1)
                .toLowerCase();

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_PROFILE_IMAGE);
        }

        try {
            String storedFileName = UUID.randomUUID() + "." + extension;

            Path userProfilePath = Paths
                    .get(profileUploadDir, String.valueOf(userId))
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(userProfilePath);

            Path targetPath = userProfilePath
                    .resolve(storedFileName)
                    .toAbsolutePath()
                    .normalize();

            profileImage.transferTo(targetPath.toFile());

            String profileImageUrl = "/uploads/profile/" + userId + "/" + storedFileName;

            user.updateProfileImageUrl(profileImageUrl);

            return MyInfoResponseDto.from(user);
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패. userId={}, originalFileName={}, uploadDir={}",
                    userId,
                    originalFileName,
                    profileUploadDir,
                    e
            );

            throw new BusinessException(ErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
        }
    }
}