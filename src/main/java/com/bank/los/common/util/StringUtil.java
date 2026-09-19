package com.bank.los.common.util;

public final class StringUtil {

    private StringUtil() {}

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String getFullName(String firstName, String middleName, String lastName) {
        StringBuilder sb = new StringBuilder();
        if (isNotBlank(firstName)) {
            sb.append(firstName.trim());
        }
        if (isNotBlank(middleName)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(middleName.trim());
        }
        if (isNotBlank(lastName)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName.trim());
        }
        return sb.toString();
    }
}
