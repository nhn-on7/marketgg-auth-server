package com.nhnacademy.marketgg.auth.service.impl;

import com.nhnacademy.marketgg.auth.dto.request.AuthUpdateRequest;
import com.nhnacademy.marketgg.auth.dto.request.AuthWithDrawRequest;
import com.nhnacademy.marketgg.auth.dto.response.MemberInfoResponse;
import com.nhnacademy.marketgg.auth.dto.response.MemberNameResponse;
import com.nhnacademy.marketgg.auth.dto.response.MemberResponse;
import com.nhnacademy.marketgg.auth.dto.response.TokenResponse;
import com.nhnacademy.marketgg.auth.entity.Auth;
import com.nhnacademy.marketgg.auth.exception.AuthNotFoundException;
import com.nhnacademy.marketgg.auth.jwt.TokenUtils;
import com.nhnacademy.marketgg.auth.repository.AuthRepository;
import com.nhnacademy.marketgg.auth.repository.RoleRepository;
import com.nhnacademy.marketgg.auth.service.AuthInfoService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 정보에 대한 비즈니스 로직을 처리하는 기본 구현체입니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DefaultAuthInfoService implements AuthInfoService {

    private final AuthRepository authRepository;
    private final TokenUtils tokenUtils;
    private final RoleRepository roleRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * {@inheritDoc}
     *
     * @author 윤동열
     */
    @Override
    public MemberResponse findAuthByUuid(final String token) {
        String uuid = tokenUtils.getUuidFromToken(token);
        Auth auth = authRepository.findByUuid(uuid)
                                  .orElseThrow(AuthNotFoundException::new);

        return new MemberResponse(auth.getEmail(), auth.getName(), auth.getPhoneNumber());
    }

    /**
     * {@inheritDoc}
     *
     * @author 윤동열
     */
    @Override
    public MemberInfoResponse findMemberInfoByUuid(final String uuid) {
        Auth auth = authRepository.findByUuid(uuid)
                                  .orElseThrow(AuthNotFoundException::new);

        return new MemberInfoResponse(auth.getName(), auth.getEmail());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MemberNameResponse> findMemberNameList(List<String> uuids) {
        return authRepository.findMembersByUuid(uuids);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public TokenResponse update(final String token, final AuthUpdateRequest authUpdateRequest) {

        String uuid = tokenUtils.getUuidFromToken(token);
        Auth updatedAuth = authRepository.findByUuid(uuid)
                                         .orElseThrow(AuthNotFoundException::new);

        updatedAuth.updateAuth(authUpdateRequest);
        redisTemplate.opsForHash()
                     .delete(updatedAuth.getUuid(), TokenUtils.REFRESH_TOKEN);
        List<SimpleGrantedAuthority> roles = roleRepository.findRolesByAuthId(updatedAuth.getId())
                                                           .stream()
                                                           .map(r -> new SimpleGrantedAuthority(
                                                               r.getName().name()))
                                                           .collect(
                                                               Collectors.toUnmodifiableList());

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(updatedAuth.getUuid(), "", roles);

        return tokenUtils.saveRefreshToken(redisTemplate, auth);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void withdraw(String token, final AuthWithDrawRequest authWithDrawRequest) {

        String uuid = tokenUtils.getUuidFromToken(token);

        Auth deletedAuth = authRepository.findByUuid(uuid)
                                         .orElseThrow(AuthNotFoundException::new);

        deletedAuth.deleteAuth(authWithDrawRequest);
    }

}
