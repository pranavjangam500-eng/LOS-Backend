package com.bank.los.security;

import com.bank.los.config.TenantContext;
import com.bank.los.master.entity.InternalUser;
import com.bank.los.master.entity.LoginDirectory;
import com.bank.los.master.repository.InternalUserRepository;
import com.bank.los.master.repository.LoginDirectoryRepository;
import com.bank.los.tenant.entity.Customer;
import com.bank.los.tenant.entity.TenantUser;
import com.bank.los.tenant.repository.CustomerRepository;
import com.bank.los.tenant.repository.TenantUserRepository;
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
    private final TenantUserRepository tenantUserRepository;
    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Find routing in Master Login Directory
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
        LoginDirectory directory = loginDirectoryRepository.findByEmailOrUserCodeOrPhone(identifier, identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));

        String userType = directory.getUserType();
        String orgCode = directory.getOrganization() != null ? directory.getOrganization().getCode() : null;
        String tenantDb = directory.getOrganization() != null ? directory.getOrganization().getDbName() : null;

        if ("INTERNAL".equalsIgnoreCase(userType)) {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            InternalUser internalUser = internalUserRepository.findByEmail(identifier)
                    .or(() -> internalUserRepository.findByUserCode(identifier))
                    .orElseThrow(() -> new UsernameNotFoundException("Internal user record not found"));

            return UserPrincipal.builder()
                    .id(internalUser.getId())
                    .email(internalUser.getEmail())
                    .userCode(internalUser.getUserCode())
                    .fullName(internalUser.getFirstName() + " " + internalUser.getLastName())
                    .password(internalUser.getPasswordHash())
                    .role(internalUser.getRole().getName())
                    .userType("INTERNAL")
                    .active(Boolean.TRUE.equals(internalUser.getIsActive()))
                    .build();
        }

        // Route to Tenant Database
        TenantContext.setCurrentTenant(tenantDb);
        if ("STAFF".equalsIgnoreCase(userType)) {
            TenantUser staffUser = tenantUserRepository.findByEmail(identifier)
                    .or(() -> tenantUserRepository.findByUserCode(identifier))
                    .orElseThrow(() -> new UsernameNotFoundException("Staff user record not found in tenant database"));

            return UserPrincipal.builder()
                    .id(staffUser.getId())
                    .email(staffUser.getEmail())
                    .userCode(staffUser.getUserCode())
                    .fullName(staffUser.getFirstName() + " " + staffUser.getLastName())
                    .password(staffUser.getPasswordHash())
                    .role(staffUser.getRole().getName())
                    .userType("STAFF")
                    .organizationCode(orgCode)
                    .tenantDbName(tenantDb)
                    .branchId(staffUser.getBranch() != null ? staffUser.getBranch().getId() : null)
                    .active(Boolean.TRUE.equals(staffUser.getIsActive()))
                    .build();
        } else if ("CUSTOMER".equalsIgnoreCase(userType)) {
            Customer customer = customerRepository.findByEmail(identifier)
                    .or(() -> customerRepository.findByCustomerCode(identifier))
                    .orElseThrow(() -> new UsernameNotFoundException("Customer record not found in tenant database"));

            return UserPrincipal.builder()
                    .id(customer.getId())
                    .email(customer.getEmail())
                    .userCode(customer.getCustomerCode())
                    .fullName(customer.getFirstName() + " " + customer.getLastName())
                    .password(customer.getPasswordHash())
                    .role("CUSTOMER")
                    .userType("CUSTOMER")
                    .organizationCode(orgCode)
                    .tenantDbName(tenantDb)
                    .branchId(customer.getBranch() != null ? customer.getBranch().getId() : null)
                    .active(Boolean.TRUE.equals(customer.getIsActive()))
                    .build();
        }

        throw new UsernameNotFoundException("Unsupported user type: " + userType);
    }
}
