package com.bank.los.bank.auth.mapper;

import com.bank.los.administration.master.entity.InternalUser;
import com.bank.los.administration.master.entity.Organization;
import com.bank.los.bank.auth.dto.response.UserProfileResponse;
import com.bank.los.bank.master.entity.Customer;
import com.bank.los.bank.master.entity.OrganizationUser;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public UserProfileResponse toProfileResponse(InternalUser user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .empNo(user.getEmpNo())
                .userCode(user.getEmpNo())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .gender(user.getGender())
                .dob(user.getDob())
                .designation(user.getDesignation())
                .userType("INTERNAL")
                .role(user.getRole() != null ? user.getRole().getName() : "INTERNAL_ADMIN")
                .status(user.getStatus())
                .twoFaEnabled(user.getTwoFaEnabled())
                .multiBranchAccess(false)
                .organizationCode("MASTER")
                .organizationName("LOS Master Platform")
                .loginOnHolidays(user.getLoginOnHolidays())
                .loginTime(user.getLoginTime())
                .logoutTime(user.getLogoutTime())
                .inactiveSessionTimeout(user.getInactiveSessionTimeout())
                .lastLoginDate(user.getLastLoginDate())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }

    public UserProfileResponse toProfileResponse(OrganizationUser user, Organization org) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .empNo(user.getEmpNo())
                .userCode(user.getEmpNo())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .gender(user.getGender())
                .dob(user.getDob())
                .designation(user.getDesignation())
                .userType("STAFF")
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .status(user.getStatus())
                .twoFaEnabled(user.getTwoFaEnabled())
                .multiBranchAccess(user.getMultiBranchAccess())
                .branchId(user.getLoginBranch() != null ? user.getLoginBranch().getId() : null)
                .branchName(user.getLoginBranch() != null ? user.getLoginBranch().getName() : null)
                .organizationCode(org != null ? org.getCode() : null)
                .organizationName(org != null ? org.getName() : null)
                .loginOnHolidays(user.getLoginOnHolidays())
                .loginTime(user.getLoginTime())
                .logoutTime(user.getLogoutTime())
                .inactiveSessionTimeout(user.getInactiveSessionTimeout())
                .lastLoginDate(user.getLastLoginDate())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }

    public UserProfileResponse toProfileResponse(Customer customer, Organization org) {
        return UserProfileResponse.builder()
                .id(customer.getId())
                .userCode(customer.getCustomerCode())
                .username(customer.getCustomerCode())
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .fullName(customer.getFirstName() + " " + customer.getLastName())
                .email(customer.getEmail())
                .mobile(customer.getPhone())
                .userType("CUSTOMER")
                .role("CUSTOMER")
                .status(Boolean.TRUE.equals(customer.getIsActive()) ? "OPERATIVE" : "INACTIVE")
                .twoFaEnabled(false)
                .multiBranchAccess(false)
                .branchId(customer.getBranch() != null ? customer.getBranch().getId() : null)
                .branchName(customer.getBranch() != null ? customer.getBranch().getName() : null)
                .organizationCode(org != null ? org.getCode() : null)
                .organizationName(org != null ? org.getName() : null)
                .loginOnHolidays(true)
                .inactiveSessionTimeout(3600)
                .build();
    }
}
