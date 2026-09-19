package com.bank.los.config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_ORG_CODE = new ThreadLocal<>();

    public static final String MASTER_TENANT_ID = "master";

    private TenantContext() {}

    public static void setCurrentTenant(String tenantDbName) {
        log.debug("Setting current tenant DB to: {}", tenantDbName);
        CURRENT_TENANT.set(tenantDbName);
    }

    public static String getCurrentTenant() {
        String tenant = CURRENT_TENANT.get();
        return tenant != null ? tenant : MASTER_TENANT_ID;
    }

    public static void setCurrentOrgCode(String orgCode) {
        CURRENT_ORG_CODE.set(orgCode);
    }

    public static String getCurrentOrgCode() {
        return CURRENT_ORG_CODE.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_ORG_CODE.remove();
    }
}
