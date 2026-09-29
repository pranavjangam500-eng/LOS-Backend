package com.bank.los.bank.lookup.service;

import com.bank.los.administration.lookup.dto.LookupSubTypeResponse;
import com.bank.los.administration.lookup.dto.LookupTypeResponse;
import com.bank.los.administration.master.entity.MasterLookupSubType;
import com.bank.los.administration.master.repository.MasterLookupSubTypeRepository;
import com.bank.los.bank.audit.service.BankAuditService;
import com.bank.los.bank.auth.service.PermissionService;
import com.bank.los.bank.lookup.dto.BankCreateLookupSubTypeRequest;
import com.bank.los.bank.lookup.dto.BankUpdateLookupSubTypeRequest;
import com.bank.los.bank.master.entity.BankLookupSubType;
import com.bank.los.bank.master.entity.BankLookupType;
import com.bank.los.bank.master.entity.BankLookupTypePermission;
import com.bank.los.bank.master.repository.BankLookupSubTypeRepository;
import com.bank.los.bank.master.repository.BankLookupTypePermissionRepository;
import com.bank.los.bank.master.repository.BankLookupTypeRepository;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.common.exception.UnauthorizedException;
import com.bank.los.config.BankContext;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankLookupService {

    private final BankLookupTypeRepository bankLookupTypeRepository;
    private final BankLookupSubTypeRepository bankLookupSubTypeRepository;
    private final MasterLookupSubTypeRepository masterLookupSubTypeRepository;
    private final BankLookupTypePermissionRepository bankLookupTypePermissionRepository;
    private final PermissionService permissionService;
    private final BankAuditService bankAuditService;

    public static final Set<String> DEFAULT_LOOKUP_PERMISSIONS = Set.of(
            "VIEW", "ADD", "ADD_FROM_MASTER", "EDIT", "DELETE", "ACTIVATE", "DEACTIVATE"
    );

    @Transactional(readOnly = true)
    public List<LookupTypeResponse> getAllLookupTypes(UserPrincipal principal) {
        setTenantContext(principal);
        checkPermission(principal, ApplicationConstants.Permissions.LOOKUP_BANK_VIEW);
        return bankLookupTypeRepository.findAll().stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsActive()) && isLookupActionAllowed(t.getCode(), "VIEW"))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LookupTypeResponse getLookupTypeByCode(UserPrincipal principal, String code) {
        setTenantContext(principal);
        checkPermission(principal, ApplicationConstants.Permissions.LOOKUP_BANK_VIEW);
        BankLookupType type = bankLookupTypeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup type not found with code: " + code));

        if (!isLookupActionAllowed(code, "VIEW")) {
            throw new BusinessException("Lookup type '" + code + "' (" + type.getDescription() + ") viewing is disabled for this bank.");
        }

        return mapToResponse(type);
    }

    @Transactional(readOnly = true)
    public List<LookupSubTypeResponse> getOptionsByLookupCode(UserPrincipal principal, String lookupTypeCode) {
        setTenantContext(principal);
        checkPermission(principal, ApplicationConstants.Permissions.LOOKUP_BANK_VIEW);
        BankLookupType type = bankLookupTypeRepository.findByCode(lookupTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup type not found with code: " + lookupTypeCode));

        if (!isLookupActionAllowed(lookupTypeCode, "VIEW")) {
            throw new BusinessException("Lookup type '" + lookupTypeCode + "' (" + type.getDescription() + ") viewing is disabled for this bank.");
        }

        return bankLookupSubTypeRepository.findByLookupTypeCodeAndIsActiveTrueOrderByDisplayOrderAscIdAsc(lookupTypeCode)
                .stream()
                .map(this::mapSubTypeToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LookupSubTypeResponse addBankSubType(UserPrincipal principal, String lookupTypeCode, BankCreateLookupSubTypeRequest request) {
        setTenantContext(principal);
        checkPermission(principal, ApplicationConstants.Permissions.LOOKUP_BANK_ADD);

        BankLookupType type = bankLookupTypeRepository.findByCode(lookupTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup type not found with code: " + lookupTypeCode));

        if (!isLookupActionAllowed(lookupTypeCode, "ADD")) {
            throw new BusinessException("Lookup type '" + lookupTypeCode + "' (" + type.getDescription() + ") does not allow adding custom options for this bank.");
        }

        if (Boolean.TRUE.equals(type.getIsFixed())) {
            throw new BusinessException("Lookup type '" + lookupTypeCode + "' (" + type.getDescription() + ") is system-fixed and cannot be modified by the bank.");
        }

        if (bankLookupSubTypeRepository.existsByLookupTypeCodeAndSubTypeCode(lookupTypeCode, request.getSubTypeCode())) {
            throw new BusinessException("Option with code '" + request.getSubTypeCode() + "' already exists in this bank database.");
        }

        BankLookupSubType subType = BankLookupSubType.builder()
                .lookupTypeCode(lookupTypeCode)
                .typeDescription(type.getDescription())
                .subTypeCode(request.getSubTypeCode().trim())
                .subTypeDescription(request.getSubTypeDescription().trim())
                .isFixed(false)
                .isActive(true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .createdBy(principal != null ? principal.getId() : null)
                .build();

        BankLookupSubType saved = bankLookupSubTypeRepository.save(subType);
        log.info("Bank user {} added custom option code={} to lookup={}",
                principal != null ? principal.getEmail() : "USER", saved.getSubTypeCode(), lookupTypeCode);

        if (principal != null) {
            bankAuditService.logAction(
                    principal, "ADD_LOOKUP_OPTION", "LOOKUP",
                    "Added custom option " + saved.getSubTypeCode() + " (" + saved.getSubTypeDescription() + ") under " + lookupTypeCode, null
            );
        }

        return mapSubTypeToResponse(saved);
    }

    @Transactional
    public LookupSubTypeResponse importOptionFromMaster(UserPrincipal principal, String lookupTypeCode, String subTypeCode) {
        setTenantContext(principal);
        checkPermission(principal, ApplicationConstants.Permissions.LOOKUP_BANK_ADD_FROM_MASTER);

        BankLookupType type = bankLookupTypeRepository.findByCode(lookupTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup type not found in bank database with code: " + lookupTypeCode));

        if (!isLookupActionAllowed(lookupTypeCode, "ADD_FROM_MASTER")) {
            throw new BusinessException("Lookup type '" + lookupTypeCode + "' (" + type.getDescription() + ") does not allow importing options from master for this bank.");
        }

        if (Boolean.TRUE.equals(type.getIsFixed())) {
            throw new BusinessException("Lookup type '" + lookupTypeCode + "' is system-fixed.");
        }

        if (bankLookupSubTypeRepository.existsByLookupTypeCodeAndSubTypeCode(lookupTypeCode, subTypeCode)) {
            throw new BusinessException("Option '" + subTypeCode + "' already exists in bank database.");
        }

        // Read from Master DB
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        MasterLookupSubType masterOpt = masterLookupSubTypeRepository.findByLookupTypeCodeAndSubTypeCode(lookupTypeCode, subTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Master option not found for " + lookupTypeCode + " / " + subTypeCode));

        // Switch back to Bank DB
        setTenantContext(principal);
        BankLookupSubType imported = BankLookupSubType.builder()
                .lookupTypeCode(lookupTypeCode)
                .typeDescription(masterOpt.getTypeDescription())
                .subTypeCode(masterOpt.getSubTypeCode())
                .subTypeDescription(masterOpt.getSubTypeDescription())
                .isFixed(false)
                .isActive(true)
                .displayOrder(masterOpt.getDisplayOrder())
                .createdBy(principal != null ? principal.getId() : null)
                .build();

        BankLookupSubType saved = bankLookupSubTypeRepository.save(imported);
        log.info("Bank user {} imported master option code={} to lookup={}",
                principal != null ? principal.getEmail() : "USER", saved.getSubTypeCode(), lookupTypeCode);

        if (principal != null) {
            bankAuditService.logAction(
                    principal, "IMPORT_MASTER_LOOKUP_OPTION", "LOOKUP",
                    "Imported master option " + saved.getSubTypeCode() + " into " + lookupTypeCode, null
            );
        }

        return mapSubTypeToResponse(saved);
    }

    @Transactional
    public LookupSubTypeResponse updateBankSubType(UserPrincipal principal, Long id, BankUpdateLookupSubTypeRequest request) {
        setTenantContext(principal);
        checkPermission(principal, ApplicationConstants.Permissions.LOOKUP_BANK_EDIT);

        BankLookupSubType subType = bankLookupSubTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup option not found with id: " + id));

        BankLookupType type = bankLookupTypeRepository.findByCode(subType.getLookupTypeCode()).orElse(null);
        if (type != null && !isLookupActionAllowed(subType.getLookupTypeCode(), "EDIT")) {
            throw new BusinessException("Lookup type '" + subType.getLookupTypeCode() + "' (" + type.getDescription() + ") does not allow editing options for this bank.");
        }

        if (Boolean.TRUE.equals(subType.getIsFixed())) {
            throw new BusinessException("System-fixed options cannot be modified.");
        }

        if (type != null && Boolean.TRUE.equals(type.getIsFixed())) {
            throw new BusinessException("Cannot modify options of system-fixed lookup type: " + type.getDescription());
        }

        subType.setSubTypeDescription(request.getSubTypeDescription().trim());
        if (request.getIsActive() != null) {
            subType.setIsActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            subType.setDisplayOrder(request.getDisplayOrder());
        }
        subType.setModifiedBy(principal != null ? principal.getId() : null);

        BankLookupSubType saved = bankLookupSubTypeRepository.save(subType);

        if (principal != null) {
            bankAuditService.logAction(
                    principal, "UPDATE_LOOKUP_OPTION", "LOOKUP",
                    "Updated option ID " + id + " (" + saved.getSubTypeCode() + " - " + saved.getSubTypeDescription() + ")", null
            );
        }

        return mapSubTypeToResponse(saved);
    }

    @Transactional
    public void deleteBankSubType(UserPrincipal principal, Long id) {
        setTenantContext(principal);
        checkPermission(principal, ApplicationConstants.Permissions.LOOKUP_BANK_DELETE);

        BankLookupSubType subType = bankLookupSubTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup option not found with id: " + id));

        BankLookupType type = bankLookupTypeRepository.findByCode(subType.getLookupTypeCode()).orElse(null);
        if (type != null && !isLookupActionAllowed(subType.getLookupTypeCode(), "DELETE")) {
            throw new BusinessException("Lookup type '" + subType.getLookupTypeCode() + "' (" + type.getDescription() + ") does not allow deleting options for this bank.");
        }

        if (Boolean.TRUE.equals(subType.getIsFixed())) {
            throw new BusinessException("System-fixed options cannot be deleted.");
        }

        if (type != null && Boolean.TRUE.equals(type.getIsFixed())) {
            throw new BusinessException("Cannot delete options from system-fixed lookup type: " + type.getDescription());
        }

        bankLookupSubTypeRepository.delete(subType);
        log.info("Bank user {} deleted custom lookup option id={}", principal != null ? principal.getEmail() : "USER", id);

        if (principal != null) {
            bankAuditService.logAction(
                    principal, "DELETE_LOOKUP_OPTION", "LOOKUP",
                    "Deleted option ID " + id + " (" + subType.getSubTypeCode() + " - " + subType.getSubTypeDescription() + ")", null
            );
        }
    }

    @Transactional
    public LookupSubTypeResponse activateBankSubType(UserPrincipal principal, Long id) {
        setTenantContext(principal);
        checkPermission(principal, ApplicationConstants.Permissions.LOOKUP_BANK_ACTIVATE);

        BankLookupSubType subType = bankLookupSubTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup option not found with id: " + id));

        BankLookupType type = bankLookupTypeRepository.findByCode(subType.getLookupTypeCode()).orElse(null);
        if (type != null && !isLookupActionAllowed(subType.getLookupTypeCode(), "ACTIVATE")) {
            throw new BusinessException("Lookup type '" + subType.getLookupTypeCode() + "' (" + type.getDescription() + ") does not allow activating options for this bank.");
        }

        subType.setIsActive(true);
        subType.setModifiedBy(principal != null ? principal.getId() : null);
        BankLookupSubType saved = bankLookupSubTypeRepository.save(subType);

        if (principal != null) {
            bankAuditService.logAction(
                    principal, "ACTIVATE_LOOKUP_OPTION", "LOOKUP",
                    "Activated option ID " + id + " (" + saved.getSubTypeCode() + ")", null
            );
        }

        return mapSubTypeToResponse(saved);
    }

    @Transactional
    public LookupSubTypeResponse deactivateBankSubType(UserPrincipal principal, Long id) {
        setTenantContext(principal);
        checkPermission(principal, ApplicationConstants.Permissions.LOOKUP_BANK_DEACTIVATE);

        BankLookupSubType subType = bankLookupSubTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup option not found with id: " + id));

        BankLookupType type = bankLookupTypeRepository.findByCode(subType.getLookupTypeCode()).orElse(null);
        if (type != null && !isLookupActionAllowed(subType.getLookupTypeCode(), "DEACTIVATE")) {
            throw new BusinessException("Lookup type '" + subType.getLookupTypeCode() + "' (" + type.getDescription() + ") does not allow deactivating options for this bank.");
        }

        if (Boolean.TRUE.equals(subType.getIsFixed())) {
            throw new BusinessException("System-fixed options cannot be deactivated.");
        }

        subType.setIsActive(false);
        subType.setModifiedBy(principal != null ? principal.getId() : null);
        BankLookupSubType saved = bankLookupSubTypeRepository.save(subType);

        if (principal != null) {
            bankAuditService.logAction(
                    principal, "DEACTIVATE_LOOKUP_OPTION", "LOOKUP",
                    "Deactivated option ID " + id + " (" + saved.getSubTypeCode() + ")", null
            );
        }

        return mapSubTypeToResponse(saved);
    }

    public boolean isLookupActionAllowed(String lookupTypeCode, String action) {
        List<BankLookupTypePermission> perms = bankLookupTypePermissionRepository.findByLookupTypeCode(lookupTypeCode);
        if (perms.isEmpty()) {
            return true;
        }
        return perms.stream().anyMatch(p -> p.getPermissionCode().equalsIgnoreCase(action));
    }

    private void checkPermission(UserPrincipal principal, String requiredPermission) {
        if (principal == null) return;
        if (!permissionService.hasEffectivePermission(principal, requiredPermission)) {
            throw new UnauthorizedException("Permission denied: User lacks " + requiredPermission + " permission.");
        }
    }

    private void setTenantContext(UserPrincipal principal) {
        if (principal != null && principal.getOrganizationDbName() != null) {
            BankContext.setCurrentBank(principal.getOrganizationDbName());
            OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        }
    }

    private LookupTypeResponse mapToResponse(BankLookupType entity) {
        List<LookupSubTypeResponse> subTypeResponses = entity.getSubTypes() != null
                ? entity.getSubTypes().stream()
                .filter(st -> Boolean.TRUE.equals(st.getIsActive()))
                .map(this::mapSubTypeToResponse)
                .collect(Collectors.toList())
                : List.of();

        List<BankLookupTypePermission> perms = bankLookupTypePermissionRepository.findByLookupTypeCode(entity.getCode());
        Set<String> permCodes = perms.isEmpty()
                ? new HashSet<>(DEFAULT_LOOKUP_PERMISSIONS)
                : perms.stream().map(BankLookupTypePermission::getPermissionCode).collect(Collectors.toSet());

        return LookupTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .isFixed(entity.getIsFixed())
                .isActive(entity.getIsActive())
                .permissions(permCodes)
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .modifiedBy(entity.getModifiedBy())
                .updatedAt(entity.getUpdatedAt())
                .subTypes(subTypeResponses)
                .build();
    }

    private LookupSubTypeResponse mapSubTypeToResponse(BankLookupSubType entity) {
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
