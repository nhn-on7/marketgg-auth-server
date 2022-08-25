package com.nhnacademy.marketgg.auth.service;

import com.nhnacademy.marketgg.auth.dto.response.login.oauth.TokenResponse;

/**
 * 인증 관련 비즈니스 로직을 처리하는 클래스입니다.
 *
 * @version 1.0.0
 */
public interface AuthService {

    /**
     * 로그아웃을 진행합니다.
     *
     * @param token - 로그아웃하려는 사용자의 JWT 입니다.
     * @author 윤동열
     * @since 1.0.0
     */
    void logout(final String token);

    /**
     * JWT 를 갱신합니다.
     *
     * @param token - 만료된 JWT 입니다.
     * @return 새로운 JWT 를 반환합니다.
     * @author 윤동열
     * @since 1.0.0
     */
    TokenResponse renewToken(final String token);

}
