package com.aha.global.security;

import com.aha.domain.user.entity.User;
import com.aha.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 일반 로그인 등 이메일 기반 인증에서 사용합니다.
     */
    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "사용자를 찾을 수 없습니다. email="
                                        + email
                        )
                );

        return new CustomUserDetails(user);
    }

    /**
     * JWT 인증에서 사용합니다.
     *
     * 일반 회원과 소셜 회원 모두 users.id가 존재하므로
     * 이메일보다 userId를 기준으로 조회하는 것이 안전합니다.
     */
    public CustomUserDetails loadUserById(
            Long userId
    ) throws UsernameNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "사용자를 찾을 수 없습니다. userId="
                                        + userId
                        )
                );

        return new CustomUserDetails(user);
    }
}