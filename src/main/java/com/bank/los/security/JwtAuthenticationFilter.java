package com.bank.los.security;

import com.bank.los.config.BankContext;
import com.bank.los.config.OrganizationContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TenantResolutionService tenantResolutionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                Claims claims = jwtTokenProvider.getClaimsFromToken(jwt);

                Long userId = claims.get(SecurityConstants.CLAIM_USER_ID, Number.class).longValue();
                String username = claims.getSubject();
                String userType = claims.get(SecurityConstants.CLAIM_USER_TYPE, String.class);
                String role = claims.get(SecurityConstants.CLAIM_ROLE, String.class);
                
                // Extract tenant identifiers from verified JWT
                Number orgIdNum = claims.get(SecurityConstants.CLAIM_ORG_ID, Number.class);
                if (orgIdNum == null) {
                    orgIdNum = claims.get(SecurityConstants.CLAIM_TENANT_ID, Number.class);
                }
                Long orgId = orgIdNum != null ? orgIdNum.longValue() : null;

                String orgUuidStr = claims.get(SecurityConstants.CLAIM_ORG_UUID, String.class);
                java.util.UUID orgUuid = orgUuidStr != null ? java.util.UUID.fromString(orgUuidStr) : null;

                String orgCode = claims.get(SecurityConstants.CLAIM_ORG_CODE, String.class);
                if (orgCode == null) {
                    orgCode = claims.get(SecurityConstants.CLAIM_BANK_CODE, String.class);
                }

                String fullName = claims.get(SecurityConstants.CLAIM_FULL_NAME, String.class);

                Number branchIdNum = claims.get(SecurityConstants.CLAIM_BRANCH_ID, Number.class);
                Long branchId = branchIdNum != null ? branchIdNum.longValue() : null;

                // SECURE RESOLUTION: Look up the Master DB to resolve org_id -> physical db_name.
                // NEVER trust client headers for database routing.
                String resolvedDb = tenantResolutionService.resolveTenantDb(orgId, orgCode, orgUuid, userType);

                // Configure dynamic multi-tenant routing context
                OrganizationContext.setCurrentOrganization(resolvedDb);
                BankContext.setCurrentBank(resolvedDb);

                if (StringUtils.hasText(orgCode)) {
                    OrganizationContext.setCurrentOrgCode(orgCode);
                    BankContext.setCurrentBankCode(orgCode);
                }

                String jti = claims.getId();
                if (jti == null) {
                    jti = claims.get(SecurityConstants.CLAIM_JTI, String.class);
                }

                String designation = claims.get(SecurityConstants.CLAIM_DESIGNATION, String.class);
                @SuppressWarnings("unchecked")
                java.util.List<String> permissions = claims.get(SecurityConstants.CLAIM_PERMISSIONS, java.util.List.class);

                UserPrincipal userPrincipal = UserPrincipal.builder()
                        .id(userId)
                        .email(username)
                        .userType(userType)
                        .role(role)
                        .designation(designation)
                        .permissions(permissions)
                        .organizationId(orgId)
                        .organizationUuid(orgUuid)
                        .organizationCode(orgCode)
                        .organizationDbName(resolvedDb)
                        .branchId(branchId)
                        .fullName(fullName)
                        .active(true)
                        .jti(jti)
                        .build();


                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // Default unauthenticated requests safely to Master DB
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
                BankContext.setCurrentBank(BankContext.MASTER_BANK_ID);
            }

            filterChain.doFilter(request, response);
        } finally {
            OrganizationContext.clear();
            BankContext.clear();
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(SecurityConstants.TOKEN_PREFIX.length());
        }
        return null;
    }
}
