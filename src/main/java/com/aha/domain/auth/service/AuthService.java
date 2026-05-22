package com.aha.domain.auth.service;

import com.aha.domain.auth.dto.request.LoginRequestDto;
import com.aha.domain.auth.dto.request.SignupRequestDto;
import com.aha.domain.auth.dto.response.LoginResponseDto;
import com.aha.domain.auth.dto.response.SignupResponseDto;
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

    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {
        validateDuplicateEmail(request.getEmail());
        validateDuplicateNickname(request.getNickname());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
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

        return new LoginResponseDto(
                accessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds()
        );
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