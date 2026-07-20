package com.aha.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthCodeExchangeRequestDto(

        @NotBlank(message = "OAuth 인증 코드는 필수입니다.")
        String code
) {
}