package com.bank.los.auth.mapper;

import com.bank.los.auth.dto.response.UserProfileResponse;
import com.bank.los.common.util.StringUtil;
import com.bank.los.master.entity.InternalUser;
import com.bank.los.master.entity.Organization;
import com.bank.los.tenant.entity.Customer;
import com.bank.los.tenant.entity.TenantUser;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public UserProfileResponse toProfileResponse(InternalUser user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .userCode(user.getUserCode())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(StringUtil.getFullName(user.getFirstName(), user.getMiddleName(), user.getLastName()))
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().getName() : "INTERNAL_ADMIN")
                .userType("INTERNAL")
                .organizationName("LOS Master Platform")
                .organizationCode("MASTER")
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    public UserProfileResponse toProfileResponse(TenantUser user, Organization org) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .userCode(user.getUserCode())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(StringUtil.getFullName(user.getFirstName(), user.getMiddleName(), user.getLastName()))
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .userType("STAFF")
                .organizationName(org != null ? org.getName() : null)
                .organizationCode(org != null ? org.getCode() : null)
                .organizationType(org != null ? org.getType() : null)
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .branchCode(user.getBranch() != null ? user.getBranch().getCode() : null)
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    public UserProfileResponse toProfileResponse(Customer customer, Organization org) {
        return UserProfileResponse.builder()
                .id(customer.getId())
                .userCode(customer.getCustomerCode())
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .fullName(StringUtil.getFullName(customer.getFirstName(), customer.getMiddleName(), customer.getLastName()))
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .role("CUSTOMER")
                .userType("CUSTOMER")
                .organizationName(org != null ? org.getName() : null)
                .organizationCode(org != null ? org.getCode() : null)
                .organizationType(org != null ? org.getType() : null)
                .branchId(customer.getBranch() != null ? customer.getBranch().getId() : null)
                .branchName(customer.getBranch() != null ? customer.getBranch().getName() : null)
                .branchCode(customer.getBranch() != null ? customer.getBranch().getCode() : null)
                .lastLoginAt(customer.getLastLoginAt())
                .build();
    }
}
