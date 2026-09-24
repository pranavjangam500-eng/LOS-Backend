package com.bank.los.bank.customer.service;

import com.bank.los.administration.master.entity.LoginDirectory;
import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.LoginDirectoryRepository;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.bank.customer.dto.request.CreateCustomerRequest;
import com.bank.los.bank.customer.dto.request.UpdateCustomerRequest;
import com.bank.los.bank.customer.dto.response.CustomerResponse;
import com.bank.los.bank.customer.mapper.CustomerMapper;
import com.bank.los.bank.customer.validator.CustomerValidator;
import com.bank.los.bank.master.entity.Branch;
import com.bank.los.bank.master.entity.Customer;
import com.bank.los.bank.master.repository.BranchRepository;
import com.bank.los.bank.master.repository.CustomerRepository;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.common.response.PageResponse;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final LoginDirectoryRepository loginDirectoryRepository;
    private final OrganizationRepository organizationRepository;
    private final CustomerMapper customerMapper;
    private final CustomerValidator customerValidator;
    private final PasswordEncoder passwordEncoder;

    public PageResponse<CustomerResponse> getCustomers(UserPrincipal principal, Pageable pageable) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        Page<Customer> page = customerRepository.findAll(pageable);
        return PageResponse.of(page.map(customerMapper::toResponse));
    }

    public CustomerResponse getCustomerById(UserPrincipal principal, Long id) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(UserPrincipal principal, CreateCustomerRequest request) {
        customerValidator.validateCreate(request);

        String orgDb = principal.getOrganizationDbName();
        String orgCode = principal.getOrganizationCode();

        OrganizationContext.setCurrentOrganization(orgDb);

        if (customerRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessException("EMAIL_EXISTS", "Customer with email " + request.getEmail() + " already exists");
        }
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("PHONE_EXISTS", "Customer with phone " + request.getPhone() + " already exists");
        }

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));
        } else {
            branch = branchRepository.findAll().stream().findFirst().orElse(null);
        }

        String customerCode = "CUST-" + orgCode + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Customer customer = Customer.builder()
                .customerCode(customerCode)
                .branch(branch)
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone())
                .passwordHash(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : null)
                .isActive(true)
                .build();

        Customer saved = customerRepository.save(customer);

        // Register in Master Login Directory for customer portal login routing
        try {
            OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
            Organization org = organizationRepository.findByCode(orgCode).orElse(null);
            if (org != null) {
                loginDirectoryRepository.save(LoginDirectory.builder()
                        .userCode(saved.getCustomerCode())
                        .email(saved.getEmail())
                        .phone(saved.getPhone())
                        .organization(org)
                        .userType(ApplicationConstants.UserTypes.CUSTOMER)
                        .build());
            }
        } finally {
            OrganizationContext.setCurrentOrganization(orgDb);
        }

        log.info("Customer registered: code={}, org={}", saved.getCustomerCode(), orgCode);
        return customerMapper.toResponse(saved);
    }

    @Transactional
    public CustomerResponse updateCustomer(UserPrincipal principal, Long id, UpdateCustomerRequest request) {
        customerValidator.validateUpdate(request);
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (request.getFirstName() != null) customer.setFirstName(request.getFirstName());
        if (request.getMiddleName() != null) customer.setMiddleName(request.getMiddleName());
        if (request.getLastName() != null) customer.setLastName(request.getLastName());
        if (request.getEmail() != null) customer.setEmail(request.getEmail().trim().toLowerCase());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getIsActive() != null) customer.setIsActive(request.getIsActive());

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));
            customer.setBranch(branch);
        }

        customer.setUpdatedAt(LocalDateTime.now());
        Customer updated = customerRepository.save(customer);
        return customerMapper.toResponse(updated);
    }
}
