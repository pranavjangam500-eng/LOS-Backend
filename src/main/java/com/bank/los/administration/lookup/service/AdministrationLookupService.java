package com.bank.los.administration.lookup.service;

import com.bank.los.administration.audit.service.AdminAuditService;
import com.bank.los.administration.lookup.dto.*;
import com.bank.los.administration.master.entity.MasterLookupSubType;
import com.bank.los.administration.master.entity.MasterLookupType;
import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.MasterLookupSubTypeRepository;
import com.bank.los.administration.master.repository.MasterLookupTypeRepository;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.bank.lookup.service.BankLookupService;
import com.bank.los.bank.master.entity.BankLookupSubType;
import com.bank.los.bank.master.entity.BankLookupType;
import com.bank.los.bank.master.entity.BankLookupTypePermission;
import com.bank.los.bank.master.repository.BankLookupSubTypeRepository;
import com.bank.los.bank.master.repository.BankLookupTypePermissionRepository;
import com.bank.los.bank.master.repository.BankLookupTypeRepository;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ConflictException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.BankContext;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdministrationLookupService {

    private final MasterLookupTypeRepository masterLookupTypeRepository;
    private final MasterLookupSubTypeRepository masterLookupSubTypeRepository;
    private final com.bank.los.administration.master.repository.MasterLookupTypePermissionRepository masterLookupTypePermissionRepository;
    private final OrganizationRepository organizationRepository;
    private final BankLookupTypeRepository bankLookupTypeRepository;
    private final BankLookupSubTypeRepository bankLookupSubTypeRepository;
    private final BankLookupTypePermissionRepository bankLookupTypePermissionRepository;
    private final AdminAuditService adminAuditService;
    private final EntityManager entityManager;

    @PostConstruct
    public void init() {
        try {
            ensureMasterLookupTypeCodeSequence();
        } catch (Exception e) {
            log.debug("Notice during master lookup sequence initialization: {}", e.getMessage());
        }
    }

    @Transactional
    public synchronized void ensureMasterLookupTypeCodeSequence() {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        try {
            entityManager.createNativeQuery(
                    "CREATE SEQUENCE IF NOT EXISTS identity.master_lookup_type_code_seq START WITH 10010"
            ).executeUpdate();

            List<?> existingCodes = entityManager.createNativeQuery(
                    "SELECT code FROM identity.lookup_types"
            ).getResultList();

            long maxNumeric = 10009;
            for (Object item : existingCodes) {
                if (item != null) {
                    String c = item.toString().trim();
                    if (c.matches("^\\d+$")) {
                        try {
                            long val = Long.parseLong(c);
                            if (val > maxNumeric) {
                                maxNumeric = val;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            if (maxNumeric >= 10010) {
                try {
                    Object currentValObj = entityManager.createNativeQuery(
                            "SELECT last_value FROM identity.master_lookup_type_code_seq"
                    ).getSingleResult();
                    long currentVal = ((Number) currentValObj).longValue();
                    if (currentVal < maxNumeric) {
                        entityManager.createNativeQuery(
                                "SELECT setval('identity.master_lookup_type_code_seq', " + maxNumeric + ", true)"
                        ).getSingleResult();
                    }
                } catch (Exception ex) {
                    try {
                        entityManager.createNativeQuery(
                                "ALTER SEQUENCE identity.master_lookup_type_code_seq RESTART WITH " + (maxNumeric + 1)
                        ).executeUpdate();
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            log.debug("Notice in ensureMasterLookupTypeCodeSequence: {}", e.getMessage());
        }
    }

    private String generateNextLookupTypeCode() {
        try {
            Number nextVal = (Number) entityManager
                    .createNativeQuery("SELECT nextval('identity.master_lookup_type_code_seq')")
                    .getSingleResult();
            return String.valueOf(nextVal.longValue());
        } catch (Exception e) {
            ensureMasterLookupTypeCodeSequence();
            Number nextVal = (Number) entityManager
                    .createNativeQuery("SELECT nextval('identity.master_lookup_type_code_seq')")
                    .getSingleResult();
            return String.valueOf(nextVal.longValue());
        }
    }

    // =========================================================================
    //  MASTER LOOKUP TYPES (CENTRAL ADMIN & BANK VISIBILITY)
    // =========================================================================

    private void validateModificationAccess(String ownerType, Long ownerBankId, UserPrincipal principal) {
        if (principal == null || principal.isPlatformAdmin()) {
            return; // Platform admin can modify all
        }
        if (ownerType == null || "INTERNAL_ADMIN".equalsIgnoreCase(ownerType)) {
            throw new org.springframework.security.access.AccessDeniedException("This lookup is managed by the platform and cannot be modified.");
        }
        if ("BANK".equalsIgnoreCase(ownerType)) {
            Long userBankId = principal.getOrganizationId();
            if (userBankId == null || !userBankId.equals(ownerBankId)) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not have permission to modify this bank lookup.");
            }
        }
    }

    @Transactional(readOnly = true)
    public List<LookupTypeResponse> getAllLookupTypes(UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        if (principal != null && !principal.isPlatformAdmin()) {
            Long bankId = principal.getOrganizationId();
            return masterLookupTypeRepository.findAllVisibleToBank(bankId).stream()
                    .map(t -> mapToResponse(t, false, principal))
                    .collect(Collectors.toList());
        }
        return masterLookupTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "code")).stream()
                .map(t -> mapToResponse(t, false, principal))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LookupTypeResponse> getAllLookupTypes() {
        return getAllLookupTypes(null);
    }

    @Transactional(readOnly = true)
    public PagedLookupResponse<LookupTypeResponse> searchLookupTypes(String search, String status, int page, int size, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        Specification<MasterLookupType> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (principal != null && !principal.isPlatformAdmin()) {
                Long bankId = principal.getOrganizationId();
                predicates.add(cb.or(
                        cb.equal(root.get("ownerType"), "INTERNAL_ADMIN"),
                        cb.isNull(root.get("ownerType")),
                        cb.equal(root.get("ownerBankId"), bankId)
                ));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (status != null && !status.isBlank()) {
                boolean active = !"INACTIVE".equalsIgnoreCase(status.trim());
                predicates.add(cb.equal(root.get("isActive"), active));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        Page<MasterLookupType> pagedResult = masterLookupTypeRepository.findAll(spec, pageable);

        List<LookupTypeResponse> items = pagedResult.getContent().stream()
                .map(t -> mapToResponse(t, false, principal))
                .collect(Collectors.toList());

        return PagedLookupResponse.<LookupTypeResponse>builder()
                .items(items)
                .page(pagedResult.getNumber())
                .size(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public PagedLookupResponse<LookupTypeResponse> searchLookupTypes(String search, String status, int page, int size) {
        return searchLookupTypes(search, status, page, size, null);
    }

    @Transactional(readOnly = true)
    public LookupTypeResponse getLookupTypeByCode(String code, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        MasterLookupType type = masterLookupTypeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + code));

        if (principal != null && !principal.isPlatformAdmin()) {
            Long bankId = principal.getOrganizationId();
            boolean isVisible = "INTERNAL_ADMIN".equalsIgnoreCase(type.getOwnerType()) ||
                    type.getOwnerType() == null ||
                    (bankId != null && bankId.equals(type.getOwnerBankId()));
            if (!isVisible) {
                throw new ResourceNotFoundException("Master lookup type not found with code: " + code);
            }
        }
        return mapToResponse(type, true, principal);
    }

    @Transactional(readOnly = true)
    public LookupTypeResponse getLookupTypeByCode(String code) {
        return getLookupTypeByCode(code, null);
    }

    @Transactional
    public LookupTypeResponse createLookupType(CreateLookupTypeRequest request, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        String typeName = request.getName() != null ? request.getName().trim() : "";
        if (typeName.isEmpty() && request.getDescription() != null) {
            typeName = request.getDescription().trim();
        }
        if (typeName.isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "Lookup type name is required");
        }

        // Case-insensitive duplicate name -> 409
        if (masterLookupTypeRepository.existsByDescriptionIgnoreCase(typeName)) {
            throw new ConflictException("Lookup type already exists with name: " + typeName);
        }

        // Server-generated unique type code
        String generatedCode = generateNextLookupTypeCode();

        boolean isActive = !"INACTIVE".equalsIgnoreCase(request.getStatus());
        boolean isPlatformAdmin = principal == null || principal.isPlatformAdmin();
        String ownerType = isPlatformAdmin ? "INTERNAL_ADMIN" : "BANK";
        Long ownerBankId = isPlatformAdmin ? null : principal.getOrganizationId();

        MasterLookupType lookupType = MasterLookupType.builder()
                .code(generatedCode)
                .description(typeName)
                .isFixed(Boolean.TRUE.equals(request.getIsFixed()))
                .isActive(isActive)
                .ownerType(ownerType)
                .ownerBankId(ownerBankId)
                .createdBy(principal != null ? principal.getId() : null)
                .build();

        MasterLookupType saved = masterLookupTypeRepository.save(lookupType);

        // Save permissions (default: 7 standard permissions)
        List<String> permsToSave = (request.getPermissions() != null && !request.getPermissions().isEmpty())
                ? request.getPermissions()
                : new ArrayList<>(BankLookupService.DEFAULT_LOOKUP_PERMISSIONS);

        for (String perm : permsToSave) {
            if (perm != null && !perm.trim().isEmpty()) {
                masterLookupTypePermissionRepository.save(com.bank.los.administration.master.entity.MasterLookupTypePermission.builder()
                        .lookupTypeCode(saved.getCode())
                        .permissionCode(perm.trim().toUpperCase())
                        .build());
            }
        }

        log.info("Created new Master Lookup Type code={} name='{}' ownerType={} by user={}",
                saved.getCode(), saved.getDescription(), ownerType, principal != null ? principal.getEmail() : "SYSTEM");
        return mapToResponse(saved, true, principal);
    }

    @Transactional
    public LookupTypeResponse updateLookupType(String code, UpdateLookupTypeRequest request, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupType type = masterLookupTypeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + code));

        validateModificationAccess(type.getOwnerType(), type.getOwnerBankId(), principal);

        String newName = request.getName() != null ? request.getName().trim() : "";
        if (newName.isEmpty() && request.getDescription() != null) {
            newName = request.getDescription().trim();
        }
        if (newName.isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "Lookup type name is required");
        }

        // Case-insensitive duplicate name check against other types -> 409
        if (masterLookupTypeRepository.existsByDescriptionIgnoreCaseAndCodeNot(newName, code)) {
            throw new ConflictException("Lookup type already exists with name: " + newName);
        }

        type.setDescription(newName);

        Boolean resolvedActive = request.resolveIsActive();
        if (resolvedActive != null) {
            if (!resolvedActive && Boolean.TRUE.equals(type.getIsFixed())) {
                throw new BusinessException("CANNOT_DEACTIVATE_FIXED", "Cannot deactivate system-fixed lookup type: " + code);
            }
            type.setIsActive(resolvedActive);
        }

        type.setModifiedBy(principal != null ? principal.getId() : null);

        MasterLookupType saved = masterLookupTypeRepository.save(type);

        if (request.getPermissions() != null) {
            masterLookupTypePermissionRepository.deleteByLookupTypeCode(code);
            for (String perm : request.getPermissions()) {
                if (perm != null && !perm.trim().isEmpty()) {
                    masterLookupTypePermissionRepository.save(com.bank.los.administration.master.entity.MasterLookupTypePermission.builder()
                            .lookupTypeCode(code)
                            .permissionCode(perm.trim().toUpperCase())
                            .build());
                }
            }
        }

        log.info("Updated Master Lookup Type code={} by user={}", saved.getCode(), principal != null ? principal.getEmail() : "SYSTEM");
        return mapToResponse(saved, true, principal);
    }

    @Transactional
    public LookupTypeResponse activateLookupType(String code, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupType type = masterLookupTypeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + code));

        validateModificationAccess(type.getOwnerType(), type.getOwnerBankId(), principal);

        type.setIsActive(true);
        type.setModifiedBy(principal != null ? principal.getId() : null);
        MasterLookupType saved = masterLookupTypeRepository.save(type);
        log.info("Activated Master Lookup Type code={}", code);
        return mapToResponse(saved, true, principal);
    }

    @Transactional
    public LookupTypeResponse deactivateLookupType(String code, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupType type = masterLookupTypeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + code));

        validateModificationAccess(type.getOwnerType(), type.getOwnerBankId(), principal);

        if (Boolean.TRUE.equals(type.getIsFixed())) {
            throw new BusinessException("CANNOT_DEACTIVATE_FIXED", "Cannot deactivate system-fixed lookup type: " + code);
        }

        type.setIsActive(false);
        type.setModifiedBy(principal != null ? principal.getId() : null);
        MasterLookupType saved = masterLookupTypeRepository.save(type);
        log.info("Deactivated Master Lookup Type code={}", code);
        return mapToResponse(saved, true, principal);
    }

    @Transactional
    public void deleteLookupType(String code, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupType type = masterLookupTypeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + code));

        validateModificationAccess(type.getOwnerType(), type.getOwnerBankId(), principal);

        if (Boolean.TRUE.equals(type.getIsFixed())) {
            throw new BusinessException("CANNOT_DELETE_FIXED", "Cannot delete system fixed master lookup type: " + code);
        }

        masterLookupSubTypeRepository.deleteByLookupTypeCode(code);
        masterLookupTypePermissionRepository.deleteByLookupTypeCode(code);
        masterLookupTypeRepository.delete(type);
        log.info("Deleted Master Lookup Type code={}", code);
    }

    @Transactional
    public void deleteLookupType(String code) {
        deleteLookupType(code, null);
    }

    // =========================================================================
    //  MASTER LOOKUP SUB-TYPES / OPTIONS (CENTRAL ADMIN & BANK VISIBILITY)
    // =========================================================================

    @Transactional(readOnly = true)
    public List<LookupSubTypeResponse> getSubTypesByLookupTypeCode(String lookupTypeCode, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupType type = masterLookupTypeRepository.findByCode(lookupTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + lookupTypeCode));

        if (principal != null && !principal.isPlatformAdmin()) {
            Long bankId = principal.getOrganizationId();
            boolean isVisible = "INTERNAL_ADMIN".equalsIgnoreCase(type.getOwnerType()) ||
                    type.getOwnerType() == null ||
                    (bankId != null && bankId.equals(type.getOwnerBankId()));
            if (!isVisible) {
                throw new ResourceNotFoundException("Master lookup type not found with code: " + lookupTypeCode);
            }
            return masterLookupSubTypeRepository.findVisibleToBank(lookupTypeCode, bankId).stream()
                    .map(this::mapSubTypeToResponse)
                    .collect(Collectors.toList());
        }

        return masterLookupSubTypeRepository.findByLookupTypeCodeOrderByDisplayOrderAscIdAsc(lookupTypeCode).stream()
                .map(this::mapSubTypeToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LookupSubTypeResponse> getSubTypesByLookupTypeCode(String lookupTypeCode) {
        return getSubTypesByLookupTypeCode(lookupTypeCode, null);
    }

    @Transactional(readOnly = true)
    public PagedLookupResponse<LookupSubTypeResponse> searchLookupSubTypes(
            String lookupTypeCode, String search, String status, int page, int size, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupType type = masterLookupTypeRepository.findByCode(lookupTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + lookupTypeCode));

        if (principal != null && !principal.isPlatformAdmin()) {
            Long bankId = principal.getOrganizationId();
            boolean isVisible = "INTERNAL_ADMIN".equalsIgnoreCase(type.getOwnerType()) ||
                    type.getOwnerType() == null ||
                    (bankId != null && bankId.equals(type.getOwnerBankId()));
            if (!isVisible) {
                throw new ResourceNotFoundException("Master lookup type not found with code: " + lookupTypeCode);
            }
        }

        Specification<MasterLookupSubType> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("lookupTypeCode"), lookupTypeCode));
            if (principal != null && !principal.isPlatformAdmin()) {
                Long bankId = principal.getOrganizationId();
                predicates.add(cb.or(
                        cb.equal(root.get("ownerType"), "INTERNAL_ADMIN"),
                        cb.isNull(root.get("ownerType")),
                        cb.equal(root.get("ownerBankId"), bankId)
                ));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("subTypeCode")), pattern),
                        cb.like(cb.lower(root.get("subTypeDescription")), pattern)
                ));
            }
            if (status != null && !status.isBlank()) {
                boolean active = !"INACTIVE".equalsIgnoreCase(status.trim());
                predicates.add(cb.equal(root.get("isActive"), active));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder").and(Sort.by(Sort.Direction.ASC, "id")));
        Page<MasterLookupSubType> pagedResult = masterLookupSubTypeRepository.findAll(spec, pageable);

        List<LookupSubTypeResponse> items = pagedResult.getContent().stream()
                .map(this::mapSubTypeToResponse)
                .collect(Collectors.toList());

        return PagedLookupResponse.<LookupSubTypeResponse>builder()
                .items(items)
                .page(pagedResult.getNumber())
                .size(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public PagedLookupResponse<LookupSubTypeResponse> searchLookupSubTypes(
            String lookupTypeCode, String search, String status, int page, int size) {
        return searchLookupSubTypes(lookupTypeCode, search, status, page, size, null);
    }

    @Transactional
    public LookupSubTypeResponse addSubType(String lookupTypeCode, CreateLookupSubTypeRequest request, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        // 1. Pessimistic lock on parent type row
        MasterLookupType type = masterLookupTypeRepository.findByCodeForUpdate(lookupTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + lookupTypeCode));

        if (principal != null && !principal.isPlatformAdmin()) {
            Long bankId = principal.getOrganizationId();
            boolean isVisible = "INTERNAL_ADMIN".equalsIgnoreCase(type.getOwnerType()) ||
                    type.getOwnerType() == null ||
                    (bankId != null && bankId.equals(type.getOwnerBankId()));
            if (!isVisible) {
                throw new ResourceNotFoundException("Master lookup type not found with code: " + lookupTypeCode);
            }
        }

        String subTypeName = request.getName() != null ? request.getName().trim() : "";
        if (subTypeName.isEmpty() && request.getSubTypeDescription() != null) {
            subTypeName = request.getSubTypeDescription().trim();
        }
        if (subTypeName.isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "Lookup sub-type name is required");
        }

        // 2. Duplicate name under the same type (case-insensitive) -> 409
        if (masterLookupSubTypeRepository.existsByLookupTypeCodeAndSubTypeDescriptionIgnoreCase(lookupTypeCode, subTypeName)) {
            throw new ConflictException("Lookup sub-type already exists with name '" + subTypeName + "' under type " + lookupTypeCode);
        }

        // 3. Sub-type code = highest numeric sub_type_code under that type + 1
        List<MasterLookupSubType> existing = masterLookupSubTypeRepository.findByLookupTypeCodeOrderByDisplayOrderAscIdAsc(lookupTypeCode);
        long maxNumericCode = 0;
        int maxDisplayOrder = 0;
        for (MasterLookupSubType st : existing) {
            String codeStr = st.getSubTypeCode();
            if (codeStr != null && codeStr.matches("^\\d+$")) {
                try {
                    long val = Long.parseLong(codeStr);
                    if (val > maxNumericCode) {
                        maxNumericCode = val;
                    }
                } catch (NumberFormatException ignored) {}
            }
            if (st.getDisplayOrder() != null && st.getDisplayOrder() > maxDisplayOrder) {
                maxDisplayOrder = st.getDisplayOrder();
            }
        }
        String generatedSubTypeCode = String.valueOf(maxNumericCode == 0 ? 1 : (maxNumericCode + 1));

        int displayOrder = request.getDisplayOrder() != null ? request.getDisplayOrder() : (maxDisplayOrder + 1);
        boolean isActive = !"INACTIVE".equalsIgnoreCase(request.getStatus());

        boolean isPlatformAdmin = principal == null || principal.isPlatformAdmin();
        String ownerType = isPlatformAdmin ? "INTERNAL_ADMIN" : "BANK";
        Long ownerBankId = isPlatformAdmin ? null : principal.getOrganizationId();

        MasterLookupSubType subType = MasterLookupSubType.builder()
                .lookupTypeCode(lookupTypeCode)
                .typeDescription(type.getDescription())
                .subTypeCode(generatedSubTypeCode)
                .subTypeDescription(subTypeName)
                .isFixed(Boolean.TRUE.equals(request.getIsFixed()))
                .isActive(isActive)
                .displayOrder(displayOrder)
                .ownerType(ownerType)
                .ownerBankId(ownerBankId)
                .createdBy(principal != null ? principal.getId() : null)
                .build();

        MasterLookupSubType saved = masterLookupSubTypeRepository.save(subType);
        log.info("Added SubType code={} name='{}' to Master Lookup Type={} ownerType={} by user={}",
                saved.getSubTypeCode(), saved.getSubTypeDescription(), lookupTypeCode, ownerType, principal != null ? principal.getEmail() : "SYSTEM");
        return mapSubTypeToResponse(saved);
    }

    @Transactional
    public LookupSubTypeResponse updateSubType(Long id, UpdateLookupSubTypeRequest request, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupSubType subType = masterLookupSubTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup sub-type not found with id: " + id));

        validateModificationAccess(subType.getOwnerType(), subType.getOwnerBankId(), principal);

        String newName = request.getName() != null ? request.getName().trim() : "";
        if (newName.isEmpty() && request.getSubTypeDescription() != null) {
            newName = request.getSubTypeDescription().trim();
        }
        if (newName.isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "Lookup sub-type name is required");
        }

        // Duplicate name under same type check -> 409
        if (masterLookupSubTypeRepository.existsByLookupTypeCodeAndSubTypeDescriptionIgnoreCaseAndIdNot(
                subType.getLookupTypeCode(), newName, id)) {
            throw new ConflictException("Lookup sub-type already exists with name '" + newName + "' under type " + subType.getLookupTypeCode());
        }

        subType.setSubTypeDescription(newName);

        Boolean resolvedActive = request.resolveIsActive();
        if (resolvedActive != null) {
            if (!resolvedActive) {
                MasterLookupType parentType = masterLookupTypeRepository.findByCode(subType.getLookupTypeCode()).orElse(null);
                if (Boolean.TRUE.equals(subType.getIsFixed()) || (parentType != null && Boolean.TRUE.equals(parentType.getIsFixed()))) {
                    throw new BusinessException("CANNOT_DEACTIVATE_FIXED",
                            "Cannot deactivate option under system-fixed lookup type: " + subType.getLookupTypeCode());
                }
            }
            subType.setIsActive(resolvedActive);
        }

        if (request.getDisplayOrder() != null) {
            subType.setDisplayOrder(request.getDisplayOrder());
        }
        subType.setModifiedBy(principal != null ? principal.getId() : null);

        MasterLookupSubType saved = masterLookupSubTypeRepository.save(subType);
        return mapSubTypeToResponse(saved);
    }

    @Transactional
    public LookupSubTypeResponse activateSubType(Long id, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupSubType subType = masterLookupSubTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup sub-type not found with id: " + id));

        validateModificationAccess(subType.getOwnerType(), subType.getOwnerBankId(), principal);

        subType.setIsActive(true);
        subType.setModifiedBy(principal != null ? principal.getId() : null);
        MasterLookupSubType saved = masterLookupSubTypeRepository.save(subType);
        log.info("Activated Master Lookup SubType id={}", id);
        return mapSubTypeToResponse(saved);
    }

    @Transactional
    public LookupSubTypeResponse deactivateSubType(Long id, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupSubType subType = masterLookupSubTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup sub-type not found with id: " + id));

        validateModificationAccess(subType.getOwnerType(), subType.getOwnerBankId(), principal);

        MasterLookupType parentType = masterLookupTypeRepository.findByCode(subType.getLookupTypeCode()).orElse(null);
        if (Boolean.TRUE.equals(subType.getIsFixed()) || (parentType != null && Boolean.TRUE.equals(parentType.getIsFixed()))) {
            throw new BusinessException("CANNOT_DEACTIVATE_FIXED",
                    "Cannot deactivate option under system-fixed lookup type: " + subType.getLookupTypeCode());
        }

        subType.setIsActive(false);
        subType.setModifiedBy(principal != null ? principal.getId() : null);
        MasterLookupSubType saved = masterLookupSubTypeRepository.save(subType);
        log.info("Deactivated Master Lookup SubType id={}", id);
        return mapSubTypeToResponse(saved);
    }

    @Transactional
    public void deleteSubType(Long id, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupSubType subType = masterLookupSubTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup sub-type not found with id: " + id));

        validateModificationAccess(subType.getOwnerType(), subType.getOwnerBankId(), principal);

        MasterLookupType parentType = masterLookupTypeRepository.findByCode(subType.getLookupTypeCode()).orElse(null);
        if (Boolean.TRUE.equals(subType.getIsFixed()) || (parentType != null && Boolean.TRUE.equals(parentType.getIsFixed()))) {
            throw new BusinessException("CANNOT_DELETE_FIXED",
                    "Cannot delete option under system fixed master lookup type: " + subType.getLookupTypeCode());
        }

        masterLookupSubTypeRepository.delete(subType);
        log.info("Deleted Master Lookup SubType id={}", id);
    }

    @Transactional
    public void deleteSubType(Long id) {
        deleteSubType(id, null);
    }

    // =========================================================================
    //  DROPDOWN OPTIONS FOR INTERNAL ADMIN PANEL & BANK APIS
    // =========================================================================

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> getActiveOptionsByLookupCode(String lookupTypeCode, UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        MasterLookupType type = masterLookupTypeRepository.findByCode(lookupTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup type not found with code: " + lookupTypeCode));

        if (!Boolean.TRUE.equals(type.getIsActive())) {
            throw new ResourceNotFoundException("Master lookup type is not active with code: " + lookupTypeCode);
        }

        List<MasterLookupSubType> activeSubTypes;
        if (principal != null && !principal.isPlatformAdmin()) {
            Long bankId = principal.getOrganizationId();
            boolean isVisible = "INTERNAL_ADMIN".equalsIgnoreCase(type.getOwnerType()) ||
                    type.getOwnerType() == null ||
                    (bankId != null && bankId.equals(type.getOwnerBankId()));
            if (!isVisible) {
                throw new ResourceNotFoundException("Master lookup type not found with code: " + lookupTypeCode);
            }
            activeSubTypes = masterLookupSubTypeRepository.findActiveVisibleToBank(lookupTypeCode, bankId);
        } else {
            activeSubTypes = masterLookupSubTypeRepository
                    .findByLookupTypeCodeAndIsActiveTrueOrderByDisplayOrderAscIdAsc(lookupTypeCode);
        }

        return activeSubTypes.stream()
                .map(st -> LookupOptionResponse.builder()
                        .code(st.getSubTypeCode())
                        .description(st.getSubTypeDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> getActiveOptionsByLookupCode(String lookupTypeCode) {
        return getActiveOptionsByLookupCode(lookupTypeCode, null);
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

                List<BankLookupTypePermission> existingPerms = bankLookupTypePermissionRepository.findByLookupTypeCode(mt.getCode());
                if (existingPerms.isEmpty()) {
                    List<com.bank.los.administration.master.entity.MasterLookupTypePermission> masterPerms =
                            masterLookupTypePermissionRepository.findByLookupTypeCode(mt.getCode());
                    if (!masterPerms.isEmpty()) {
                        for (com.bank.los.administration.master.entity.MasterLookupTypePermission mp : masterPerms) {
                            bankLookupTypePermissionRepository.save(BankLookupTypePermission.builder()
                                    .lookupTypeCode(mt.getCode())
                                    .permissionCode(mp.getPermissionCode())
                                    .build());
                        }
                    } else {
                        for (String perm : BankLookupService.DEFAULT_LOOKUP_PERMISSIONS) {
                            bankLookupTypePermissionRepository.save(BankLookupTypePermission.builder()
                                    .lookupTypeCode(mt.getCode())
                                    .permissionCode(perm)
                                    .build());
                        }
                    }
                }
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
                syncMasterLookupsToBank(org.getBankCode());
            } catch (Exception e) {
                log.warn("Failed to sync lookups to org {}: {}", org.getBankCode(), e.getMessage());
            }
        }
    }

    @Transactional
    public LookupTypeResponse updateBankLookupPermissions(
            String bankCode,
            String lookupTypeCode,
            UpdateBankLookupPermissionsRequest request,
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

            bankLookupTypePermissionRepository.deleteByLookupTypeCode(lookupTypeCode);

            if (request.getPermissions() != null) {
                for (String perm : request.getPermissions()) {
                    if (perm != null && !perm.trim().isEmpty()) {
                        bankLookupTypePermissionRepository.save(BankLookupTypePermission.builder()
                                .lookupTypeCode(lookupTypeCode)
                                .permissionCode(perm.trim().toUpperCase())
                                .build());
                    }
                }
            }

            bt.setModifiedBy(principal != null ? principal.getId() : null);
            BankLookupType saved = bankLookupTypeRepository.save(bt);

            if (principal != null) {
                adminAuditService.logAdminAction(
                        principal.getId(), principal.getEmail(), "UPDATE_BANK_LOOKUP_PERMISSIONS",
                        "LOOKUP", bankCode,
                        "Updated permissions for lookup " + lookupTypeCode + " in bank " + bankCode + ": " + request.getPermissions(),
                        null
                );
            }

            log.info("Admin updated permissions for lookup {} in bank {}", lookupTypeCode, bankCode);
            return mapBankTypeToResponse(saved);
        } finally {
            BankContext.setCurrentBank(previousBank != null ? previousBank : BankContext.MASTER_DB_NAME);
            OrganizationContext.setCurrentOrganization(previousOrg != null ? previousOrg : OrganizationContext.MASTER_ORG_ID);
        }
    }

    // =========================================================================
    //  MAPPERS
    // =========================================================================

    public LookupTypeResponse mapToResponse(MasterLookupType entity, boolean includeSubTypes, UserPrincipal principal) {
        List<LookupSubTypeResponse> subTypeResponses = null;
        long count = 0;
        boolean isBank = principal != null && !principal.isPlatformAdmin();
        Long bankId = isBank ? principal.getOrganizationId() : null;

        if (isBank) {
            count = masterLookupSubTypeRepository.countVisibleToBank(entity.getCode(), bankId);
            if (includeSubTypes) {
                subTypeResponses = masterLookupSubTypeRepository.findVisibleToBank(entity.getCode(), bankId).stream()
                        .map(this::mapSubTypeToResponse)
                        .collect(Collectors.toList());
            }
        } else {
            if (entity.getSubTypes() != null && !entity.getSubTypes().isEmpty()) {
                count = entity.getSubTypes().size();
                if (includeSubTypes) {
                    subTypeResponses = entity.getSubTypes().stream()
                            .map(this::mapSubTypeToResponse)
                            .collect(Collectors.toList());
                }
            } else {
                count = masterLookupSubTypeRepository.countByLookupTypeCode(entity.getCode());
                if (includeSubTypes) {
                    subTypeResponses = masterLookupSubTypeRepository.findByLookupTypeCodeOrderByDisplayOrderAscIdAsc(entity.getCode()).stream()
                            .map(this::mapSubTypeToResponse)
                            .collect(Collectors.toList());
                }
            }
        }

        List<com.bank.los.administration.master.entity.MasterLookupTypePermission> perms =
                masterLookupTypePermissionRepository.findByLookupTypeCode(entity.getCode());
        Set<String> permCodes = perms.isEmpty()
                ? new HashSet<>(BankLookupService.DEFAULT_LOOKUP_PERMISSIONS)
                : perms.stream().map(com.bank.los.administration.master.entity.MasterLookupTypePermission::getPermissionCode).collect(Collectors.toSet());

        return LookupTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getDescription())
                .description(entity.getDescription())
                .status(Boolean.TRUE.equals(entity.getIsActive()) ? "ACTIVE" : "INACTIVE")
                .isFixed(entity.getIsFixed())
                .isActive(entity.getIsActive())
                .ownerType(entity.getOwnerType() != null ? entity.getOwnerType() : "INTERNAL_ADMIN")
                .ownerBankId(entity.getOwnerBankId())
                .subTypeCount(count)
                .permissions(permCodes)
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .modifiedBy(entity.getModifiedBy())
                .updatedAt(entity.getUpdatedAt())
                .subTypes(subTypeResponses)
                .build();
    }

    public LookupTypeResponse mapToResponse(MasterLookupType entity, boolean includeSubTypes) {
        return mapToResponse(entity, includeSubTypes, null);
    }

    public LookupTypeResponse mapToResponse(MasterLookupType entity) {
        return mapToResponse(entity, true, null);
    }

    public LookupSubTypeResponse mapSubTypeToResponse(MasterLookupSubType entity) {
        return LookupSubTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getSubTypeCode())
                .typeCode(entity.getLookupTypeCode())
                .lookupTypeCode(entity.getLookupTypeCode())
                .typeDescription(entity.getTypeDescription())
                .subTypeCode(entity.getSubTypeCode())
                .name(entity.getSubTypeDescription())
                .description(entity.getSubTypeDescription())
                .subTypeDescription(entity.getSubTypeDescription())
                .status(Boolean.TRUE.equals(entity.getIsActive()) ? "ACTIVE" : "INACTIVE")
                .isFixed(entity.getIsFixed())
                .isActive(entity.getIsActive())
                .ownerType(entity.getOwnerType() != null ? entity.getOwnerType() : "INTERNAL_ADMIN")
                .ownerBankId(entity.getOwnerBankId())
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

        List<BankLookupTypePermission> perms = bankLookupTypePermissionRepository.findByLookupTypeCode(entity.getCode());
        Set<String> permCodes = perms.isEmpty()
                ? new HashSet<>(BankLookupService.DEFAULT_LOOKUP_PERMISSIONS)
                : perms.stream().map(BankLookupTypePermission::getPermissionCode).collect(Collectors.toSet());

        return LookupTypeResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getDescription())
                .description(entity.getDescription())
                .status(Boolean.TRUE.equals(entity.getIsActive()) ? "ACTIVE" : "INACTIVE")
                .isFixed(entity.getIsFixed())
                .isActive(entity.getIsActive())
                .subTypeCount((long) subTypeResponses.size())
                .permissions(permCodes)
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
                .code(entity.getSubTypeCode())
                .typeCode(entity.getLookupTypeCode())
                .lookupTypeCode(entity.getLookupTypeCode())
                .typeDescription(entity.getTypeDescription())
                .subTypeCode(entity.getSubTypeCode())
                .name(entity.getSubTypeDescription())
                .description(entity.getSubTypeDescription())
                .subTypeDescription(entity.getSubTypeDescription())
                .status(Boolean.TRUE.equals(entity.getIsActive()) ? "ACTIVE" : "INACTIVE")
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
