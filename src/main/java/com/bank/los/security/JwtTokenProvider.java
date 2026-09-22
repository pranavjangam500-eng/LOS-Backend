package com.bank.los.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms:86400000}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Value("${app.jwt.issuer:LOS-Platform}")
    private String issuer;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        String jti = userPrincipal.getJti() != null
                ? userPrincipal.getJti()
                : java.util.UUID.randomUUID().toString();

        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.CLAIM_JTI,       jti);
        claims.put(SecurityConstants.CLAIM_USER_ID,   userPrincipal.getId());
        claims.put(SecurityConstants.CLAIM_USER_TYPE, userPrincipal.getUserType());
        claims.put(SecurityConstants.CLAIM_ROLE,      userPrincipal.getRole());
        claims.put(SecurityConstants.CLAIM_ORG_CODE,  userPrincipal.getOrganizationCode());
        claims.put(SecurityConstants.CLAIM_TENANT_DB, userPrincipal.getTenantDbName());
        claims.put(SecurityConstants.CLAIM_BRANCH_ID, userPrincipal.getBranchId());
        claims.put(SecurityConstants.CLAIM_FULL_NAME, userPrincipal.getFullName());

        return Jwts.builder()
                .id(jti)
                .subject(userPrincipal.getUsername())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .claims(claims)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Short-lived (5-minute) JWT used as a temporary session token during the 2FA OTP step.
     * Carries enough claims for OTP verification to identify the user and their tenant.
     */
    public String generateTempSessionToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 5 * 60 * 1000L); // 5 minutes

        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.CLAIM_USER_ID,   userPrincipal.getId());
        claims.put(SecurityConstants.CLAIM_USER_TYPE, userPrincipal.getUserType());
        claims.put(SecurityConstants.CLAIM_TENANT_DB, userPrincipal.getTenantDbName());
        claims.put(SecurityConstants.CLAIM_ORG_CODE,  userPrincipal.getOrganizationCode());
        claims.put("temp", true); // marks this as a temp/challenge token, not a full auth token

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .claims(claims)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", java.util.UUID.randomUUID().toString());
        claims.put(SecurityConstants.CLAIM_USER_ID, userPrincipal.getId());
        claims.put(SecurityConstants.CLAIM_USER_TYPE, userPrincipal.getUserType());
        claims.put(SecurityConstants.CLAIM_ORG_CODE, userPrincipal.getOrganizationCode());
        claims.put(SecurityConstants.CLAIM_TENANT_DB, userPrincipal.getTenantDbName());

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .claims(claims)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}
