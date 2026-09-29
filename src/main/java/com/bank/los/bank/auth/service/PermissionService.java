package com.bank.los.bank.auth.service;

import com.bank.los.bank.master.repository.DesignationRoleMappingRepository;
import com.bank.los.bank.master.repository.OrganizationRoleRepository;
import com.bank.los.bank.master.repository.PermissionOverrideRepository;
import com.bank.los.bank.master.repository.PermissionRepository;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.config.BankContext;
import com.bank.los.config.BankDataSourceProvider;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final OrganizationRoleRepository organizationRoleRepository;
    private final PermissionRepository permissionRepository;
    private final BankDataSourceProvider bankDataSourceProvider;
    private final DesignationRoleMappingRepository designationRoleMappingRepository;
    private final PermissionOverrideRepository permissionOverrideRepository;

    public List<String> getPermissionCodes(String roleName, String orgDbName) {
        return getEffectivePermissions(orgDbName, roleName, null);
    }

    public List<String> getEffectivePermissions(String orgDbName, String roleName, String designation) {
        if (roleName == null) return Collections.emptyList();

        if (ApplicationConstants.Roles.INTERNAL_ADMIN.equalsIgnoreCase(roleName)) {
            return getAllInternalAdminPermissions();
        }

        if (orgDbName == null || orgDbName.equalsIgnoreCase(BankContext.MASTER_BANK_ID)) {
            return Collections.emptyList();
        }

        Set<String> effectivePerms = new HashSet<>();

        // 1. Resolve base role permissions
        effectivePerms.addAll(getBaseRolePermissions(orgDbName, roleName));

        // 2. If designation is provided, resolve inherited role permissions
        if (designation != null && !designation.isBlank()) {
            String mappedRole = getMappedRoleForDesignation(orgDbName, designation);
            if (mappedRole != null && !mappedRole.equalsIgnoreCase(roleName)) {
                effectivePerms.addAll(getBaseRolePermissions(orgDbName, mappedRole));
            }
        }

        // 3. Apply ROLE-level overrides
        applyOverrides(orgDbName, "ROLE", roleName, effectivePerms);

        // 4. Apply DESIGNATION-level overrides (takes highest precedence)
        if (designation != null && !designation.isBlank()) {
            applyOverrides(orgDbName, "DESIGNATION", designation, effectivePerms);
        }

        return new ArrayList<>(effectivePerms);
    }

    public boolean hasEffectivePermission(UserPrincipal principal, String permissionCode) {
        if (principal == null || permissionCode == null) return false;

        if (ApplicationConstants.Roles.INTERNAL_ADMIN.equalsIgnoreCase(principal.getRole()) ||
            ApplicationConstants.Roles.SUPER_ADMIN.equalsIgnoreCase(principal.getRole())) {
            return true;
        }

        List<String> effective = getEffectivePermissions(
                principal.getOrganizationDbName(),
                principal.getRole(),
                principal.getDesignation()
        );

        return effective.contains(permissionCode);
    }

    private Set<String> getBaseRolePermissions(String orgDbName, String roleName) {
        Set<String> perms = new HashSet<>();
        DataSource ds = bankDataSourceProvider.getBankDataSource(orgDbName);
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
            log.debug("Notice querying base permissions for role {} in {}: {}", roleName, orgDbName, ex.getMessage());
        }
        return perms;
    }

    private String getMappedRoleForDesignation(String orgDbName, String designation) {
        DataSource ds = bankDataSourceProvider.getBankDataSource(orgDbName);
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
            log.debug("Notice querying mapped role for designation {} in {}: {}", designation, orgDbName, ex.getMessage());
        }
        return null;
    }

    private void applyOverrides(String orgDbName, String targetType, String targetName, Set<String> effectivePerms) {
        DataSource ds = bankDataSourceProvider.getBankDataSource(orgDbName);
        if (ds == null) return;

        String sql = "SELECT permission_code, effect FROM identity.permission_overrides " +
                     "WHERE UPPER(target_type) = ? AND LOWER(target_name) = LOWER(?) AND is_active = true";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, targetType.toUpperCase());
            ps.setString(2, targetName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String permCode = rs.getString("permission_code");
                    String effect = rs.getString("effect");
                    if ("ALLOW".equalsIgnoreCase(effect)) {
                        effectivePerms.add(permCode);
                    } else if ("DENY".equalsIgnoreCase(effect)) {
                        effectivePerms.remove(permCode);
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("Notice applying permission overrides for {} {} in {}: {}", targetType, targetName, orgDbName, ex.getMessage());
        }
    }

    private List<String> getAllInternalAdminPermissions() {
        return List.of(
                ApplicationConstants.Permissions.ORGANIZATION_CREATE,
                ApplicationConstants.Permissions.ORGANIZATION_VIEW,
                ApplicationConstants.Permissions.ORGANIZATION_UPDATE,
                ApplicationConstants.Permissions.SYSTEM_AUDIT_VIEW,
                ApplicationConstants.Permissions.LOOKUP_MASTER_VIEW,
                ApplicationConstants.Permissions.LOOKUP_BANK_VIEW,
                ApplicationConstants.Permissions.LOOKUP_BANK_ADD,
                ApplicationConstants.Permissions.LOOKUP_BANK_ADD_FROM_MASTER,
                ApplicationConstants.Permissions.LOOKUP_BANK_EDIT,
                ApplicationConstants.Permissions.LOOKUP_BANK_DELETE,
                ApplicationConstants.Permissions.LOOKUP_BANK_ACTIVATE,
                ApplicationConstants.Permissions.LOOKUP_BANK_DEACTIVATE,
                ApplicationConstants.Permissions.ROLE_PERMISSION_MANAGE,
                ApplicationConstants.Permissions.DASHBOARD_VIEW,
                ApplicationConstants.Permissions.DASHBOARD_ANALYTICS_VIEW
        );
    }
}
