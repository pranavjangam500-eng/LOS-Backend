package com.bank.los.bank.customer.mapper;

import com.bank.los.bank.customer.dto.response.CustomerResponse;
import com.bank.los.bank.master.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) return null;

        String fullName = (customer.getMiddleName() != null && !customer.getMiddleName().isBlank())
                ? customer.getFirstName() + " " + customer.getMiddleName() + " " + customer.getLastName()
                : customer.getFirstName() + " " + customer.getLastName();

        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .branchId(customer.getBranch() != null ? customer.getBranch().getId() : null)
                .branchName(customer.getBranch() != null ? customer.getBranch().getName() : null)
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .fullName(fullName)
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .isActive(customer.getIsActive())
                .lastLoginAt(customer.getLastLoginAt())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
