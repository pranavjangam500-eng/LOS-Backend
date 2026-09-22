package com.bank.los.common.constant;

public final class ApplicationConstants {

    private ApplicationConstants() {}

    public static final String API_V1_PREFIX = "/api/v1";

    /** Role names — must match identity.roles.name */
    public static final class Roles {
        public static final String INTERNAL_ADMIN = "INTERNAL_ADMIN"; // our platform team (master DB)
        public static final String ADMIN          = "ADMIN";           // bank/NBFC internal admin (tenant DB)
        public static final String CHECKER        = "CHECKER";
        public static final String MAKER          = "MAKER";
        public static final String VIEWER         = "VIEWER";
        public static final String CUSTOMER       = "CUSTOMER";
    }

    public static final class Panels {
        public static final String INTERNAL  = "INTERNAL";
        public static final String BANK_NBFC = "BANK_NBFC";
        public static final String CUSTOMER  = "CUSTOMER";
    }

    public static final class UserTypes {
        public static final String INTERNAL = "INTERNAL";
        public static final String STAFF    = "STAFF";
        public static final String CUSTOMER = "CUSTOMER";
    }

    public static final class OrgStatus {
        public static final String ACTIVE    = "ACTIVE";
        public static final String INACTIVE  = "INACTIVE";
        public static final String SUSPENDED = "SUSPENDED";
    }

    /** User status values matching the senior's design */
    public static final class UserStatus {
        public static final String PENDING_VERIFICATION = "PENDING_VERIFICATION";
        public static final String OPERATIVE            = "OPERATIVE";
        public static final String NON_OPERATIVE        = "NON_OPERATIVE";
    }

    /** Permission module groups */
    public static final class PermissionModules {
        public static final String USER        = "USER";
        public static final String BRANCH      = "BRANCH";
        public static final String LOAN        = "LOAN";
        public static final String CUSTOMER    = "CUSTOMER";
        public static final String DOCUMENT    = "DOCUMENT";
        public static final String REPORT      = "REPORT";
        public static final String DASHBOARD   = "DASHBOARD";
        public static final String SYSTEM      = "SYSTEM";
    }

    /** All permission codes seeded into identity.permissions */
    public static final class Permissions {
        // User management
        public static final String USER_CREATE         = "USER_CREATE";
        public static final String USER_UPDATE         = "USER_UPDATE";
        public static final String USER_VIEW           = "USER_VIEW";
        public static final String USER_DEACTIVATE     = "USER_DEACTIVATE";
        public static final String USER_VERIFY         = "USER_VERIFY";
        public static final String USER_RESET_PASSWORD = "USER_RESET_PASSWORD";

        // Branch
        public static final String BRANCH_CREATE = "BRANCH_CREATE";
        public static final String BRANCH_UPDATE = "BRANCH_UPDATE";
        public static final String BRANCH_VIEW   = "BRANCH_VIEW";

        // Loan application
        public static final String LOAN_APPLICATION_CREATE  = "LOAN_APPLICATION_CREATE";
        public static final String LOAN_APPLICATION_EDIT    = "LOAN_APPLICATION_EDIT";
        public static final String LOAN_APPLICATION_VIEW    = "LOAN_APPLICATION_VIEW";
        public static final String LOAN_APPLICATION_SUBMIT  = "LOAN_APPLICATION_SUBMIT";
        public static final String LOAN_APPLICATION_VERIFY  = "LOAN_APPLICATION_VERIFY";
        public static final String LOAN_APPLICATION_APPROVE = "LOAN_APPLICATION_APPROVE";
        public static final String LOAN_APPLICATION_REJECT  = "LOAN_APPLICATION_REJECT";

        // Customer
        public static final String CUSTOMER_CREATE   = "CUSTOMER_CREATE";
        public static final String CUSTOMER_EDIT     = "CUSTOMER_EDIT";
        public static final String CUSTOMER_VIEW     = "CUSTOMER_VIEW";
        public static final String CUSTOMER_VIEW_ALL = "CUSTOMER_VIEW_ALL";

        // Document
        public static final String DOCUMENT_UPLOAD = "DOCUMENT_UPLOAD";
        public static final String DOCUMENT_VERIFY = "DOCUMENT_VERIFY";
        public static final String DOCUMENT_VIEW   = "DOCUMENT_VIEW";

        // Reports
        public static final String REPORT_VIEW   = "REPORT_VIEW";
        public static final String REPORT_EXPORT = "REPORT_EXPORT";

        // Dashboard
        public static final String DASHBOARD_VIEW            = "DASHBOARD_VIEW";
        public static final String DASHBOARD_ANALYTICS_VIEW  = "DASHBOARD_ANALYTICS_VIEW";

        // Roles & Permissions (ADMIN only)
        public static final String ROLE_PERMISSION_MANAGE = "ROLE_PERMISSION_MANAGE";

        // System (INTERNAL only)
        public static final String ORGANIZATION_CREATE = "ORGANIZATION_CREATE";
        public static final String ORGANIZATION_VIEW   = "ORGANIZATION_VIEW";
        public static final String ORGANIZATION_UPDATE = "ORGANIZATION_UPDATE";
        public static final String SYSTEM_AUDIT_VIEW   = "SYSTEM_AUDIT_VIEW";
    }

    /** 2FA & OTP */
    public static final int OTP_EXPIRY_MINUTES         = 5;
    public static final int PASSWORD_RESET_EXPIRY_MINS = 15;
    public static final int MAX_RESET_REQUESTS_PER_HOUR = 3;
    public static final int MAX_BAD_LOGIN_ATTEMPTS      = 5;
}
