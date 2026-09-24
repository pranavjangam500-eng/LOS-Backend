package com.bank.los.db.init;

import com.bank.los.administration.master.entity.InternalUser;
import com.bank.los.administration.master.entity.LoginDirectory;
import com.bank.los.administration.master.entity.MasterRole;
import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.InternalUserRepository;
import com.bank.los.administration.master.repository.LoginDirectoryRepository;
import com.bank.los.administration.master.repository.MasterRoleRepository;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.bank.master.entity.Branch;
import com.bank.los.bank.master.entity.Customer;
import com.bank.los.bank.master.entity.OrganizationRole;
import com.bank.los.bank.master.entity.OrganizationUser;
import com.bank.los.bank.master.entity.Permission;
import com.bank.los.bank.master.repository.BranchRepository;
import com.bank.los.bank.master.repository.CustomerRepository;
import com.bank.los.bank.master.repository.OrganizationRoleRepository;
import com.bank.los.bank.master.repository.OrganizationUserRepository;
import com.bank.los.bank.master.repository.PermissionRepository;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.config.BankContext;
import com.bank.los.config.BankDataSourceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final MasterRoleRepository masterRoleRepository;
    private final OrganizationRepository organizationRepository;
    private final InternalUserRepository internalUserRepository;
    private final LoginDirectoryRepository loginDirectoryRepository;

    private final OrganizationRoleRepository organizationRoleRepository;
    private final BranchRepository branchRepository;
    private final OrganizationUserRepository organizationUserRepository;
    private final CustomerRepository customerRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankDataSourceProvider bankDataSourceProvider;

    @Override
    public void run(String... args) {
        log.info("Checking database initialization and seed data...");
        try {
            seedMasterDatabase();
            seedOrganizationDatabase("los_hdfc01_db", "HDFC01");
            seedOrganizationDatabase("los_bajaj02_db", "BAJAJ02");
            log.info("Database seeding completed successfully.");
        } catch (Exception e) {
            log.warn("Notice during seeding: {}", e.getMessage());
        } finally {
            BankContext.clear();
        }
    }

    // =====================================================================
    //  MASTER DATABASE (Administration Panel)
    // =====================================================================

    private void seedMasterDatabase() {
        BankContext.setCurrentBank(BankContext.MASTER_DB_NAME);

        // Internal platform admin role
        MasterRole adminRole = masterRoleRepository.findByName(ApplicationConstants.Roles.INTERNAL_ADMIN)
                .orElseGet(() -> masterRoleRepository.save(MasterRole.builder()
                        .name(ApplicationConstants.Roles.INTERNAL_ADMIN)
                        .panel(ApplicationConstants.Panels.INTERNAL)
                        .description("Platform team; manages organizations and system features")
                        .build()));

        // Sample organizations
        Organization hdfc = organizationRepository.findByCode("HDFC01")
                .orElseGet(() -> organizationRepository.save(Organization.builder()
                        .name("HDFC Bank")
                        .code("HDFC01")
                        .type("BANK")
                        .status("ACTIVE")
                        .contactEmail("contact@hdfcbank.com")
                        .contactPhone("+912261606161")
                        .dbName("los_hdfc01_db")
                        .dbHost("localhost")
                        .dbPort(5432)
                        .build()));

        Organization bajaj = organizationRepository.findByCode("BAJAJ02")
                .orElseGet(() -> organizationRepository.save(Organization.builder()
                        .name("Bajaj Finance Limited")
                        .code("BAJAJ02")
                        .type("NBFC")
                        .status("ACTIVE")
                        .contactEmail("customercare@bajajfinserv.in")
                        .contactPhone("+912071576403")
                        .dbName("los_bajaj02_db")
                        .dbHost("localhost")
                        .dbPort(5432)
                        .build()));

        // Super Admin user (platform team)
        if (internalUserRepository.findByEmail("admin@losplatform.com").isEmpty()) {
            InternalUser internalAdmin = internalUserRepository.save(InternalUser.builder()
                    .empNo("EMP-MST-001")
                    .username("superadmin")
                    .role(adminRole)
                    .firstName("Super")
                    .middleName("")
                    .lastName("Administrator")
                    .email("admin@losplatform.com")
                    .mobile("+919999900000")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .isActive(true)
                    .status(ApplicationConstants.UserStatus.OPERATIVE)
                    .loginOnHolidays(true)
                    .inactiveSessionTimeout(3600)
                    .build());

            loginDirectoryRepository.save(LoginDirectory.builder()
                    .userCode(internalAdmin.getEmpNo())
                    .email(internalAdmin.getEmail())
                    .phone(internalAdmin.getMobile())
                    .organization(hdfc)
                    .userType(ApplicationConstants.UserTypes.INTERNAL)
                    .build());
        }

        // Login directory entries for HDFC demo staff
        registerLoginDirectoryEntry("admin@hdfcbank.com", "EMP-HDFC-001", "+919876500001", hdfc, ApplicationConstants.UserTypes.STAFF);
        registerLoginDirectoryEntry("maker@hdfcbank.com", "EMP-HDFC-002", "+919876500002", hdfc, ApplicationConstants.UserTypes.STAFF);
        registerLoginDirectoryEntry("checker@hdfcbank.com", "EMP-HDFC-003", "+919876500003", hdfc, ApplicationConstants.UserTypes.STAFF);
        registerLoginDirectoryEntry("viewer@hdfcbank.com", "EMP-HDFC-004", "+919876500004", hdfc, ApplicationConstants.UserTypes.STAFF);
        registerLoginDirectoryEntry("rajesh.kumar@gmail.com", "CUST-HDFC-1001", "+919876500005", hdfc, ApplicationConstants.UserTypes.CUSTOMER);

        // Login directory entries for Bajaj demo staff
        registerLoginDirectoryEntry("admin@bajajfinance.com", "EMP-BJ-001", "+919876500010", bajaj, ApplicationConstants.UserTypes.STAFF);
    }

    private void registerLoginDirectoryEntry(String email, String userCode, String phone, Organization org, String userType) {
        if (loginDirectoryRepository.findByEmail(email).isEmpty()) {
            loginDirectoryRepository.save(LoginDirectory.builder()
                    .email(email)
                    .userCode(userCode)
                    .phone(phone)
                    .organization(org)
                    .userType(userType)
                    .build());
        }
    }

    // =====================================================================
    //  ORGANIZATION DATABASE (Bank / NBFC Panel)
    // =====================================================================

    private void seedOrganizationDatabase(String orgDbName, String orgCode) {
        BankContext.setCurrentBank(orgDbName);
        BankContext.setCurrentBankCode(orgCode);

        // ── 5 Organization Roles ──────────────────────────────────────────
        OrganizationRole adminRole = seedRole("ADMIN", "BANK_NBFC", "Bank/NBFC internal admin — configures org and assigns roles");
        OrganizationRole makerRole = seedRole("MAKER", "BANK_NBFC", "Creates and initiates loan records for Checker approval");
        OrganizationRole checkerRole = seedRole("CHECKER", "BANK_NBFC", "Reviews and approves Maker actions");
        OrganizationRole viewerRole = seedRole("VIEWER", "BANK_NBFC", "Read-only access");
        seedRole("CUSTOMER", "CUSTOMER", "Loan applicant");

        // ── Branches ──────────────────────────────────────────────────────
        Branch mainBranch = branchRepository.findByCode(orgCode + "-BR-01")
                .orElseGet(() -> branchRepository.save(Branch.builder()
                        .name(orgCode.equals("HDFC01") ? "Mumbai Fort Branch" : "Pune Central Branch")
                        .code(orgCode + "-BR-01")
                        .address("Nariman Point / Senapati Bapat Marg")
                        .city(orgCode.equals("HDFC01") ? "Mumbai" : "Pune")
                        .state("Maharashtra")
                        .pincode("400021")
                        .status("ACTIVE")
                        .build()));

        branchRepository.findByCode(orgCode + "-BR-02")
                .orElseGet(() -> branchRepository.save(Branch.builder()
                        .name("New Delhi Regional Branch")
                        .code(orgCode + "-BR-02")
                        .address("Connaught Place Block B")
                        .city("New Delhi")
                        .state("Delhi")
                        .pincode("110001")
                        .status("ACTIVE")
                        .build()));

        // ── Seed Permissions & Role Mappings ─────────────────────────────
        seedPermissions();

        seedRolePermissions(orgDbName, adminRole, List.of(
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

        seedRolePermissions(orgDbName, makerRole, List.of(
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

        seedRolePermissions(orgDbName, checkerRole, List.of(
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

        seedRolePermissions(orgDbName, viewerRole, List.of(
                ApplicationConstants.Permissions.LOAN_APPLICATION_VIEW,
                ApplicationConstants.Permissions.CUSTOMER_VIEW,
                ApplicationConstants.Permissions.CUSTOMER_VIEW_ALL,
                ApplicationConstants.Permissions.DOCUMENT_VIEW,
                ApplicationConstants.Permissions.REPORT_VIEW,
                ApplicationConstants.Permissions.REPORT_EXPORT,
                ApplicationConstants.Permissions.DASHBOARD_VIEW,
                ApplicationConstants.Permissions.DASHBOARD_ANALYTICS_VIEW
        ));

        // ── Staff Users ───────────────────────────────────────────────────
        if ("HDFC01".equals(orgCode)) {
            createStaffUser("admin@hdfcbank.com", "EMP-HDFC-001", "admin", "Vikram", "Aditya", "Mehta", "+919876500001", "Admin@123", adminRole, mainBranch);
            createStaffUser("maker@hdfcbank.com", "EMP-HDFC-002", "maker01", "Rohan", "Kumar", "Verma", "+919876500002", "Maker@123", makerRole, mainBranch);
            createStaffUser("checker@hdfcbank.com", "EMP-HDFC-003", "chk01", "Priyanka", "Devi", "Nair", "+919876500003", "Checker@123", checkerRole, mainBranch);
            createStaffUser("viewer@hdfcbank.com", "EMP-HDFC-004", "view01", "Sanjay", "Rao", "Kulkarni", "+919876500004", "Viewer@123", viewerRole, mainBranch);

            // Demo customer
            if (customerRepository.findByEmail("rajesh.kumar@gmail.com").isEmpty()) {
                customerRepository.save(Customer.builder()
                        .customerCode("CUST-HDFC-1001")
                        .branch(mainBranch)
                        .firstName("Rajesh")
                        .middleName("")
                        .lastName("Kumar")
                        .email("rajesh.kumar@gmail.com")
                        .phone("+919876500005")
                        .passwordHash(passwordEncoder.encode("Customer@123"))
                        .isActive(true)
                        .build());
            }
        } else if ("BAJAJ02".equals(orgCode)) {
            createStaffUser("admin@bajajfinance.com", "EMP-BJ-001", "bj_admin", "Ananya", "R", "Deshmukh", "+919876500010", "Admin@123", adminRole, mainBranch);
        }
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
                // code, description, module
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

    /**
     * Creates a staff user if they don't already exist.
     * Initial status is OPERATIVE (demo seed — in production new users start as PENDING_VERIFICATION).
     */
    private void createStaffUser(String email, String empNo, String username,
                                 String firstName, String middleName, String lastName,
                                 String mobile, String rawPassword,
                                 OrganizationRole role, Branch branch) {
        if (organizationUserRepository.findByEmail(email).isEmpty()) {
            organizationUserRepository.save(OrganizationUser.builder()
                    .empNo(empNo)
                    .username(username)
                    .loginBranch(branch)
                    .role(role)
                    .firstName(firstName)
                    .middleName(middleName)
                    .lastName(lastName)
                    .email(email)
                    .mobile(mobile)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .isActive(true)
                    .status(ApplicationConstants.UserStatus.OPERATIVE)
                    .twoFaEnabled(true)
                    .loginOnHolidays(false)
                    .multiBranchAccess(false)
                    .inactiveSessionTimeout(1800)
                    .noOfBadLogins(0)
                    .build());
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
