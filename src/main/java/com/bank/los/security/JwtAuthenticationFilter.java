package com.bank.los.security;

import com.bank.los.config.TenantContext;
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
                String orgCode = claims.get(SecurityConstants.CLAIM_ORG_CODE, String.class);
                String tenantDb = claims.get(SecurityConstants.CLAIM_TENANT_DB, String.class);
                String fullName = claims.get(SecurityConstants.CLAIM_FULL_NAME, String.class);

                Number branchIdNum = claims.get(SecurityConstants.CLAIM_BRANCH_ID, Number.class);
                Long branchId = branchIdNum != null ? branchIdNum.longValue() : null;

                // Configure dynamic multi-tenant context
                if (StringUtils.hasText(tenantDb)) {
                    TenantContext.setCurrentTenant(tenantDb);
                } else {
                    TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
                }

                if (StringUtils.hasText(orgCode)) {
                    TenantContext.setCurrentOrgCode(orgCode);
                }

                UserPrincipal userPrincipal = UserPrincipal.builder()
                        .id(userId)
                        .email(username)
                        .userType(userType)
                        .role(role)
                        .organizationCode(orgCode)
                        .tenantDbName(tenantDb)
                        .branchId(branchId)
                        .fullName(fullName)
                        .active(true)
                        .build();

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // If header provides X-Tenant-Code or for public requests, set default
                String tenantHeader = request.getHeader(SecurityConstants.TENANT_HEADER);
                if (StringUtils.hasText(tenantHeader)) {
                    TenantContext.setCurrentOrgCode(tenantHeader);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
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
