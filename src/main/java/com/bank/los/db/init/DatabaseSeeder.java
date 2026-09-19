package com.bank.los.db.init;

import com.bank.los.config.TenantContext;
import com.bank.los.master.entity.InternalUser;
import com.bank.los.master.entity.LoginDirectory;
import com.bank.los.master.entity.MasterRole;
import com.bank.los.master.entity.Organization;
import com.bank.los.master.repository.InternalUserRepository;
import com.bank.los.master.repository.LoginDirectoryRepository;
import com.bank.los.master.repository.MasterRoleRepository;
import com.bank.los.master.repository.OrganizationRepository;
import com.bank.los.tenant.entity.Branch;
import com.bank.los.tenant.entity.Customer;
import com.bank.los.tenant.entity.TenantRole;
import com.bank.los.tenant.entity.TenantUser;
import com.bank.los.tenant.repository.BranchRepository;
import com.bank.los.tenant.repository.CustomerRepository;
import com.bank.los.tenant.repository.TenantRoleRepository;
import com.bank.los.tenant.repository.TenantUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final MasterRoleRepository masterRoleRepository;
    private final OrganizationRepository organizationRepository;
    private final InternalUserRepository internalUserRepository;
    private final LoginDirectoryRepository loginDirectoryRepository;

    private final TenantRoleRepository tenantRoleRepository;
    private final BranchRepository branchRepository;
    private final TenantUserRepository tenantUserRepository;
    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking database initialization and seed data...");

        try {
            seedMasterDatabase();
            seedTenantDatabase("los_hdfc01_db", "HDFC01");
            seedTenantDatabase("los_bajaj02_db", "BAJAJ02");
            log.info("Database seeding completed successfully.");
        } catch (Exception e) {
            log.warn("Notice during seeding: {}", e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }

    private void seedMasterDatabase() {
        TenantContext.setCurrentTenant(TenantContext.MASTER_TENANT_ID);

        // 1. Master Internal Role
        MasterRole adminRole = masterRoleRepository.findByName("INTERNAL_ADMIN")
                .orElseGet(() -> masterRoleRepository.save(MasterRole.builder()
                        .name("INTERNAL_ADMIN")
                        .panel("INTERNAL")
                        .description("Platform team; manages tenants and features")
                        .build()));

        // 2. Sample Organizations
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

        // 3. Master Internal Admin User
        if (internalUserRepository.findByEmail("admin@losplatform.com").isEmpty()) {
            InternalUser internalAdmin = internalUserRepository.save(InternalUser.builder()
                    .userCode("ADM-001")
                    .role(adminRole)
                    .firstName("Super")
                    .middleName("")
                    .lastName("Administrator")
                    .email("admin@losplatform.com")
                    .phone("+919999900000")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .isActive(true)
                    .failedLoginAttempts(0)
                    .build());

            loginDirectoryRepository.save(LoginDirectory.builder()
                    .userCode(internalAdmin.getUserCode())
                    .email(internalAdmin.getEmail())
                    .phone(internalAdmin.getPhone())
                    .organization(hdfc) // or default anchor
                    .userType("INTERNAL")
                    .build());
        }

        // 4. Register HDFC Bank demo accounts in routing directory
        registerLoginDirectoryEntry("superadmin@hdfcbank.com", "HDFC-ADM-01", "+919876500001", hdfc, "STAFF");
        registerLoginDirectoryEntry("maker@hdfcbank.com", "HDFC-MKR-01", "+919876500002", hdfc, "STAFF");
        registerLoginDirectoryEntry("checker@hdfcbank.com", "HDFC-CHK-01", "+919876500003", hdfc, "STAFF");
        registerLoginDirectoryEntry("viewer@hdfcbank.com", "HDFC-VIW-01", "+919876500004", hdfc, "STAFF");
        registerLoginDirectoryEntry("rajesh.kumar@gmail.com", "CUST-HDFC-1001", "+919876500005", hdfc, "CUSTOMER");

        // 5. Register Bajaj demo accounts in routing directory
        registerLoginDirectoryEntry("superadmin@bajajfinance.com", "BJ-ADM-01", "+919876500010", bajaj, "STAFF");
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

    private void seedTenantDatabase(String tenantDbName, String orgCode) {
        TenantContext.setCurrentTenant(tenantDbName);
        TenantContext.setCurrentOrgCode(orgCode);

        // 1. Tenant Roles
        TenantRole superAdminRole = tenantRoleRepository.findByName("SUPER_ADMIN")
                .orElseGet(() -> tenantRoleRepository.save(TenantRole.builder()
                        .name("SUPER_ADMIN")
                        .panel("BANK_NBFC")
                        .description("Full control within this NBFC/Bank")
                        .build()));

        TenantRole makerRole = tenantRoleRepository.findByName("MAKER")
                .orElseGet(() -> tenantRoleRepository.save(TenantRole.builder()
                        .name("MAKER")
                        .panel("BANK_NBFC")
                        .description("Creates/initiates records for Checker approval")
                        .build()));

        TenantRole checkerRole = tenantRoleRepository.findByName("CHECKER")
                .orElseGet(() -> tenantRoleRepository.save(TenantRole.builder()
                        .name("CHECKER")
                        .panel("BANK_NBFC")
                        .description("Reviews and approves Maker actions")
                        .build()));

        TenantRole viewerRole = tenantRoleRepository.findByName("VIEWER")
                .orElseGet(() -> tenantRoleRepository.save(TenantRole.builder()
                        .name("VIEWER")
                        .panel("BANK_NBFC")
                        .description("Read-only access")
                        .build()));

        TenantRole customerRole = tenantRoleRepository.findByName("CUSTOMER")
                .orElseGet(() -> tenantRoleRepository.save(TenantRole.builder()
                        .name("CUSTOMER")
                        .panel("CUSTOMER")
                        .description("Loan applicant")
                        .build()));

        // 2. Branches
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

        Branch secondBranch = branchRepository.findByCode(orgCode + "-BR-02")
                .orElseGet(() -> branchRepository.save(Branch.builder()
                        .name("New Delhi Regional Branch")
                        .code(orgCode + "-BR-02")
                        .address("Connaught Place Block B")
                        .city("New Delhi")
                        .state("Delhi")
                        .pincode("110001")
                        .status("ACTIVE")
                        .build()));

        // 3. Staff Users
        if (orgCode.equals("HDFC01")) {
            createStaffUserIfNotExists("superadmin@hdfcbank.com", "HDFC-ADM-01", "Vikram", "Aditya", "Mehta", "+919876500001", "Admin@123", superAdminRole, mainBranch);
            createStaffUserIfNotExists("maker@hdfcbank.com", "HDFC-MKR-01", "Rohan", "Kumar", "Verma", "+919876500002", "Maker@123", makerRole, mainBranch);
            createStaffUserIfNotExists("checker@hdfcbank.com", "HDFC-CHK-01", "Priyanka", "Devi", "Nair", "+919876500003", "Checker@123", checkerRole, mainBranch);
            createStaffUserIfNotExists("viewer@hdfcbank.com", "HDFC-VIW-01", "Sanjay", "Rao", "Kulkarni", "+919876500004", "Viewer@123", viewerRole, mainBranch);

            // 4. Customer
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
        } else if (orgCode.equals("BAJAJ02")) {
            createStaffUserIfNotExists("superadmin@bajajfinance.com", "BJ-ADM-01", "Ananya", "R", "Deshmukh", "+919876500010", "Admin@123", superAdminRole, mainBranch);
        }
    }

    private void createStaffUserIfNotExists(String email, String userCode, String firstName, String middleName, String lastName,
                                           String phone, String rawPassword, TenantRole role, Branch branch) {
        if (tenantUserRepository.findByEmail(email).isEmpty()) {
            tenantUserRepository.save(TenantUser.builder()
                    .userCode(userCode)
                    .branch(branch)
                    .role(role)
                    .firstName(firstName)
                    .middleName(middleName)
                    .lastName(lastName)
                    .email(email)
                    .phone(phone)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .isActive(true)
                    .failedLoginAttempts(0)
                    .build());
        }
    }
}
