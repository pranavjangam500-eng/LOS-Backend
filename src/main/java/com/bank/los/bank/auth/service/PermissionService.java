package com.bank.los.bank.auth.service;

import com.bank.los.bank.master.entity.OrganizationRole;
import com.bank.los.bank.master.repository.OrganizationRoleRepository;
import com.bank.los.bank.master.repository.PermissionRepository;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.config.BankContext;
import com.bank.los.config.BankDataSourceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final OrganizationRoleRepository organizationRoleRepository;
    private final PermissionRepository permissionRepository;
    private final BankDataSourceProvider bankDataSourceProvider;

    public List<String> getPermissionCodes(String roleName, String orgDbName) {
        if (roleName == null) return Collections.emptyList();

        if (ApplicationConstants.Roles.INTERNAL_ADMIN.equalsIgnoreCase(roleName)) {
            return List.of(
                    "ORGANIZATION_CREATE", "ORGANIZATION_VIEW", "ORGANIZATION_UPDATE",
                    "ADMIN_USER_CREATE", "ADMIN_USER_VIEW",
                    "SYSTEM_MONITOR_VIEW", "DASHBOARD_VIEW"
            );
        }

        if (orgDbName == null || orgDbName.equalsIgnoreCase(BankContext.MASTER_BANK_ID)) {
            return Collections.emptyList();
        }

        DataSource ds = bankDataSourceProvider.getBankDataSource(orgDbName);
        if (ds == null) {
            return Collections.emptyList();
        }

        List<String> perms = new ArrayList<>();
        String sql = "SELECT p.code FROM identity.role_permissions rp " +
                     "JOIN identity.roles r ON rp.role_id = r.id " +
                     "JOIN identity.permissions p ON rp.permission_id = p.id " +
                     "WHERE r.name = ?";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    perms.add(rs.getString("code"));
                }
            }
        } catch (Exception ex) {
            log.debug("Notice querying permissions for role {} in {}: {}", roleName, orgDbName, ex.getMessage());
        }

        return perms;
    }
}
