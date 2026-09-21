package com.bank.los.common.validation;

import com.bank.los.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Enterprise Password Complexity Policy Enforcement Utility.
 */
public final class PasswordPolicy {

    private PasswordPolicy() {}

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    public static void validate(String password) {
        if (!StringUtils.hasText(password)) {
            throw new BusinessException("WEAK_PASSWORD", "Password cannot be empty");
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new BusinessException("WEAK_PASSWORD", "Password must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters long");
        }

        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            throw new BusinessException("WEAK_PASSWORD", "Password must contain at least one uppercase letter (A-Z)");
        }

        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            throw new BusinessException("WEAK_PASSWORD", "Password must contain at least one lowercase letter (a-z)");
        }

        if (!DIGIT_PATTERN.matcher(password).matches()) {
            throw new BusinessException("WEAK_PASSWORD", "Password must contain at least one digit (0-9)");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            throw new BusinessException("WEAK_PASSWORD", "Password must contain at least one special character (e.g. !@#$%^&*)");
        }
    }
}
