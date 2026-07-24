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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialLoginProcessor {

    private static final int NICKNAME_MAX_LENGTH = 50;
    private static final int NICKNAME_SUFFIX_LENGTH = 8;
    private static final int NICKNAME_GENERATION_MAX_ATTEMPTS = 10;

    /*
     * 닉네임 뒤에 "_" + 8자리 UUID 문자열을 붙이므로
     * 기본 닉네임은 최대 41자까지 허용합니다.
     */
    private static final int NICKNAME_BASE_MAX_LENGTH =
            NICKNAME_MAX_LENGTH
                    - 1
                    - NICKNAME_SUFFIX_LENGTH;

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final UserAccountStatusValidator userAccountStatusValidator;

    @Transactional
    public User process(
            SocialProvider provider,
            OAuth2UserInfo userInfo
    ) {
        validateBasicUserInfo(
                provider,
                userInfo
        );

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

    /**
     * 신규 소셜 사용자 생성
     *
     * 1. 인증된 이메일 검증
     * 2. 기존 동일 이메일 사용자 검사
     * 3. User 생성
     * 4. SocialAccount 생성
     */
    private User createSocialUser(
            SocialProvider provider,
            OAuth2UserInfo userInfo
    ) {
        String normalizedEmail =
                validateAndNormalizeEmail(userInfo);

        if (userRepository.existsByEmail(
                normalizedEmail
        )) {
            throw new BusinessException(
                    ErrorCode.SOCIAL_ACCOUNT_LINK_REQUIRED
            );
        }

        String name =
                resolveName(
                        provider,
                        userInfo.getName()
                );

        String nickname =
                createUniqueNickname(
                        provider,
                        userInfo.getNickname()
                );

        User user =
                User.builder()
                        .email(normalizedEmail)
                        .password(null)
                        .name(name)
                        .nickname(nickname)
                        .role(UserRole.USER)
                        .status(UserStatus.ACTIVE)
                        .isEmailVerified(true)
                        .profileImageUrl(
                                userInfo.getProfileImageUrl()
                        )
                        .build();

        try {
            User savedUser =
                    userRepository.saveAndFlush(user);

            SocialAccount socialAccount =
                    SocialAccount.builder()
                            .user(savedUser)
                            .provider(provider)
                            .providerId(
                                    userInfo.getProviderId()
                            )
                            .providerEmail(
                                    normalizedEmail
                            )
                            .profileImageUrl(
                                    userInfo.getProfileImageUrl()
                            )
                            .build();

            socialAccountRepository.saveAndFlush(
                    socialAccount
            );

            return savedUser;

        } catch (DataIntegrityViolationException exception) {
            /*
             * existsByEmail() 통과 직후 다른 요청이 같은 이메일이나
             * 같은 provider/providerId를 먼저 저장한 경쟁 상황을 처리합니다.
             */
            throw new BusinessException(
                    ErrorCode.SOCIAL_ACCOUNT_LINK_REQUIRED
            );
        }
    }

    /**
     * 기존 소셜 사용자 로그인 처리
     *
     * 이메일 필수 정책이므로 기존 사용자도 인증된 이메일을
     * 제공하지 않으면 로그인할 수 없습니다.
     */
    private User updateExistingUser(
            SocialAccount socialAccount,
            OAuth2UserInfo userInfo
    ) {
        User user =
                socialAccount.getUser();

        userAccountStatusValidator.validate(user);

        String normalizedEmail =
                validateAndNormalizeEmail(userInfo);

        validateEmailOwnership(
                user,
                normalizedEmail
        );

        try {
            /*
             * 과거에 email=null로 생성된 소셜 사용자의 이메일을
             * 현재 Provider에서 받은 인증된 이메일로 보완합니다.
             */
            if (user.getEmail() == null
                    || user.getEmail().isBlank()) {
                user.updateEmail(
                        normalizedEmail,
                        true
                );
            }

            socialAccount.updateLoginInfo(
                    normalizedEmail,
                    userInfo.getProfileImageUrl()
            );

            user.updateLastLoginAt();

            /*
             * 트랜잭션 종료 시점까지 기다리지 않고
             * 이메일 Unique 충돌을 이 위치에서 확인합니다.
             */
            userRepository.flush();

            return user;

        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    ErrorCode.SOCIAL_ACCOUNT_LINK_REQUIRED
            );
        }
    }

    /**
     * 기존 User가 Provider에서 받은 이메일을 사용할 수 있는지 검사합니다.
     */
    private void validateEmailOwnership(
            User user,
            String normalizedEmail
    ) {
        String currentEmail =
                normalizeEmailOrNull(
                        user.getEmail()
                );

        /*
         * 기존 User 이메일과 소셜 Provider 이메일이 서로 다르면
         * 자동으로 변경하거나 연결하지 않습니다.
         */
        if (currentEmail != null
                && !currentEmail.equals(
                normalizedEmail
        )) {
            throw new BusinessException(
                    ErrorCode.SOCIAL_ACCOUNT_LINK_REQUIRED
            );
        }

        /*
         * 현재 User를 제외한 다른 User가 해당 이메일을 사용 중인지 검사합니다.
         */
        if (userRepository.existsByEmailAndIdNot(
                normalizedEmail,
                user.getId()
        )) {
            throw new BusinessException(
                    ErrorCode.SOCIAL_ACCOUNT_LINK_REQUIRED
            );
        }
    }

    /**
     * Provider와 Provider ID처럼 모든 소셜 로그인에 필요한
     * 기본 사용자 정보를 검증합니다.
     */
    private void validateBasicUserInfo(
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

    /**
     * Google과 Kakao 모두 인증된 이메일을 필수로 요구합니다.
     */
    private String validateAndNormalizeEmail(
            OAuth2UserInfo userInfo
    ) {
        String normalizedEmail =
                normalizeEmailOrNull(
                        userInfo.getEmail()
                );

        if (normalizedEmail == null
                || !userInfo.isEmailVerified()) {
            throw new BusinessException(
                    ErrorCode.SOCIAL_EMAIL_REQUIRED
            );
        }

        return normalizedEmail;
    }

    private String normalizeEmailOrNull(
            String email
    ) {
        if (email == null
                || email.isBlank()) {
            return null;
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
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
        String baseNickname =
                resolveBaseNickname(
                        provider,
                        providerNickname
                );

        for (int attempt = 0;
             attempt < NICKNAME_GENERATION_MAX_ATTEMPTS;
             attempt++) {

            String suffix =
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(
                                    0,
                                    NICKNAME_SUFFIX_LENGTH
                            );

            String candidate =
                    truncateBaseNickname(
                            baseNickname
                    )
                            + "_"
                            + suffix;

            if (!userRepository.existsByNickname(
                    candidate
            )) {
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
            return sanitizeNickname(
                    providerNickname
            );
        }

        return switch (provider) {
            case GOOGLE -> "구글사용자";
            case KAKAO -> "카카오사용자";
        };
    }

    private String sanitizeNickname(
            String nickname
    ) {
        String sanitized =
                nickname
                        .trim()
                        .replaceAll(
                                "\\s+",
                                ""
                        )
                        .replaceAll(
                                "[^가-힣a-zA-Z0-9_]",
                                ""
                        );

        if (sanitized.isBlank()) {
            return "사용자";
        }

        return sanitized;
    }

    private String truncateBaseNickname(
            String value
    ) {
        if (value.length()
                <= NICKNAME_BASE_MAX_LENGTH) {
            return value;
        }

        return value.substring(
                0,
                NICKNAME_BASE_MAX_LENGTH
        );
    }
}