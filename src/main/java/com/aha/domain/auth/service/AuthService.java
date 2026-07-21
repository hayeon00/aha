package com.aha.domain.auth.service;

import com.aha.domain.auth.dto.request.LoginRequestDto;
import com.aha.domain.auth.dto.request.SignupRequestDto;
import com.aha.domain.auth.dto.response.SignupResponseDto;
import com.aha.domain.auth.entity.RefreshToken;
import com.aha.domain.auth.repository.RefreshTokenRepository;
import com.aha.domain.user.entity.User;
import com.aha.domain.user.enums.UserRole;
import com.aha.domain.user.enums.UserStatus;
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
    private final AuthTokenService authTokenService;
    private final UserAccountStatusValidator userAccountStatusValidator;

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
                .status(UserStatus.ACTIVE)
                .isEmailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        return new SignupResponseDto(savedUser);
    }

    @Transactional
    public IssuedTokens login(LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_CREDENTIALS
                        )
                );

        userAccountStatusValidator.validate(user);

        validatePassword(
                request.getPassword(),
                user.getPassword()
        );

        user.updateLastLoginAt();

        return authTokenService.issueTokens(user);
    }

    @Transactional
    public IssuedTokens reissue(
            String refreshToken
    ) {
        if (!jwtTokenProvider
                .validateRefreshToken(refreshToken)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REFRESH_TOKEN
            );
        }

        RefreshToken savedToken =
                refreshTokenRepository
                        .findByToken(refreshToken)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INVALID_REFRESH_TOKEN
                                )
                        );

        User user = savedToken.getUser();

        userAccountStatusValidator.validate(user);

        String newAccessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name()
                );

        String newRefreshToken =
                jwtTokenProvider.createRefreshToken(
                        user.getId()
                );

        savedToken.updateToken(
                newRefreshToken
        );

        return new IssuedTokens(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider
                        .getAccessTokenExpirationSeconds()
        );
    }



    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;

        refreshTokenRepository
                .findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
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