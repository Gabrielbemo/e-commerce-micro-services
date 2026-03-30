package com.gabriel.gatewayserver.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void shouldBuildSecurityWebFilterChain() {
        ServerHttpSecurity serverHttpSecurity = ServerHttpSecurity.http();
        ReactiveJwtDecoder jwtDecoder = mock(ReactiveJwtDecoder.class);
        serverHttpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder)));

        SecurityWebFilterChain chain = securityConfig.securityWebFilterChain(serverHttpSecurity);

        assertThat(chain).isNotNull();
    }
}
