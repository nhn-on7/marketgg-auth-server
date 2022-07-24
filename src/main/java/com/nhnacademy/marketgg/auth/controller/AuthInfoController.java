package com.nhnacademy.marketgg.auth.controller;

import com.nhnacademy.marketgg.auth.dto.request.AuthUpdateRequest;
import com.nhnacademy.marketgg.auth.dto.request.AuthWithDrawRequest;
import com.nhnacademy.marketgg.auth.dto.response.AuthUpdateResponse;
import com.nhnacademy.marketgg.auth.dto.response.TokenResponse;
import com.nhnacademy.marketgg.auth.exception.UnAuthorizationException;
import com.nhnacademy.marketgg.auth.jwt.TokenUtils;
import com.nhnacademy.marketgg.auth.service.AuthInfoService;
import com.nhnacademy.marketgg.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/info")
public class AuthInfoController {

    private final AuthInfoService authInfoService;

    private final AuthService authService;

    /**
     * 회원정보 수정을 위한 컨트롤러 메서드 입니다.
     *
     * @param authUpdateRequest - 수정할 회원 정보를 담고있는 객체 입니다.
     * @return - 상태코드를 리턴합니다.
     */
    @PutMapping
    public ResponseEntity<Void> update(@RequestBody final AuthUpdateRequest authUpdateRequest
            , HttpServletRequest httpServletRequest) throws UnAuthorizationException {

        String token = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        TokenResponse update
                = authInfoService.update(token, authUpdateRequest);

        authService.logout(token);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(update.getJwt());
        httpHeaders.set(TokenUtils.JWT_EXPIRE, update.getExpiredDate().toString());

        return ResponseEntity.status(OK)
                             .headers(httpHeaders)
                             .build();
    }

    /**
     * 회원정보 삭제를 위한 컨트롤러 메서드 입니다.
     *
     * @param httpServletRequest - Header 를 가지고 있는 객체 입니다. 해당 객체를 통해 uuid 를 추출해서 soft delete 를 할 예정입니다.
     * @return - AuthWithdrawResponse Auth 서버와 Shop 서버간의 통신에서 오는 시간 격차를 줄이기 위한 객체 입니다.
     */

    @DeleteMapping
    public ResponseEntity<AuthUpdateResponse> withdraw(@RequestBody final AuthWithDrawRequest authWithDrawRequest
            , HttpServletRequest httpServletRequest) throws UnAuthorizationException {

        authInfoService.withdraw(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)
                , authWithDrawRequest);
        return ResponseEntity.status(OK)
                             .build();
    }

}
