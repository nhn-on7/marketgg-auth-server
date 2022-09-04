package com.nhnacademy.marketgg.auth.jwt;

import static java.util.stream.Collectors.toList;

import com.nhnacademy.marketgg.auth.entity.Role;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * UserDetailsService 에서 반환하는 UserDetails 를 상속받은 사용자 정보를 담고있는 클래스 입니다.
 */
@AllArgsConstructor
public class CustomUser implements UserDetails {

    private final String uuid;
    private final String password;
    private final transient List<Role> authorities; // 직렬화 될 일이 없는 클래스입니다. SonarQube 위해 transient 추가
    private final boolean isWithdraw;

    /**
     * 권한 정보를 반환합니다.
     *
     * @return 사용자의 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream()
                          .map(r -> new SimpleGrantedAuthority(r.getName().name()))
                          .collect(toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return uuid;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isWithdraw() {
        return this.isWithdraw;
    }


}
