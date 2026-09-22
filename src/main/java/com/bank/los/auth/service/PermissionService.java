package com.bank.los.auth.service;

import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.config.TenantContext;
import com.bank.los.tenant.entity.Permission;
import com.bank.los.tenant.repository.PermissionRepository;
import com.bank.los.tenant.repository.TenantRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads role permissions from the DB at login time.
 * The bank ADMIN can update role_permissions at runtime — changes reflect on next login.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final TenantRoleRepository tenantRoleRepository;

    /**
     * Returns permission codes for a given role from the current tenant DB.
     * Falls back to empty list if role not found (safe default).
     */
    public List<String> getPermissionCodes(String roleName, String tenantDb) {
        if (roleName == null || tenantDb == null) return Collections.emptyList();

        // INTERNAL_ADMIN gets all system-level permissions (not stored per-role in tenant DB)
        if (ApplicationConstants.Roles.INTERNAL_ADMIN.equalsIgnoreCase(roleName)) {
            return List.of(
                    ApplicationConstants.Permissions.ORGANIZATION_CREATE,
                    ApplicationConstants.Permissions.ORGANIZATION_VIEW,
                    ApplicationConstants.Permissions.ORGANIZATION_UPDATE,
                    ApplicationConstants.Permissions.SYSTEM_AUDIT_VIEW,
                    ApplicationConstants.Permissions.DASHBOARD_VIEW,
                    ApplicationConstants.Permissions.DASHBOARD_ANALYTICS_VIEW
            );
        }

        try {
            TenantContext.setCurrentTenant(tenantDb);
            return tenantRoleRepository.findByName(roleName)
                    .map(role -> permissionRepository.findByRoleId(role.getId())
                            .stream()
                            .map(Permission::getCode)
                            .collect(Collectors.toList()))
                    .orElseGet(Collections::emptyList);
        } catch (Exception e) {
            log.warn("Could not load permissions for role={} from db={}: {}", roleName, tenantDb, e.getMessage());
            return Collections.emptyList();
        }
    }
}
