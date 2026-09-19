package com.bank.los.customer.service;

import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.TenantContext;
import com.bank.los.customer.dto.request.CreateCustomerRequest;
import com.bank.los.customer.dto.request.UpdateCustomerRequest;
import com.bank.los.customer.dto.response.CustomerResponse;
import com.bank.los.customer.mapper.CustomerMapper;
import com.bank.los.customer.validator.CustomerValidator;
import com.bank.los.master.entity.LoginDirectory;
import com.bank.los.master.entity.Organization;
import com.bank.los.master.repository.LoginDirectoryRepository;
import com.bank.los.master.repository.OrganizationRepository;
import com.bank.los.security.UserPrincipal;
import com.bank.los.tenant.entity.Branch;
import com.bank.los.tenant.entity.Customer;
import com.bank.los.tenant.repository.BranchRepository;
import com.bank.los.tenant.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final CustomerMapper customerMapper;
    private final CustomerValidator customerValidator;
    private final PasswordEncoder passwordEncoder;
    private final LoginDirectoryRepository loginDirectoryRepository;
    private final OrganizationRepository organizationRepository;

    public List<CustomerResponse> getAllCustomers(UserPrincipal principal) {
        TenantContext.setCurrentTenant(principal.getTenantDbName());
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CustomerResponse getCustomerById(UserPrincipal principal, Long id) {
        TenantContext.setCurrentTenant(principal.getTenantDbName());
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(UserPrincipal principal, CreateCustomerRequest request) {
        String tenantDb = principal.getTenantDbName();
        String orgCode = principal.getOrganizationCode();

        customerValidator.validate(request);
        TenantContext.setCurrentTenant(tenantDb);

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("CUSTOMER_EMAIL_EXISTS", "Customer with email " + request.getEmail() + " already exists");
        }

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId()).orElse(null);
        }

        String passwordHash = null;
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            passwordHash = passwordEncoder.encode(request.getPassword());
        }

        Customer customer = Customer.builder()
                .customerCode(request.getCustomerCode())
                .branch(branch)
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordHash)
                .isActive(true)
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        // Register in Master Login Directory so customer can log into the Customer Portal
        try {
            TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);
            Organization org = organizationRepository.findByCode(orgCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "code", orgCode));

            LoginDirectory loginDirectory = LoginDirectory.builder()
                    .userCode(savedCustomer.getCustomerCode())
                    .email(savedCustomer.getEmail())
                    .phone(savedCustomer.getPhone())
                    .organization(org)
                    .userType("CUSTOMER")
                    .build();

            loginDirectoryRepository.save(loginDirectory);
        } finally {
            TenantContext.setCurrentTenant(tenantDb);
        }

        return customerMapper.toResponse(savedCustomer);
    }

    @Transactional
    public CustomerResponse updateCustomer(UserPrincipal principal, Long id, UpdateCustomerRequest request) {
        TenantContext.setCurrentTenant(principal.getTenantDbName());

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customer.setFirstName(request.getFirstName());
        customer.setMiddleName(request.getMiddleName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());

        Customer updated = customerRepository.save(customer);
        return customerMapper.toResponse(updated);
    }
}
