package com.aha.domain.auth.service;

import com.aha.domain.auth.dto.request.LoginRequestDto;
import com.aha.domain.auth.dto.request.ReissueRequestDto;
import com.aha.domain.auth.dto.request.SignupRequestDto;
import com.aha.domain.auth.dto.response.LoginResponseDto;
import com.aha.domain.auth.dto.response.SignupResponseDto;
import com.aha.domain.auth.entity.RefreshToken;
import com.aha.domain.auth.repository.RefreshTokenRepository;
import com.aha.domain.user.entity.User;
import com.aha.domain.user.enums.UserRole;
import com.aha.domain.user.repository.UserRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {
        validateDuplicateEmail(request.getEmail());
        validateDuplicateNickname(request.getNickname());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        String name = request.getName() != null && !request.getName().isBlank()
                ? request.getName()
                : request.getNickname();

        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(name)
                .nickname(request.getNickname())
                .role(UserRole.USER)
                .status("ACTIVE")
                .loginType("LOCAL")
                .build();

        User savedUser = userRepository.save(user);

        return new SignupResponseDto(savedUser);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        validatePassword(request.getPassword(), user.getPassword());

        user.updateLastLoginAt();

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        savedRefreshToken -> savedRefreshToken.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(new RefreshToken(user, refreshToken))
                );

        return new LoginResponseDto(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );
    }

    @Transactional
    public LoginResponseDto reissue(ReissueRequestDto request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        RefreshToken savedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("저장되지 않은 리프레시 토큰입니다."));

        User user = savedRefreshToken.getUser();

        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        savedRefreshToken.updateToken(newRefreshToken);

        return new LoginResponseDto(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUser_Id(userId);
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}