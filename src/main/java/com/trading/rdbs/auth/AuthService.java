package com.trading.rdbs.auth;

import com.trading.rdbs.auth.domain.Role;
import com.trading.rdbs.auth.dto.JwtResponse;
import com.trading.rdbs.auth.dto.LoginRequest;
import com.trading.rdbs.auth.dto.RegisterRequest;
import com.trading.rdbs.auth.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 【職責】註冊與登入用例；登入成功簽發 OAuth Bearer JWT。
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AppUserService appUserService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       AppUserService appUserService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.appUserService = appUserService;
    }

    public void register(RegisterRequest request) {
        appUserService.register(request.username(), request.password(), Set.of(Role.USER));
    }

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        String token = tokenProvider.generateToken(authentication.getName(), roles);
        return JwtResponse.bearer(token, authentication.getName(), roles);
    }
}
