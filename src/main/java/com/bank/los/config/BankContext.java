package com.bank.los.config;

import lombok.extern.slf4j.Slf4j;

/**
 * ThreadLocal context holding current bank database routing key and bank code.
 */
@Slf4j
public final class BankContext {

    private static final ThreadLocal<String> CURRENT_BANK_DB = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_BANK_CODE = new ThreadLocal<>();

    public static final String MASTER_BANK_ID = "master";
    public static final String MASTER_DB_NAME = MASTER_BANK_ID;
    public static final String MASTER_ORG_ID = MASTER_BANK_ID;
    public static final String MASTER_TENANT_ID = MASTER_BANK_ID;

    private BankContext() {}

    public static void setCurrentBank(String bankDbName) {
        log.debug("Setting current bank DB to: {}", bankDbName);
        CURRENT_BANK_DB.set(bankDbName);
    }

    public static String getCurrentBank() {
        String bankDb = CURRENT_BANK_DB.get();
        return bankDb != null ? bankDb : MASTER_BANK_ID;
    }

    public static void setCurrentBankCode(String bankCode) {
        CURRENT_BANK_CODE.set(bankCode);
    }

    public static String getCurrentBankCode() {
        return CURRENT_BANK_CODE.get();
    }

    // Organization & Tenant aliases for compatibility
    public static void setCurrentOrganization(String orgDbName) {
        setCurrentBank(orgDbName);
    }

    public static String getCurrentOrganization() {
        return getCurrentBank();
    }

    public static void setCurrentOrg(String orgDbName) {
        setCurrentBank(orgDbName);
    }

    public static String getCurrentOrg() {
        return getCurrentBank();
    }

    public static void setCurrentOrgCode(String orgCode) {
        setCurrentBankCode(orgCode);
    }

    public static String getCurrentOrgCode() {
        return getCurrentBankCode();
    }

    public static void setCurrentTenant(String tenantDbName) {
        setCurrentBank(tenantDbName);
    }

    public static String getCurrentTenant() {
        return getCurrentBank();
    }

    public static void clear() {
        CURRENT_BANK_DB.remove();
        CURRENT_BANK_CODE.remove();
    }
}
