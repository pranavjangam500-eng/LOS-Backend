package com.bank.los.common.constant;

public final class ApplicationConstants {

    private ApplicationConstants() {}

    public static final String API_V1_PREFIX = "/api/v1";

    public static final class Roles {
        public static final String INTERNAL_ADMIN = "INTERNAL_ADMIN";
        public static final String SUPER_ADMIN = "SUPER_ADMIN";
        public static final String CHECKER = "CHECKER";
        public static final String MAKER = "MAKER";
        public static final String VIEWER = "VIEWER";
        public static final String CUSTOMER = "CUSTOMER";
    }

    public static final class Panels {
        public static final String INTERNAL = "INTERNAL";
        public static final String BANK_NBFC = "BANK_NBFC";
        public static final String CUSTOMER = "CUSTOMER";
    }

    public static final class UserTypes {
        public static final String INTERNAL = "INTERNAL";
        public static final String STAFF = "STAFF";
        public static final String CUSTOMER = "CUSTOMER";
    }

    public static final class OrgStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String INACTIVE = "INACTIVE";
        public static final String SUSPENDED = "SUSPENDED";
    }
}
