package com.bank.los.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Should generate valid JWT access token and correctly extract claims")
    void testGenerateAndValidateToken() {
        UserPrincipal principal = UserPrincipal.builder()
                .id(42L)
                .email("test.maker@hdfcbank.com")
                .fullName("Test Maker")
                .role("MAKER")
                .userType("STAFF")
                .organizationId(1L)
                .organizationCode("HDFC01")
                .branchId(1L)
                .active(true)
                .build();

        String token = jwtTokenProvider.generateAccessToken(principal);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));

        Claims claims = jwtTokenProvider.getClaimsFromToken(token);
        assertEquals("test.maker@hdfcbank.com", claims.getSubject());
        assertEquals("MAKER", claims.get(SecurityConstants.CLAIM_ROLE));
        assertEquals("STAFF", claims.get(SecurityConstants.CLAIM_USER_TYPE));
        assertEquals("HDFC01", claims.get(SecurityConstants.CLAIM_ORG_CODE));
        assertEquals(1L, claims.get(SecurityConstants.CLAIM_ORG_ID, Number.class).longValue());
        assertEquals(1L, claims.get(SecurityConstants.CLAIM_TENANT_ID, Number.class).longValue());
        assertEquals(42L, claims.get(SecurityConstants.CLAIM_USER_ID, Number.class).longValue());
    }

    @Test
    @DisplayName("Should reject malformed JWT token")
    void testInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token.format"));
    }
}
