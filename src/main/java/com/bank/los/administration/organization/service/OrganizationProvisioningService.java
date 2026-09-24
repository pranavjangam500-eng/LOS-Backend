package com.bank.los.administration.organization.service;

import com.bank.los.bank.master.entity.Branch;
import com.bank.los.bank.master.entity.OrganizationRole;
import com.bank.los.bank.master.entity.Permission;
import com.bank.los.bank.master.repository.BranchRepository;
import com.bank.los.bank.master.repository.OrganizationRoleRepository;
import com.bank.los.bank.master.repository.PermissionRepository;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.config.BankContext;
import com.bank.los.config.BankDataSourceProvider;
import com.bank.los.config.OrganizationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * Automates schema creation, role initialization, permissions seeding,
 * and primary branch setup whenever a new Bank/NBFC Organization is onboarded.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationProvisioningService {

    private final BankDataSourceProvider bankDataSourceProvider;
    private final OrganizationRoleRepository organizationRoleRepository;
    private final BranchRepository branchRepository;
    private final PermissionRepository permissionRepository;

    public void provisionOrganization(String orgDb, String orgCode, String orgName, String host, Integer port) {
        log.info("Provisioning bank database and default entities for orgCode={}, db={}", orgCode, orgDb);
        String previousOrg = BankContext.getCurrentBank();
        String previousCode = BankContext.getCurrentBankCode();
        try {
            // 1. Create/Connect DataSource and execute schema scripts
            bankDataSourceProvider.getOrCreateBankDataSource(orgDb, host, port);

            // 2. Set context to the new Bank DB
            BankContext.setCurrentBank(orgDb);
            BankContext.setCurrentBankCode(orgCode);

            // 3. Seed default roles
            OrganizationRole adminRole = seedRole("ADMIN", "BANK_NBFC", "Bank/NBFC internal admin — configures org and manages users");
            OrganizationRole makerRole = seedRole("MAKER", "BANK_NBFC", "Creates and initiates loan records for Checker approval");
            OrganizationRole checkerRole = seedRole("CHECKER", "BANK_NBFC", "Reviews and approves Maker actions");
            OrganizationRole viewerRole = seedRole("VIEWER", "BANK_NBFC", "Read-only access");
            seedRole("CUSTOMER", "CUSTOMER", "Loan applicant");

            // 4. Seed default branch
            String branchCode = orgCode + "-BR-01";
            if (branchRepository.findByCode(branchCode).isEmpty()) {
                branchRepository.save(Branch.builder()
                        .name(orgName != null ? orgName + " Main Branch" : "Main Branch")
                        .code(branchCode)
                        .address("Headquarters")
                        .city("Mumbai")
                        .state("Maharashtra")
                        .pincode("400001")
                        .status("ACTIVE")
                        .build());
            }

            // 5. Seed permissions & role mappings
            seedPermissions();
            seedRolePermissions(orgDb, adminRole, List.of(
                    ApplicationConstants.Permissions.USER_CREATE,
                    ApplicationConstants.Permissions.USER_UPDATE,
                    ApplicationConstants.Permissions.USER_VIEW,
                    ApplicationConstants.Permissions.USER_DEACTIVATE,
                    ApplicationConstants.Permissions.USER_VERIFY,
                    ApplicationConstants.Permissions.USER_RESET_PASSWORD,
                    ApplicationConstants.Permissions.BRANCH_CREATE,
                    ApplicationConstants.Permissions.BRANCH_UPDATE,
                    ApplicationConstants.Permissions.BRANCH_VIEW,
                    ApplicationConstants.Permissions.REPORT_VIEW,
                    ApplicationConstants.Permissions.REPORT_EXPORT,
                    ApplicationConstants.Permissions.DASHBOARD_VIEW,
                    ApplicationConstants.Permissions.DASHBOARD_ANALYTICS_VIEW,
                    ApplicationConstants.Permissions.ROLE_PERMISSION_MANAGE
            ));

            seedRolePermissions(orgDb, makerRole, List.of(
                    ApplicationConstants.Permissions.LOAN_APPLICATION_CREATE,
                    ApplicationConstants.Permissions.LOAN_APPLICATION_EDIT,
                    ApplicationConstants.Permissions.LOAN_APPLICATION_VIEW,
                    ApplicationConstants.Permissions.LOAN_APPLICATION_SUBMIT,
                    ApplicationConstants.Permissions.CUSTOMER_CREATE,
                    ApplicationConstants.Permissions.CUSTOMER_EDIT,
                    ApplicationConstants.Permissions.CUSTOMER_VIEW,
                    ApplicationConstants.Permissions.DOCUMENT_UPLOAD,
                    ApplicationConstants.Permissions.DOCUMENT_VIEW,
                    ApplicationConstants.Permissions.DASHBOARD_VIEW
            ));

            seedRolePermissions(orgDb, checkerRole, List.of(
                    ApplicationConstants.Permissions.LOAN_APPLICATION_VIEW,
                    ApplicationConstants.Permissions.LOAN_APPLICATION_VERIFY,
                    ApplicationConstants.Permissions.LOAN_APPLICATION_APPROVE,
                    ApplicationConstants.Permissions.LOAN_APPLICATION_REJECT,
                    ApplicationConstants.Permissions.CUSTOMER_VIEW,
                    ApplicationConstants.Permissions.CUSTOMER_VIEW_ALL,
                    ApplicationConstants.Permissions.DOCUMENT_VERIFY,
                    ApplicationConstants.Permissions.DOCUMENT_VIEW,
                    ApplicationConstants.Permissions.DASHBOARD_VIEW
            ));

            seedRolePermissions(orgDb, viewerRole, List.of(
                    ApplicationConstants.Permissions.LOAN_APPLICATION_VIEW,
                    ApplicationConstants.Permissions.CUSTOMER_VIEW,
                    ApplicationConstants.Permissions.CUSTOMER_VIEW_ALL,
                    ApplicationConstants.Permissions.DOCUMENT_VIEW,
                    ApplicationConstants.Permissions.REPORT_VIEW,
                    ApplicationConstants.Permissions.REPORT_EXPORT,
                    ApplicationConstants.Permissions.DASHBOARD_VIEW,
                    ApplicationConstants.Permissions.DASHBOARD_ANALYTICS_VIEW
            ));

            log.info("Organization DB provisioning completed successfully for orgCode={}", orgCode);
        } catch (Exception ex) {
            log.warn("Notice during organization provisioning for {}: {}", orgCode, ex.getMessage());
        } finally {
            if (previousOrg != null) {
                OrganizationContext.setCurrentOrganization(previousOrg);
            } else {
                OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
            }
            if (previousCode != null) {
                OrganizationContext.setCurrentOrgCode(previousCode);
            }
        }
    }

    // Alias for backward compatibility
    public void provisionTenant(String tenantDb, String orgCode, String orgName, String host, Integer port) {
        provisionOrganization(tenantDb, orgCode, orgName, host, port);
    }

    private OrganizationRole seedRole(String name, String panel, String description) {
        return organizationRoleRepository.findByName(name)
                .orElseGet(() -> organizationRoleRepository.save(OrganizationRole.builder()
                        .name(name)
                        .panel(panel)
                        .description(description)
                        .build()));
    }

    private void seedPermissions() {
        List<Object[]> perms = List.of(
                new Object[]{ApplicationConstants.Permissions.USER_CREATE, "Create new users", ApplicationConstants.PermissionModules.USER},
                new Object[]{ApplicationConstants.Permissions.USER_UPDATE, "Update user details", ApplicationConstants.PermissionModules.USER},
                new Object[]{ApplicationConstants.Permissions.USER_VIEW, "View user details", ApplicationConstants.PermissionModules.USER},
                new Object[]{ApplicationConstants.Permissions.USER_DEACTIVATE, "Deactivate a user", ApplicationConstants.PermissionModules.USER},
                new Object[]{ApplicationConstants.Permissions.USER_VERIFY, "Verify pending user (2nd admin)", ApplicationConstants.PermissionModules.USER},
                new Object[]{ApplicationConstants.Permissions.USER_RESET_PASSWORD, "Reset a user's password", ApplicationConstants.PermissionModules.USER},
                new Object[]{ApplicationConstants.Permissions.BRANCH_CREATE, "Create branches", ApplicationConstants.PermissionModules.BRANCH},
                new Object[]{ApplicationConstants.Permissions.BRANCH_UPDATE, "Update branch details", ApplicationConstants.PermissionModules.BRANCH},
                new Object[]{ApplicationConstants.Permissions.BRANCH_VIEW, "View branches", ApplicationConstants.PermissionModules.BRANCH},
                new Object[]{ApplicationConstants.Permissions.LOAN_APPLICATION_CREATE, "Create loan applications", ApplicationConstants.PermissionModules.LOAN},
                new Object[]{ApplicationConstants.Permissions.LOAN_APPLICATION_EDIT, "Edit loan applications", ApplicationConstants.PermissionModules.LOAN},
                new Object[]{ApplicationConstants.Permissions.LOAN_APPLICATION_VIEW, "View loan applications", ApplicationConstants.PermissionModules.LOAN},
                new Object[]{ApplicationConstants.Permissions.LOAN_APPLICATION_SUBMIT, "Submit loan applications", ApplicationConstants.PermissionModules.LOAN},
                new Object[]{ApplicationConstants.Permissions.LOAN_APPLICATION_VERIFY, "Verify loan applications", ApplicationConstants.PermissionModules.LOAN},
                new Object[]{ApplicationConstants.Permissions.LOAN_APPLICATION_APPROVE, "Approve loan applications", ApplicationConstants.PermissionModules.LOAN},
                new Object[]{ApplicationConstants.Permissions.LOAN_APPLICATION_REJECT, "Reject loan applications", ApplicationConstants.PermissionModules.LOAN},
                new Object[]{ApplicationConstants.Permissions.CUSTOMER_CREATE, "Create customer records", ApplicationConstants.PermissionModules.CUSTOMER},
                new Object[]{ApplicationConstants.Permissions.CUSTOMER_EDIT, "Edit customer details", ApplicationConstants.PermissionModules.CUSTOMER},
                new Object[]{ApplicationConstants.Permissions.CUSTOMER_VIEW, "View own customers", ApplicationConstants.PermissionModules.CUSTOMER},
                new Object[]{ApplicationConstants.Permissions.CUSTOMER_VIEW_ALL, "View all customers in org", ApplicationConstants.PermissionModules.CUSTOMER},
                new Object[]{ApplicationConstants.Permissions.DOCUMENT_UPLOAD, "Upload documents", ApplicationConstants.PermissionModules.DOCUMENT},
                new Object[]{ApplicationConstants.Permissions.DOCUMENT_VERIFY, "Verify documents", ApplicationConstants.PermissionModules.DOCUMENT},
                new Object[]{ApplicationConstants.Permissions.DOCUMENT_VIEW, "View documents", ApplicationConstants.PermissionModules.DOCUMENT},
                new Object[]{ApplicationConstants.Permissions.REPORT_VIEW, "View reports", ApplicationConstants.PermissionModules.REPORT},
                new Object[]{ApplicationConstants.Permissions.REPORT_EXPORT, "Export reports", ApplicationConstants.PermissionModules.REPORT},
                new Object[]{ApplicationConstants.Permissions.DASHBOARD_VIEW, "View dashboard", ApplicationConstants.PermissionModules.DASHBOARD},
                new Object[]{ApplicationConstants.Permissions.DASHBOARD_ANALYTICS_VIEW, "View dashboard analytics", ApplicationConstants.PermissionModules.DASHBOARD},
                new Object[]{ApplicationConstants.Permissions.ROLE_PERMISSION_MANAGE, "Manage role permissions", ApplicationConstants.PermissionModules.SYSTEM}
        );

        for (Object[] p : perms) {
            String code = (String) p[0];
            if (permissionRepository.findByCode(code).isEmpty()) {
                permissionRepository.save(Permission.builder()
                        .code(code)
                        .description((String) p[1])
                        .module((String) p[2])
                        .build());
            }
        }
    }

    private void seedRolePermissions(String orgDb, OrganizationRole role, List<String> permCodes) {
        if (role == null || permCodes == null) return;
        DataSource ds = bankDataSourceProvider.getBankDataSource(orgDb);
        if (ds == null) return;

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(true);
            for (String code : permCodes) {
                permissionRepository.findByCode(code).ifPresent(p -> {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO identity.role_permissions (role_id, permission_id) " +
                            "SELECT " + role.getId() + ", " + p.getId() + " WHERE NOT EXISTS (" +
                            "SELECT 1 FROM identity.role_permissions WHERE role_id = " + role.getId() + " AND permission_id = " + p.getId() + ")")) {
                        ps.executeUpdate();
                    } catch (Exception ex) {
                        log.debug("Notice seeding permission {} for role {}: {}", code, role.getName(), ex.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            log.debug("Notice opening connection to seed permissions for role {}: {}", role.getName(), e.getMessage());
        }
    }
}
