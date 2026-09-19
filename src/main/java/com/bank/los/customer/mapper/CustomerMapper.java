package com.bank.los.customer.mapper;

import com.bank.los.common.util.StringUtil;
import com.bank.los.customer.dto.response.CustomerResponse;
import com.bank.los.tenant.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) return null;

        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .branchId(customer.getBranch() != null ? customer.getBranch().getId() : null)
                .branchName(customer.getBranch() != null ? customer.getBranch().getName() : null)
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .fullName(StringUtil.getFullName(customer.getFirstName(), customer.getMiddleName(), customer.getLastName()))
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .isActive(customer.getIsActive())
                .lastLoginAt(customer.getLastLoginAt())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
