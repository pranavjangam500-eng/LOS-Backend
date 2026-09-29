package com.bank.los.administration.lookup.service;

import com.bank.los.administration.audit.service.AdminAuditService;
import com.bank.los.administration.lookup.dto.*;
import com.bank.los.administration.master.entity.MasterLookupSubType;
import com.bank.los.administration.master.entity.MasterLookupType;
import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.MasterLookupSubTypeRepository;
import com.bank.los.administration.master.repository.MasterLookupTypeRepository;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.bank.master.entity.BankLookupSubType;
import com.bank.los.bank.master.entity.BankLookupType;
import com.bank.los.bank.master.repository.BankLookupSubTypeRepository;
import com.bank.los.bank.master.repository.BankLookupTypeRepository;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.BankContext;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdministrationLookupService {

    private final MasterLookupTypeRepository masterLookupTypeRepository;
    private final MasterLookupSubTypeRepository masterLookupSubTypeRepository;
    private final OrganizationRepository organizationRepository;
    private final BankLookupTypeRepository bankLookupTypeRepository;
    private final BankLookupSubTypeRepository bankLookupSubTypeRepository;
    private final AdminAuditService adminAuditService;

    @Transactional(readOnly = true)
    public List<LookupTypeResponse> getAllLookupTypes() {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        return masterLookupTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LookupTypeResponse getLookupTypeByCode(String code) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        MasterLookupType type = masterLookupTypeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + code));
        return mapToResponse(type);
    }

    @Transactional
    public LookupTypeResponse createLookupType(CreateLookupTypeRequest request, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        if (masterLookupTypeRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Lookup type already exists with code: " + request.getCode());
        }

        MasterLookupType lookupType = MasterLookupType.builder()
                .code(request.getCode().trim())
                .description(request.getDescription().trim())
                .isFixed(Boolean.TRUE.equals(request.getIsFixed()))
                .isActive(request.getIsActive() == null || request.getIsActive())
                .createdBy(principal != null ? principal.getId() : null)
                .build();

        MasterLookupType saved = masterLookupTypeRepository.save(lookupType);
        log.info("Created new Master Lookup Type code={} by admin={}", saved.getCode(), principal != null ? principal.getEmail() : "SYSTEM");
        return mapToResponse(saved);
    }

    @Transactional
    public LookupTypeResponse updateLookupType(String code, UpdateLookupTypeRequest request, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupType type = masterLookupTypeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + code));

        type.setDescription(request.getDescription().trim());
        if (request.getIsFixed() != null) {
            type.setIsFixed(request.getIsFixed());
        }
        if (request.getIsActive() != null) {
            type.setIsActive(request.getIsActive());
        }
        type.setModifiedBy(principal != null ? principal.getId() : null);

        MasterLookupType saved = masterLookupTypeRepository.save(type);
        log.info("Updated Master Lookup Type code={} by admin={}", saved.getCode(), principal != null ? principal.getEmail() : "SYSTEM");
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteLookupType(String code) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupType type = masterLookupTypeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + code));

        if (Boolean.TRUE.equals(type.getIsFixed())) {
            throw new BusinessException("Cannot delete system fixed master lookup type: " + code);
        }

        masterLookupSubTypeRepository.deleteByLookupTypeCode(code);
        masterLookupTypeRepository.delete(type);
        log.info("Deleted Master Lookup Type code={}", code);
    }

    @Transactional
    public LookupSubTypeResponse addSubType(String lookupTypeCode, CreateLookupSubTypeRequest request, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupType type = masterLookupTypeRepository.findByCode(lookupTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + lookupTypeCode));

        if (masterLookupSubTypeRepository.existsByLookupTypeCodeAndSubTypeCode(lookupTypeCode, request.getSubTypeCode())) {
            throw new BusinessException("Sub-type code '" + request.getSubTypeCode() + "' already exists under lookup type " + lookupTypeCode);
        }

        MasterLookupSubType subType = MasterLookupSubType.builder()
                .lookupTypeCode(lookupTypeCode)
                .typeDescription(type.getDescription())
                .subTypeCode(request.getSubTypeCode().trim())
                .subTypeDescription(request.getSubTypeDescription().trim())
                .isFixed(Boolean.TRUE.equals(request.getIsFixed()))
                .isActive(request.getIsActive() == null || request.getIsActive())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .createdBy(principal != null ? principal.getId() : null)
                .build();

        MasterLookupSubType saved = masterLookupSubTypeRepository.save(subType);
        log.info("Added SubType code={} to Master Lookup Type={} by admin={}", saved.getSubTypeCode(), lookupTypeCode, principal != null ? principal.getEmail() : "SYSTEM");
        return mapSubTypeToResponse(saved);
    }

    @Transactional
    public LookupSubTypeResponse updateSubType(Long id, UpdateLookupSubTypeRequest request, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupSubType subType = masterLookupSubTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup sub-type not found with id: " + id));

        subType.setSubTypeDescription(request.getSubTypeDescription().trim());
        if (request.getIsFixed() != null) {
            subType.setIsFixed(request.getIsFixed());
        }
        if (request.getIsActive() != null) {
            subType.setIsActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            subType.setDisplayOrder(request.getDisplayOrder());
        }
        subType.setModifiedBy(principal != null ? principal.getId() : null);

        MasterLookupSubType saved = masterLookupSubTypeRepository.save(subType);
        return mapSubTypeToResponse(saved);
    }

    @Transactional
    public void deleteSubType(Long id) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupSubType subType = masterLookupSubTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup sub-type not found with id: " + id));

        masterLookupSubTypeRepository.delete(subType);
        log.info("Deleted Master Lookup SubType id={}", id);
    }

    // =========================================================================
    //  BANK VISIBILITY & SYNC FOR SYSTEM ADMINISTRATOR
    // =========================================================================

    public List<LookupTypeResponse> getBankLookups(String bankCode) {
        Organization org = organizationRepository.findByCode(bankCode)
                .orElseThrow(() -> new ResourceNotFoundException("Bank / Organization not found with code: " + bankCode));

        String previousBank = BankContext.getCurrentBank();
        String previousOrg = OrganizationContext.getCurrentOrganization();
        try {
            BankContext.setCurrentBank(org.getDbName());
            OrganizationContext.setCurrentOrganization(org.getDbName());

            return bankLookupTypeRepository.findAll().stream()
                    .map(this::mapBankTypeToResponse)
                    .collect(Collectors.toList());
        } finally {
            BankContext.setCurrentBank(previousBank != null ? previousBank : BankContext.MASTER_DB_NAME);
            OrganizationContext.setCurrentOrganization(previousOrg != null ? previousOrg : OrganizationContext.MASTER_ORG_ID);
        }
    }

    public void syncMasterLookupsToBank(String bankCode) {
        Organization org = organizationRepository.findByCode(bankCode)
                .orElseThrow(() -> new ResourceNotFoundException("Bank / Organization not found with code: " + bankCode));

        // Read master lookups first
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        List<MasterLookupType> masterTypes = masterLookupTypeRepository.findAll();
        List<MasterLookupSubType> masterSubTypes = masterLookupSubTypeRepository.findAll();

        String previousBank = BankContext.getCurrentBank();
        String previousOrg = OrganizationContext.getCurrentOrganization();
        try {
            BankContext.setCurrentBank(org.getDbName());
            OrganizationContext.setCurrentOrganization(org.getDbName());

            for (MasterLookupType mt : masterTypes) {
                BankLookupType bt = bankLookupTypeRepository.findByCode(mt.getCode())
                        .orElseGet(() -> bankLookupTypeRepository.save(BankLookupType.builder()
                                .code(mt.getCode())
                                .description(mt.getDescription())
                                .isFixed(mt.getIsFixed())
                                .isActive(mt.getIsActive())
                                .build()));

                bt.setDescription(mt.getDescription());
                bt.setIsFixed(mt.getIsFixed());
                bt.setIsActive(mt.getIsActive());
                bankLookupTypeRepository.save(bt);
            }

            for (MasterLookupSubType mst : masterSubTypes) {
                BankLookupSubType bst = bankLookupSubTypeRepository.findByLookupTypeCodeAndSubTypeCode(
                                mst.getLookupTypeCode(), mst.getSubTypeCode())
                        .orElseGet(() -> bankLookupSubTypeRepository.save(BankLookupSubType.builder()
                                .lookupTypeCode(mst.getLookupTypeCode())
                                .typeDescription(mst.getTypeDescription())
                                .subTypeCode(mst.getSubTypeCode())
                                .subTypeDescription(mst.getSubTypeDescription())
                                .isFixed(mst.getIsFixed())
                                .isActive(mst.getIsActive())
                                .displayOrder(mst.getDisplayOrder())
                                .build()));

                // Sync fixed master status
                if (Boolean.TRUE.equals(mst.getIsFixed())) {
                    bst.setTypeDescription(mst.getTypeDescription());
                    bst.setSubTypeDescription(mst.getSubTypeDescription());
                    bst.setIsFixed(true);
                    bst.setDisplayOrder(mst.getDisplayOrder());
                    bankLookupSubTypeRepository.save(bst);
                }
            }

            log.info("Master lookups synchronized successfully to bank db={}", org.getDbName());
        } finally {
            BankContext.setCurrentBank(previousBank != null ? previousBank : BankContext.MASTER_DB_NAME);
            OrganizationContext.setCurrentOrganization(previousOrg != null ? previousOrg : OrganizationContext.MASTER_ORG_ID);
        }
    }

    public void syncMasterLookupsToAllBanks() {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        List<Organization> orgs = organizationRepository.findAll();
        for (Organization org : orgs) {
            try {
                syncMasterLookupsToBank(org.getInstitutionCode());
            } catch (Exception e) {
                log.warn("Failed to sync lookups to org {}: {}", org.getInstitutionCode(), e.getMessage());
            }
        }
    }

    @Transactional
    public LookupTypeResponse updateBankLookupCapabilities(
            String bankCode,
            String lookupTypeCode,
            UpdateBankLookupCapabilitiesRequest request,
            UserPrincipal principal) {
        Organization org = organizationRepository.findByCode(bankCode)
                .orElseThrow(() -> new ResourceNotFoundException("Bank / Organization not found with code: " + bankCode));

        String previousBank = BankContext.getCurrentBank();
        String previousOrg = OrganizationContext.getCurrentOrganization();
        try {
            BankContext.setCurrentBank(org.getDbName());
            OrganizationContext.setCurrentOrganization(org.getDbName());

            BankLookupType bt = bankLookupTypeRepository.findByCode(lookupTypeCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Lookup type not found in bank database with code: " + lookupTypeCode));

            if (request.getCanView() != null) bt.setCanView(request.getCanView());
            if (request.getCanAdd() != null) bt.setCanAdd(request.getCanAdd());
            if (request.getCanImportFromMaster() != null) bt.setCanImportFromMaster(request.getCanImportFromMaster());
            if (request.getCanEdit() != null) bt.setCanEdit(request.getCanEdit());
            if (request.getCanDelete() != null) bt.setCanDelete(request.getCanDelete());
            if (request.getCanActivate() != null) bt.setCanActivate(request.getCanActivate());
            if (request.getCanDeactivate() != null) bt.setCanDeactivate(request.getCanDeactivate());

            bt.setModifiedBy(principal != null ? principal.getId() : null);
            BankLookupType saved = bankLookupTypeRepository.save(bt);

            if (principal != null) {
                adminAuditService.logAdminAction(
                        principal.getId(), principal.getEmail(), "UPDATE_BANK_LOOKUP_CAPABILITIES",
                        "LOOKUP", bankCode,
                        "Updated capabilities for lookup " + lookupTypeCode + " in bank " + bankCode,
                        null
                );
            }

            log.info("Admin updated capabilities for lookup {} in bank {}", lookupTypeCode, bankCode);
            return mapBankTypeToResponse(saved);
        } finally {
            BankContext.setCurrentBank(previousBank != null ? previousBank : BankContext.MASTER_DB_NAME);
            OrganizationContext.setCurrentOrganization(previousOrg != null ? previousOrg : OrganizationContext.MASTER_ORG_ID);
        }
    }

    // =========================================================================
    //  MAPPERS
    // =========================================================================

    private LookupTypeResponse mapToResponse(MasterLookupType entity) {
        List<LookupSubTypeResponse> subTypeResponses = entity.getSubTypes() != null
                ? entity.getSubTypes().stream().map(this::mapSubTypeToResponse).collect(Collectors.toList())
                : List.of();

        return LookupTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .isFixed(entity.getIsFixed())
                .isActive(entity.getIsActive())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .modifiedBy(entity.getModifiedBy())
                .updatedAt(entity.getUpdatedAt())
                .subTypes(subTypeResponses)
                .build();
    }

    private LookupSubTypeResponse mapSubTypeToResponse(MasterLookupSubType entity) {
        return LookupSubTypeResponse.builder()
                .id(entity.getId())
                .lookupTypeCode(entity.getLookupTypeCode())
                .typeDescription(entity.getTypeDescription())
                .subTypeCode(entity.getSubTypeCode())
                .subTypeDescription(entity.getSubTypeDescription())
                .isFixed(entity.getIsFixed())
                .isActive(entity.getIsActive())
                .displayOrder(entity.getDisplayOrder())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .modifiedBy(entity.getModifiedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private LookupTypeResponse mapBankTypeToResponse(BankLookupType entity) {
        List<LookupSubTypeResponse> subTypeResponses = entity.getSubTypes() != null
                ? entity.getSubTypes().stream().map(this::mapBankSubTypeToResponse).collect(Collectors.toList())
                : List.of();

        return LookupTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .isFixed(entity.getIsFixed())
                .isActive(entity.getIsActive())
                .canView(entity.getCanView() != null ? entity.getCanView() : true)
                .canAdd(entity.getCanAdd() != null ? entity.getCanAdd() : true)
                .canImportFromMaster(entity.getCanImportFromMaster() != null ? entity.getCanImportFromMaster() : true)
                .canEdit(entity.getCanEdit() != null ? entity.getCanEdit() : true)
                .canDelete(entity.getCanDelete() != null ? entity.getCanDelete() : true)
                .canActivate(entity.getCanActivate() != null ? entity.getCanActivate() : true)
                .canDeactivate(entity.getCanDeactivate() != null ? entity.getCanDeactivate() : true)
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .modifiedBy(entity.getModifiedBy())
                .updatedAt(entity.getUpdatedAt())
                .subTypes(subTypeResponses)
                .build();
    }

    private LookupSubTypeResponse mapBankSubTypeToResponse(BankLookupSubType entity) {
        return LookupSubTypeResponse.builder()
                .id(entity.getId())
                .lookupTypeCode(entity.getLookupTypeCode())
                .typeDescription(entity.getTypeDescription())
                .subTypeCode(entity.getSubTypeCode())
                .subTypeDescription(entity.getSubTypeDescription())
                .isFixed(entity.getIsFixed())
                .isActive(entity.getIsActive())
                .displayOrder(entity.getDisplayOrder())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .modifiedBy(entity.getModifiedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
