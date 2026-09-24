package com.bank.los.security;

import com.bank.los.administration.master.entity.InternalUser;
import com.bank.los.administration.master.entity.LoginDirectory;
import com.bank.los.administration.master.repository.InternalUserRepository;
import com.bank.los.administration.master.repository.LoginDirectoryRepository;
import com.bank.los.bank.master.entity.Customer;
import com.bank.los.bank.master.entity.OrganizationUser;
import com.bank.los.bank.master.repository.CustomerRepository;
import com.bank.los.bank.master.repository.OrganizationUserRepository;
import com.bank.los.config.OrganizationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final LoginDirectoryRepository loginDirectoryRepository;
    private final InternalUserRepository internalUserRepository;
    private final OrganizationUserRepository organizationUserRepository;
    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Find routing in Master Login Directory
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        LoginDirectory directory = loginDirectoryRepository.findByEmailOrUserCodeOrPhone(identifier, identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));

        String userType = directory.getUserType();
        String orgCode = directory.getOrganization() != null ? directory.getOrganization().getCode() : null;
        String orgDb = directory.getOrganization() != null ? directory.getOrganization().getDbName() : null;

        if ("INTERNAL".equalsIgnoreCase(userType)) {
            OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
            InternalUser internalUser = internalUserRepository.findByEmail(identifier)
                    .or(() -> internalUserRepository.findByUsername(identifier))
                    .or(() -> internalUserRepository.findByEmpNo(identifier))
                    .orElseThrow(() -> new UsernameNotFoundException("Internal user record not found"));

            return UserPrincipal.builder()
                    .id(internalUser.getId())
                    .email(internalUser.getEmail())
                    .userCode(internalUser.getEmpNo() != null ? internalUser.getEmpNo() : internalUser.getUsername())
                    .fullName(internalUser.getFullName())
                    .password(internalUser.getPasswordHash())
                    .role(internalUser.getRole().getName())
                    .userType("INTERNAL")
                    .active(Boolean.TRUE.equals(internalUser.getIsActive()))
                    .build();
        }

        // Route to Organization Database
        OrganizationContext.setCurrentOrganization(orgDb);
        if ("STAFF".equalsIgnoreCase(userType)) {
            OrganizationUser staffUser = organizationUserRepository.findByEmail(identifier)
                    .or(() -> organizationUserRepository.findByUsername(identifier))
                    .or(() -> organizationUserRepository.findByEmpNo(identifier))
                    .orElseThrow(() -> new UsernameNotFoundException("Staff user record not found in organization database"));

            return UserPrincipal.builder()
                    .id(staffUser.getId())
                    .email(staffUser.getEmail())
                    .userCode(staffUser.getEmpNo() != null ? staffUser.getEmpNo() : staffUser.getUsername())
                    .fullName(staffUser.getFullName())
                    .password(staffUser.getPasswordHash())
                    .role(staffUser.getRole().getName())
                    .userType("STAFF")
                    .organizationCode(orgCode)
                    .organizationDbName(orgDb)
                    .branchId(staffUser.getLoginBranch() != null ? staffUser.getLoginBranch().getId() : null)
                    .active(Boolean.TRUE.equals(staffUser.getIsActive()))
                    .build();
        } else if ("CUSTOMER".equalsIgnoreCase(userType)) {
            Customer customer = customerRepository.findByEmail(identifier)
                    .or(() -> customerRepository.findByCustomerCode(identifier))
                    .orElseThrow(() -> new UsernameNotFoundException("Customer record not found in organization database"));

            return UserPrincipal.builder()
                    .id(customer.getId())
                    .email(customer.getEmail())
                    .userCode(customer.getCustomerCode())
                    .fullName(customer.getFirstName() + (customer.getMiddleName() != null ? " " + customer.getMiddleName() : "") + " " + customer.getLastName())
                    .password(customer.getPasswordHash())
                    .role("CUSTOMER")
                    .userType("CUSTOMER")
                    .organizationCode(orgCode)
                    .organizationDbName(orgDb)
                    .branchId(customer.getBranch() != null ? customer.getBranch().getId() : null)
                    .active(Boolean.TRUE.equals(customer.getIsActive()))
                    .build();
        }

        throw new UsernameNotFoundException("Unsupported user type: " + userType);
    }
}
