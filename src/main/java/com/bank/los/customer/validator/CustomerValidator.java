package com.bank.los.customer.validator;

import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.util.StringUtil;
import com.bank.los.customer.dto.request.CreateCustomerRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomerValidator {

    public void validate(CreateCustomerRequest request) {
        if (StringUtil.isBlank(request.getEmail()) && StringUtil.isBlank(request.getPhone())) {
            throw new BusinessException("VALIDATION_ERROR", "Either email or phone number is mandatory for customer creation");
        }
    }
}
