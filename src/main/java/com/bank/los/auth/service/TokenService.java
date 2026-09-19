package com.bank.los.auth.service;

import com.bank.los.auth.dto.response.TokenResponse;
import com.bank.los.common.exception.UnauthorizedException;
import com.bank.los.config.TenantContext;
import com.bank.los.security.JwtTokenProvider;
import com.bank.los.security.UserPrincipal;
import com.bank.los.tenant.entity.RefreshToken;
import com.bank.los.tenant.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken createRefreshToken(UserPrincipal userPrincipal) {
        if (userPrincipal.getTenantDbName() != null) {
            TenantContext.setCurrentTenant(userPrincipal.getTenantDbName());
        } else {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
        }

        String tokenString = jwtTokenProvider.generateRefreshToken(userPrincipal);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userPrincipal.getId())
                .token(tokenString)
                .userType(userPrincipal.getUserType())
                .organizationCode(userPrincipal.getOrganizationCode())
                .expiryDate(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs()))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public TokenResponse refreshToken(String requestRefreshToken) {
        if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        Claims claims = jwtTokenProvider.getClaimsFromToken(requestRefreshToken);
        Long userId = claims.get("userId", Number.class).longValue();
        String userType = claims.get("userType", String.class);
        String orgCode = claims.get("orgCode", String.class);
        String tenantDb = claims.get("tenantDb", String.class);
        String username = claims.getSubject();

        RefreshToken storedToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found or revoked"));

        if (Boolean.TRUE.equals(storedToken.getRevoked()) || storedToken.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired or been revoked");
        }

        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(userId)
                .email(username)
                .userType(userType)
                .organizationCode(orgCode)
                .tenantDbName(tenantDb)
                .active(true)
                .build();

        String newAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);

        // Update stored refresh token
        storedToken.setToken(newRefreshToken);
        storedToken.setExpiryDate(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs()));
        refreshTokenRepository.save(storedToken);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                .build();
    }
}
