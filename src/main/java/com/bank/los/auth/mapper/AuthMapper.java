package com.bank.los.auth.mapper;

import com.bank.los.auth.dto.response.UserProfileResponse;
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
                .empNo(user.getEmpNo())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole() != null ? user.getRole().getName() : "INTERNAL_ADMIN")
                .userType("INTERNAL")
                .status(user.getStatus())
                .organizationName("LOS Master Platform")
                .organizationCode("MASTER")
                .designation(user.getDesignation())
                .lastLoginDate(user.getLastLoginDate())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }

    public UserProfileResponse toProfileResponse(TenantUser user, Organization org) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .empNo(user.getEmpNo())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .userType("STAFF")
                .status(user.getStatus())
                .organizationName(org != null ? org.getName() : null)
                .organizationCode(org != null ? org.getCode() : null)
                .organizationType(org != null ? org.getType() : null)
                .branchId(user.getLoginBranch() != null ? user.getLoginBranch().getId() : null)
                .branchName(user.getLoginBranch() != null ? user.getLoginBranch().getName() : null)
                .branchCode(user.getLoginBranch() != null ? user.getLoginBranch().getCode() : null)
                .designation(user.getDesignation())
                .multiBranchAccess(user.getMultiBranchAccess())
                .inactiveSessionTimeout(user.getInactiveSessionTimeout())
                .lastLoginDate(user.getLastLoginDate())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }

    public UserProfileResponse toProfileResponse(Customer customer, Organization org) {
        return UserProfileResponse.builder()
                .id(customer.getId())
                .empNo(customer.getCustomerCode())
                .username(customer.getEmail())
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .fullName(customer.getFirstName() + " " + customer.getLastName())
                .email(customer.getEmail())
                .mobile(customer.getPhone())
                .role("CUSTOMER")
                .userType("CUSTOMER")
                .organizationName(org != null ? org.getName() : null)
                .organizationCode(org != null ? org.getCode() : null)
                .organizationType(org != null ? org.getType() : null)
                .branchId(customer.getBranch() != null ? customer.getBranch().getId() : null)
                .branchName(customer.getBranch() != null ? customer.getBranch().getName() : null)
                .branchCode(customer.getBranch() != null ? customer.getBranch().getCode() : null)
                .build();
    }
}
