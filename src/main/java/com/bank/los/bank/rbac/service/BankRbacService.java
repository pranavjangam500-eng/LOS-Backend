package com.bank.los.bank.rbac.service;

import com.bank.los.bank.audit.service.BankAuditService;
import com.bank.los.bank.auth.service.PermissionService;
import com.bank.los.bank.master.entity.DesignationRoleMapping;
import com.bank.los.bank.master.entity.OrganizationRole;
import com.bank.los.bank.master.entity.Permission;
import com.bank.los.bank.master.entity.PermissionOverride;
import com.bank.los.bank.master.repository.DesignationRoleMappingRepository;
import com.bank.los.bank.master.repository.OrganizationRoleRepository;
import com.bank.los.bank.master.repository.PermissionOverrideRepository;
import com.bank.los.bank.master.repository.PermissionRepository;
import com.bank.los.bank.rbac.dto.*;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.BankContext;
import com.bank.los.config.BankDataSourceProvider;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankRbacService {

    private final OrganizationRoleRepository organizationRoleRepository;
    private final PermissionRepository permissionRepository;
    private final DesignationRoleMappingRepository designationRoleMappingRepository;
    private final PermissionOverrideRepository permissionOverrideRepository;
    private final BankDataSourceProvider bankDataSourceProvider;
    private final PermissionService permissionService;
    private final BankAuditService bankAuditService;

    // =========================================================================
    //  PERMISSIONS QUERY
    // =========================================================================

    public List<Permission> getAllPermissions(UserPrincipal principal) {
        setTenantContext(principal);
        return permissionRepository.findAll();
    }

    // =========================================================================
    //  ROLES & INHERITED PERMISSIONS
    // =========================================================================

    public List<BankRolePermissionResponse> getAllRolesWithPermissions(String orgDb) {
        setContext(orgDb);

        List<OrganizationRole> roles = organizationRoleRepository.findAll();
        List<BankRolePermissionResponse> result = new ArrayList<>();

        for (OrganizationRole role : roles) {
            List<String> basePerms = getBaseRolePermissions(orgDb, role.getName());
            List<PermissionOverrideResponse> overrides = getOverridesForTarget(orgDb, "ROLE", role.getName());
            List<String> effective = permissionService.getEffectivePermissions(orgDb, role.getName(), null);

            result.add(BankRolePermissionResponse.builder()
                    .roleId(role.getId())
                    .roleName(role.getName())
                    .panel(role.getPanel())
                    .description(role.getDescription())
                    .basePermissions(basePerms)
                    .overrides(overrides)
                    .effectivePermissions(effective)
                    .build());
        }
        return result;
    }

    // =========================================================================
    //  DESIGNATION → ROLE MAPPINGS
    // =========================================================================

    public List<DesignationRoleMappingResponse> getAllDesignationMappings(String orgDb) {
        setContext(orgDb);

        List<DesignationRoleMapping> mappings = designationRoleMappingRepository.findAll();
        List<DesignationRoleMappingResponse> result = new ArrayList<>();

        for (DesignationRoleMapping m : mappings) {
            String roleName = m.getRole().getName();
            List<String> inherited = getBaseRolePermissions(orgDb, roleName);
            List<PermissionOverrideResponse> overrides = getOverridesForTarget(orgDb, "DESIGNATION", m.getDesignation());
            List<String> effective = permissionService.getEffectivePermissions(orgDb, roleName, m.getDesignation());

            result.add(DesignationRoleMappingResponse.builder()
                    .id(m.getId())
                    .designation(m.getDesignation())
                    .roleId(m.getRole().getId())
                    .roleName(roleName)
                    .inheritedPermissions(inherited)
                    .overrides(overrides)
                    .effectivePermissions(effective)
                    .isActive(m.getIsActive())
                    .createdAt(m.getCreatedAt())
                    .updatedAt(m.getUpdatedAt())
                    .build());
        }
        return result;
    }

    @Transactional
    public DesignationRoleMappingResponse createOrUpdateDesignationMapping(DesignationRoleMappingRequest request, UserPrincipal principal) {
        setTenantContext(principal);

        String designation = request.getDesignation().trim();
        OrganizationRole role = organizationRoleRepository.findByName(request.getRoleName().trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + request.getRoleName()));

        DesignationRoleMapping mapping = designationRoleMappingRepository.findByDesignationIgnoreCase(designation)
                .orElseGet(() -> DesignationRoleMapping.builder()
                        .designation(designation)
                        .createdBy(principal != null ? principal.getId() : null)
                        .build());

        mapping.setRole(role);
        if (request.getIsActive() != null) {
            mapping.setIsActive(request.getIsActive());
        }
        mapping.setModifiedBy(principal != null ? principal.getId() : null);

        DesignationRoleMapping saved = designationRoleMappingRepository.save(mapping);
        log.info("Mapped Designation '{}' to Role '{}' in org={}", saved.getDesignation(), role.getName(), principal != null ? principal.getOrganizationDbName() : "DB");

        if (principal != null) {
            bankAuditService.logAction(
                    principal, "MAPPING_DESIGNATION_ROLE", "RBAC",
                    "Mapped designation " + saved.getDesignation() + " to role " + role.getName(), null
            );
        }

        String orgDb = principal != null ? principal.getOrganizationDbName() : BankContext.getCurrentBank();
        List<String> inherited = getBaseRolePermissions(orgDb, role.getName());
        List<PermissionOverrideResponse> overrides = getOverridesForTarget(orgDb, "DESIGNATION", saved.getDesignation());
        List<String> effective = permissionService.getEffectivePermissions(orgDb, role.getName(), saved.getDesignation());

        return DesignationRoleMappingResponse.builder()
                .id(saved.getId())
                .designation(saved.getDesignation())
                .roleId(role.getId())
                .roleName(role.getName())
                .inheritedPermissions(inherited)
                .overrides(overrides)
                .effectivePermissions(effective)
                .isActive(saved.getIsActive())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteDesignationMapping(String designation, UserPrincipal principal) {
        setTenantContext(principal);

        DesignationRoleMapping mapping = designationRoleMappingRepository.findByDesignationIgnoreCase(designation.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Designation mapping not found for: " + designation));

        designationRoleMappingRepository.delete(mapping);
        log.info("Deleted Designation mapping '{}'", designation);

        if (principal != null) {
            bankAuditService.logAction(
                    principal, "DELETE_DESIGNATION_MAPPING", "RBAC",
                    "Deleted designation mapping for " + designation, null
            );
        }
    }

    // =========================================================================
    //  PERMISSION OVERRIDES (ALLOW / DENY)
    // =========================================================================

    public List<PermissionOverrideResponse> getAllPermissionOverrides(String orgDb) {
        setContext(orgDb);
        return permissionOverrideRepository.findAll().stream()
                .map(this::mapOverrideToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PermissionOverrideResponse createOrUpdatePermissionOverride(PermissionOverrideRequest request, UserPrincipal principal) {
        setTenantContext(principal);

        String targetType = request.getTargetType().trim().toUpperCase();
        String targetName = request.getTargetName().trim();
        String permCode = request.getPermissionCode().trim().toUpperCase();
        String effect = request.getEffect().trim().toUpperCase();

        if (!"ALLOW".equals(effect) && !"DENY".equals(effect)) {
            throw new BusinessException("Override effect must be either 'ALLOW' or 'DENY'.");
        }

        if (!"ROLE".equals(targetType) && !"DESIGNATION".equals(targetType)) {
            throw new BusinessException("Override targetType must be either 'ROLE' or 'DESIGNATION'.");
        }

        // Verify permission code exists
        if (permissionRepository.findByCode(permCode).isEmpty()) {
            throw new ResourceNotFoundException("Permission not found with code: " + permCode);
        }

        PermissionOverride override = permissionOverrideRepository
                .findByTargetTypeAndTargetNameIgnoreCaseAndPermissionCodeIgnoreCase(targetType, targetName, permCode)
                .orElseGet(() -> PermissionOverride.builder()
                        .targetType(targetType)
                        .targetName(targetName)
                        .permissionCode(permCode)
                        .createdBy(principal != null ? principal.getId() : null)
                        .build());

        override.setEffect(effect);
        override.setReason(request.getReason());
        if (request.getIsActive() != null) {
            override.setIsActive(request.getIsActive());
        }
        override.setModifiedBy(principal != null ? principal.getId() : null);

        PermissionOverride saved = permissionOverrideRepository.save(override);
        log.info("Saved Permission Override: {} {} -> {} [{}] in org={}",
                targetType, targetName, permCode, effect, principal != null ? principal.getOrganizationDbName() : "DB");

        if (principal != null) {
            bankAuditService.logAction(
                    principal, "SET_PERMISSION_OVERRIDE", "RBAC",
                    "Set override on " + targetType + " " + targetName + ": " + permCode + " -> " + effect + " (Reason: " + request.getReason() + ")", null
            );
        }

        return mapOverrideToResponse(saved);
    }

    @Transactional
    public void deletePermissionOverride(Long id, UserPrincipal principal) {
        setTenantContext(principal);

        PermissionOverride override = permissionOverrideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission override not found with ID: " + id));

        permissionOverrideRepository.delete(override);
        log.info("Deleted Permission Override ID={}", id);

        if (principal != null) {
            bankAuditService.logAction(
                    principal, "DELETE_PERMISSION_OVERRIDE", "RBAC",
                    "Deleted override ID " + id + " (" + override.getTargetType() + " " + override.getTargetName() + " - " + override.getPermissionCode() + ")", null
            );
        }
    }

    public EffectivePermissionResponse getEffectivePermissionsForTarget(String targetType, String targetName, UserPrincipal principal) {
        setTenantContext(principal);
        String orgDb = principal != null ? principal.getOrganizationDbName() : BankContext.getCurrentBank();

        String roleName = null;
        String designation = null;
        List<String> basePerms = new ArrayList<>();

        if ("ROLE".equalsIgnoreCase(targetType)) {
            roleName = targetName;
            basePerms = getBaseRolePermissions(orgDb, roleName);
        } else if ("DESIGNATION".equalsIgnoreCase(targetType)) {
            designation = targetName;
            String mappedRole = getMappedRoleForDesignation(orgDb, designation);
            if (mappedRole != null) {
                roleName = mappedRole;
                basePerms = getBaseRolePermissions(orgDb, mappedRole);
            }
        }

        List<PermissionOverrideResponse> overrides = getOverridesForTarget(orgDb, targetType, targetName);
        List<String> effective = permissionService.getEffectivePermissions(orgDb, roleName, designation);

        return EffectivePermissionResponse.builder()
                .targetType(targetType.toUpperCase())
                .targetName(targetName)
                .roleName(roleName)
                .designation(designation)
                .basePermissions(basePerms)
                .overrides(overrides)
                .effectivePermissions(effective)
                .build();
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private void setTenantContext(UserPrincipal principal) {
        if (principal != null && principal.getOrganizationDbName() != null) {
            setContext(principal.getOrganizationDbName());
        }
    }

    private void setContext(String orgDb) {
        if (orgDb != null) {
            BankContext.setCurrentBank(orgDb);
            OrganizationContext.setCurrentOrganization(orgDb);
        }
    }

    private List<String> getBaseRolePermissions(String orgDb, String roleName) {
        List<String> perms = new ArrayList<>();
        DataSource ds = bankDataSourceProvider.getBankDataSource(orgDb);
        if (ds == null) return perms;

        String sql = "SELECT p.code FROM identity.role_permissions rp " +
                     "JOIN identity.roles r ON rp.role_id = r.id " +
                     "JOIN identity.permissions p ON rp.permission_id = p.id " +
                     "WHERE UPPER(r.name) = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    perms.add(rs.getString("code"));
                }
            }
        } catch (Exception ex) {
            log.debug("Notice querying permissions for role {} in {}: {}", roleName, orgDb, ex.getMessage());
        }
        return perms;
    }

    private String getMappedRoleForDesignation(String orgDb, String designation) {
        DataSource ds = bankDataSourceProvider.getBankDataSource(orgDb);
        if (ds == null) return null;

        String sql = "SELECT r.name FROM identity.designation_role_mappings drm " +
                     "JOIN identity.roles r ON drm.role_id = r.id " +
                     "WHERE LOWER(drm.designation) = LOWER(?) AND drm.is_active = true";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, designation.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (Exception ex) {
            log.debug("Notice querying mapped role for designation {} in {}: {}", designation, orgDb, ex.getMessage());
        }
        return null;
    }

    private List<PermissionOverrideResponse> getOverridesForTarget(String orgDb, String targetType, String targetName) {
        setContext(orgDb);
        return permissionOverrideRepository.findByTargetTypeAndTargetNameIgnoreCase(targetType, targetName).stream()
                .map(this::mapOverrideToResponse)
                .collect(Collectors.toList());
    }

    private PermissionOverrideResponse mapOverrideToResponse(PermissionOverride po) {
        return PermissionOverrideResponse.builder()
                .id(po.getId())
                .targetType(po.getTargetType())
                .targetName(po.getTargetName())
                .permissionCode(po.getPermissionCode())
                .effect(po.getEffect())
                .reason(po.getReason())
                .isActive(po.getIsActive())
                .createdBy(po.getCreatedBy())
                .createdAt(po.getCreatedAt())
                .modifiedBy(po.getModifiedBy())
                .updatedAt(po.getUpdatedAt())
                .build();
    }
}
