package com.bank.los.config;

/**
 * @deprecated Use {@link BankContext} instead.
 */
@Deprecated
public final class TenantContext {

    public static final String MASTER_TENANT_ID = BankContext.MASTER_BANK_ID;

    private TenantContext() {}

    public static void setCurrentTenant(String tenantDbName) {
        BankContext.setCurrentBank(tenantDbName);
    }

    public static String getCurrentTenant() {
        return BankContext.getCurrentBank();
    }

    public static void setCurrentOrgCode(String orgCode) {
        BankContext.setCurrentBankCode(orgCode);
    }

    public static String getCurrentOrgCode() {
        return BankContext.getCurrentBankCode();
    }

    public static void clear() {
        BankContext.clear();
    }
}

