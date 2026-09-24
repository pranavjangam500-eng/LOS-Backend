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
                String bankCode = claims.get(SecurityConstants.CLAIM_BANK_CODE, String.class);
                if (bankCode == null) {
                    bankCode = claims.get(SecurityConstants.CLAIM_ORG_CODE, String.class);
                }
                String bankDb = claims.get(SecurityConstants.CLAIM_BANK_DB, String.class);
                if (bankDb == null) {
                    bankDb = claims.get(SecurityConstants.CLAIM_ORG_DB, String.class);
                }
                if (bankDb == null) {
                    bankDb = claims.get(SecurityConstants.CLAIM_TENANT_DB, String.class);
                }
                String fullName = claims.get(SecurityConstants.CLAIM_FULL_NAME, String.class);

                Number branchIdNum = claims.get(SecurityConstants.CLAIM_BRANCH_ID, Number.class);
                Long branchId = branchIdNum != null ? branchIdNum.longValue() : null;

                // Configure dynamic multi-bank routing context
                if (StringUtils.hasText(bankDb)) {
                    BankContext.setCurrentBank(bankDb);
                } else {
                    BankContext.setCurrentBank(BankContext.MASTER_BANK_ID);
                }

                if (StringUtils.hasText(bankCode)) {
                    BankContext.setCurrentBankCode(bankCode);
                }

                UserPrincipal userPrincipal = UserPrincipal.builder()
                        .id(userId)
                        .email(username)
                        .userType(userType)
                        .role(role)
                        .organizationCode(bankCode)
                        .organizationDbName(bankDb)
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
                // If header provides X-Bank-Code, X-Organization-Code or X-Tenant-Code
                String bankHeader = request.getHeader(SecurityConstants.BANK_HEADER);
                if (!StringUtils.hasText(bankHeader)) {
                    bankHeader = request.getHeader(SecurityConstants.ORGANIZATION_HEADER);
                }
                if (!StringUtils.hasText(bankHeader)) {
                    bankHeader = request.getHeader(SecurityConstants.TENANT_HEADER);
                }
                if (StringUtils.hasText(bankHeader)) {
                    BankContext.setCurrentBankCode(bankHeader);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
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
