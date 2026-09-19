package com.bank.los.security;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TENANT_HEADER = "X-Tenant-Code";

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USER_TYPE = "userType";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_ORG_CODE = "orgCode";
    public static final String CLAIM_TENANT_DB = "tenantDb";
    public static final String CLAIM_BRANCH_ID = "branchId";
    public static final String CLAIM_FULL_NAME = "fullName";

    public static final String[] PUBLIC_URLS = {
            "/api/v1/auth/login",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/health",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };
}
