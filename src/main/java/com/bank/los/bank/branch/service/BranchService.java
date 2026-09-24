package com.bank.los.bank.branch.service;

import com.bank.los.bank.branch.dto.BranchResponse;
import com.bank.los.bank.branch.dto.CreateBranchRequest;
import com.bank.los.bank.master.entity.Branch;
import com.bank.los.bank.master.repository.BranchRepository;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    public List<BranchResponse> getAllBranches(UserPrincipal principal) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        return branchRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BranchResponse getBranchById(UserPrincipal principal, Long id) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        return mapToResponse(branch);
    }

    public BranchResponse createBranch(UserPrincipal principal, CreateBranchRequest request) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        if (branchRepository.existsByCode(request.getCode())) {
            throw new BusinessException("BRANCH_EXISTS", "Branch with code " + request.getCode() + " already exists");
        }

        Branch branch = Branch.builder()
                .name(request.getName())
                .code(request.getCode())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        Branch saved = branchRepository.save(branch);
        return mapToResponse(saved);
    }

    private BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .code(branch.getCode())
                .address(branch.getAddress())
                .city(branch.getCity())
                .state(branch.getState())
                .pincode(branch.getPincode())
                .status(branch.getStatus())
                .build();
    }
}
