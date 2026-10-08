package com.bank.los.bank.auth.service;

import com.bank.los.administration.master.entity.InternalUser;
import com.bank.los.administration.master.entity.LoginDirectory;
import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.InternalUserRepository;
import com.bank.los.administration.master.repository.LoginDirectoryRepository;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.bank.auth.dto.response.TokenResponse;
import com.bank.los.bank.master.entity.Customer;
import com.bank.los.bank.master.entity.OrganizationUser;
import com.bank.los.bank.master.entity.RefreshToken;
import com.bank.los.bank.master.repository.CustomerRepository;
import com.bank.los.bank.master.repository.OrganizationUserRepository;
import com.bank.los.bank.master.repository.RefreshTokenRepository;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.common.exception.UnauthorizedException;
import com.bank.los.config.BankContext;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.JwtTokenProvider;
import com.bank.los.security.SecurityConstants;
import com.bank.los.security.TenantResolutionService;
import com.bank.los.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final InternalUserRepository internalUserRepository;
    private final OrganizationUserRepository organizationUserRepository;
    private final CustomerRepository customerRepository;
    private final LoginDirectoryRepository loginDirectoryRepository;
    private final OrganizationRepository organizationRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TenantResolutionService tenantResolutionService;

    @Transactional
    public RefreshToken createRefreshToken(UserPrincipal principal) {
        String orgDb = principal.getOrganizationDbName();
        if (orgDb == null) {
            orgDb = OrganizationContext.MASTER_ORG_ID;
        }

        OrganizationContext.setCurrentOrganization(orgDb);

        // Revoke prior refresh tokens for this user
        try {
            refreshTokenRepository.revokeAllForUser(principal.getId(), principal.getUserType());
        } catch (Exception ex) {
            log.debug("Notice revoking old tokens: {}", ex.getMessage());
        }

        String rawToken = jwtTokenProvider.generateRefreshToken(principal);

        RefreshToken token = RefreshToken.builder()
                .userId(principal.getId())
                .token(rawToken)
                .userType(principal.getUserType())
                .organizationCode(principal.getOrganizationCode())
                .expiryDate(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpirationMs() / 1000))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }

    public TokenResponse refreshToken(String rawRefreshToken) {
        if (!jwtTokenProvider.validateToken(rawRefreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        Claims claims = jwtTokenProvider.getClaimsFromToken(rawRefreshToken);
        Long userId = claims.get(SecurityConstants.CLAIM_USER_ID, Number.class).longValue();
        String userType = claims.get(SecurityConstants.CLAIM_USER_TYPE, String.class);
        
        Number orgIdNum = claims.get(SecurityConstants.CLAIM_ORG_ID, Number.class);
        if (orgIdNum == null) {
            orgIdNum = claims.get(SecurityConstants.CLAIM_TENANT_ID, Number.class);
        }
        Long orgId = orgIdNum != null ? orgIdNum.longValue() : null;

        String orgCode = claims.get(SecurityConstants.CLAIM_ORG_CODE, String.class);
        if (orgCode == null) {
            orgCode = claims.get(SecurityConstants.CLAIM_BANK_CODE, String.class);
        }

        String resolvedDb = tenantResolutionService.resolveTenantDb(orgId, orgCode, null, userType);
        OrganizationContext.setCurrentOrganization(resolvedDb);
        BankContext.setCurrentBank(resolvedDb);

        RefreshToken storedToken = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token record not found"));

        if (Boolean.TRUE.equals(storedToken.getRevoked())) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        UserPrincipal principal = rebuildPrincipal(userId, userType, orgId, orgCode, resolvedDb);
        String newAccessToken = jwtTokenProvider.generateAccessToken(principal);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(principal);

        storedToken.setToken(newRefreshToken);
        storedToken.setExpiryDate(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpirationMs() / 1000));
        refreshTokenRepository.save(storedToken);

        return TokenResponse.builder()
                .organizationId(principal.getOrganizationId())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
                .build();
    }

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        if (jwtTokenProvider.validateToken(rawRefreshToken)) {
            Claims claims = jwtTokenProvider.getClaimsFromToken(rawRefreshToken);
            Number orgIdNum = claims.get(SecurityConstants.CLAIM_ORG_ID, Number.class);
            if (orgIdNum == null) {
                orgIdNum = claims.get(SecurityConstants.CLAIM_TENANT_ID, Number.class);
            }
            Long orgId = orgIdNum != null ? orgIdNum.longValue() : null;
            String orgCode = claims.get(SecurityConstants.CLAIM_ORG_CODE, String.class);
            String userType = claims.get(SecurityConstants.CLAIM_USER_TYPE, String.class);

            String resolvedDb = tenantResolutionService.resolveTenantDb(orgId, orgCode, null, userType);
            OrganizationContext.setCurrentOrganization(resolvedDb);
            BankContext.setCurrentBank(resolvedDb);
        }
        refreshTokenRepository.revokeByToken(rawRefreshToken);
    }

    private UserPrincipal rebuildPrincipal(Long userId, String userType, Long orgId, String orgCode, String orgDb) {
        if (ApplicationConstants.UserTypes.INTERNAL.equalsIgnoreCase(userType)) {
            OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
            InternalUser u = internalUserRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
            return UserPrincipal.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .userCode(u.getEmpNo())
                    .fullName(u.getFullName())
                    .role(u.getRole().getName())
                    .userType("INTERNAL")
                    .organizationId(0L)
                    .organizationCode("MASTER")
                    .organizationDbName(OrganizationContext.MASTER_ORG_ID)
                    .active(true)
                    .build();
        } else if (ApplicationConstants.UserTypes.STAFF.equalsIgnoreCase(userType)) {
            OrganizationContext.setCurrentOrganization(orgDb);
            OrganizationUser u = organizationUserRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
            return UserPrincipal.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .userCode(u.getEmpNo())
                    .fullName(u.getFullName())
                    .role(u.getRole().getName())
                    .userType("STAFF")
                    .organizationId(orgId)
                    .organizationCode(orgCode)
                    .organizationDbName(orgDb)
                    .branchId(u.getLoginBranch() != null ? u.getLoginBranch().getId() : null)
                    .active(true)
                    .build();
        } else {
            OrganizationContext.setCurrentOrganization(orgDb);
            Customer c = customerRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("Customer not found"));
            return UserPrincipal.builder()
                    .id(c.getId())
                    .email(c.getEmail())
                    .userCode(c.getCustomerCode())
                    .fullName(c.getFirstName() + " " + c.getLastName())
                    .role("CUSTOMER")
                    .userType("CUSTOMER")
                    .organizationId(orgId)
                    .organizationCode(orgCode)
                    .organizationDbName(orgDb)
                    .branchId(c.getBranch() != null ? c.getBranch().getId() : null)
                    .active(true)
                    .build();
        }
    }
}
