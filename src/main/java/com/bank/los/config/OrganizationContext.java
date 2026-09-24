package com.bank.los.config;

import lombok.extern.slf4j.Slf4j;

/**
 * ThreadLocal context holding current bank/organization routing key and bank code.
 * (Delegates to BankContext)
 */
@Slf4j
public final class OrganizationContext {

    public static final String MASTER_ORG_ID = BankContext.MASTER_BANK_ID;
    public static final String MASTER_DB_NAME = BankContext.MASTER_DB_NAME;
    public static final String MASTER_TENANT_ID = BankContext.MASTER_TENANT_ID;

    private OrganizationContext() {}

    public static void setCurrentOrganization(String orgDbName) {
        BankContext.setCurrentBank(orgDbName);
    }

    public static void setCurrentOrg(String orgDbName) {
        BankContext.setCurrentBank(orgDbName);
    }

    public static String getCurrentOrganization() {
        return BankContext.getCurrentBank();
    }

    public static String getCurrentOrg() {
        return BankContext.getCurrentBank();
    }

    public static void setCurrentOrgCode(String orgCode) {
        BankContext.setCurrentBankCode(orgCode);
    }

    public static String getCurrentOrgCode() {
        return BankContext.getCurrentBankCode();
    }

    public static void setCurrentTenant(String tenantDbName) {
        BankContext.setCurrentBank(tenantDbName);
    }

    public static String getCurrentTenant() {
        return BankContext.getCurrentBank();
    }

    public static void clear() {
        BankContext.clear();
    }
}

