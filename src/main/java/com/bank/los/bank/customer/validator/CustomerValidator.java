package com.bank.los.bank.customer.validator;

import com.bank.los.bank.customer.dto.request.CreateCustomerRequest;
import com.bank.los.bank.customer.dto.request.UpdateCustomerRequest;
import com.bank.los.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CustomerValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");

    public void validateCreate(CreateCustomerRequest request) {
        if (request.getPhone() != null && !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new BusinessException("INVALID_PHONE", "Phone number must be a valid international format (10-15 digits)");
        }
    }

    public void validateUpdate(UpdateCustomerRequest request) {
        if (request.getPhone() != null && !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new BusinessException("INVALID_PHONE", "Phone number must be a valid international format (10-15 digits)");
        }
    }
}
