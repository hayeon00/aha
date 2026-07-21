package com.aha.domain.auth.oauth2.service;

import com.aha.domain.auth.entity.SocialAccount;
import com.aha.domain.auth.enums.SocialProvider;
import com.aha.domain.auth.oauth2.info.OAuth2UserInfo;
import com.aha.domain.auth.repository.SocialAccountRepository;
import com.aha.domain.auth.service.UserAccountStatusValidator;
import com.aha.domain.user.entity.User;
import com.aha.domain.user.enums.UserRole;
import com.aha.domain.user.enums.UserStatus;
import com.aha.domain.user.repository.UserRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/***
 *
 * process()가 User를 반환
 * 기존 SocialAccount가 있으면 기존 User 반환
 * 기존 SocialAccount가 없으면 신규 User 생성
 * 신규 SocialAccount 생성
 * 닉네임 중복 방지
 */


@Service
@RequiredArgsConstructor
public class SocialLoginProcessor {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final UserAccountStatusValidator userAccountStatusValidator;

    @Transactional
    public User process(
            SocialProvider provider,
            OAuth2UserInfo userInfo
    ) {
        validateUserInfo(provider, userInfo);

        return socialAccountRepository
                .findByProviderAndProviderId(
                        provider,
                        userInfo.getProviderId()
                )
                .map(socialAccount ->
                        updateExistingUser(
                                socialAccount,
                                userInfo
                        )
                )
                .orElseGet(() ->
                        createSocialUser(
                                provider,
                                userInfo
                        )
                );
    }

    private User createSocialUser(
            SocialProvider provider,
            OAuth2UserInfo userInfo
    ) {
        String name = resolveName(
                provider,
                userInfo.getName()
        );

        String nickname = createUniqueNickname(
                provider,
                userInfo.getNickname()
        );

        User user = User.builder()
                .email(normalizeEmail(userInfo.getEmail()))
                .password(null)
                .name(name)
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .isEmailVerified(false)
                .profileImageUrl(userInfo.getProfileImageUrl())
                .build();

        User savedUser = userRepository.save(user);

        SocialAccount socialAccount =
                SocialAccount.builder()
                        .user(savedUser)
                        .provider(provider)
                        .providerId(userInfo.getProviderId())
                        .providerEmail(userInfo.getEmail())
                        .profileImageUrl(
                                userInfo.getProfileImageUrl()
                        )
                        .build();

        socialAccountRepository.save(socialAccount);

        return savedUser;
    }

    private String resolveName(
            SocialProvider provider,
            String providerName
    ) {
        if (providerName != null
                && !providerName.isBlank()) {
            return providerName.trim();
        }

        return switch (provider) {
            case GOOGLE -> "구글 사용자";
            case KAKAO -> "카카오 사용자";
        };
    }

    private String createUniqueNickname(
            SocialProvider provider,
            String providerNickname
    ) {
        String baseNickname = resolveBaseNickname(
                provider,
                providerNickname
        );

        for (int attempt = 0; attempt < 10; attempt++) {
            String suffix = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8);

            String candidate =
                    truncate(baseNickname, 41)
                            + "_"
                            + suffix;

            if (!userRepository.existsByNickname(candidate)) {
                return candidate;
            }
        }

        throw new BusinessException(
                ErrorCode.NICKNAME_GENERATION_FAILED
        );
    }

    private String resolveBaseNickname(
            SocialProvider provider,
            String providerNickname
    ) {
        if (providerNickname != null
                && !providerNickname.isBlank()) {
            return sanitizeNickname(providerNickname);
        }

        return switch (provider) {
            case GOOGLE -> "구글사용자";
            case KAKAO -> "카카오사용자";
        };
    }

    private String sanitizeNickname(String nickname) {
        String sanitized = nickname
                .trim()
                .replaceAll("\\s+", "")
                .replaceAll(
                        "[^가-힣a-zA-Z0-9_]",
                        ""
                );

        if (sanitized.isBlank()) {
            return "사용자";
        }

        return sanitized;
    }

    private String truncate(
            String value,
            int maxLength
    ) {
        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email
                .trim()
                .toLowerCase();
    }

    private User updateExistingUser(
            SocialAccount socialAccount,
            OAuth2UserInfo userInfo
    ) {
        User user = socialAccount.getUser();

        userAccountStatusValidator.validate(user);

        socialAccount.updateLoginInfo(
                userInfo.getEmail(),
                userInfo.getProfileImageUrl()
        );

        user.updateLastLoginAt();

        return user;
    }

    private void validateUserInfo(
            SocialProvider provider,
            OAuth2UserInfo userInfo
    ) {
        if (provider == null) {
            throw new BusinessException(
                    ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER
            );
        }

        if (userInfo == null
                || userInfo.getProviderId() == null
                || userInfo.getProviderId().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_SOCIAL_USER_INFO
            );
        }
    }
}