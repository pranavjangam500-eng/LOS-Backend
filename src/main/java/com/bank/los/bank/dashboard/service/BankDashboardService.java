package com.bank.los.bank.dashboard.service;

import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.InternalUserRepository;
import com.bank.los.administration.master.repository.LoginDirectoryRepository;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.bank.dashboard.dto.*;
import com.bank.los.bank.master.entity.Branch;
import com.bank.los.bank.master.repository.BranchRepository;
import com.bank.los.bank.master.repository.CustomerRepository;
import com.bank.los.bank.master.repository.OrganizationUserRepository;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankDashboardService {

    private final OrganizationRepository organizationRepository;
    private final InternalUserRepository internalUserRepository;
    private final LoginDirectoryRepository loginDirectoryRepository;
    private final BranchRepository branchRepository;
    private final OrganizationUserRepository organizationUserRepository;
    private final CustomerRepository customerRepository;

    public BankAdminDashboardDto getBankAdminDashboard(UserPrincipal principal) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        List<Branch> branches = branchRepository.findAll();
        long totalStaff = organizationUserRepository.count();
        long totalCustomers = customerRepository.count();

        List<Map<String, Object>> branchSummaries = new ArrayList<>();
        for (Branch b : branches) {
            Map<String, Object> bMap = new HashMap<>();
            bMap.put("branchId", b.getId());
            bMap.put("branchName", b.getName());
            bMap.put("branchCode", b.getCode());
            bMap.put("city", b.getCity());
            bMap.put("status", b.getStatus());
            bMap.put("staffCount", organizationUserRepository.countByLoginBranchId(b.getId()));
            bMap.put("customerCount", customerRepository.countByBranchId(b.getId()));
            branchSummaries.add(bMap);
        }

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("monthlyOriginationTargetCr", 50.0);
        kpis.put("currentDisbursedCr", 38.4);
        kpis.put("approvalTurnaroundHours", 18.5);
        kpis.put("slaComplianceRate", "96.4%");

        return BankAdminDashboardDto.builder()
                .bankName(principal.getOrganizationCode() + " Operations")
                .bankCode(principal.getOrganizationCode())
                .totalBranches(branches.size())
                .totalStaffUsers(totalStaff)
                .totalCustomers(totalCustomers)
                .activeLoanProducts(6)
                .branchSummaries(branchSummaries)
                .operationalKpis(kpis)
                .build();
    }

    public MakerDashboardDto getMakerDashboard(UserPrincipal principal) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        List<Map<String, Object>> recentApps = new ArrayList<>();
        recentApps.add(Map.of(
                "applicationId", "APP-2026-0091",
                "customerName", "Rajesh Kumar",
                "loanType", "HOME_LOAN",
                "amount", 4500000.0,
                "status", "DRAFT",
                "createdAt", LocalDateTime.now().minusHours(2)
        ));
        recentApps.add(Map.of(
                "applicationId", "APP-2026-0092",
                "customerName", "Pooja Sharma",
                "loanType", "PERSONAL_LOAN",
                "amount", 500000.0,
                "status", "SUBMITTED_TO_CHECKER",
                "createdAt", LocalDateTime.now().minusHours(5)
        ));

        List<Map<String, Object>> actionItems = new ArrayList<>();
        actionItems.add(Map.of(
                "task", "Upload PAN & Salary Slip for APP-2026-0091",
                "priority", "HIGH",
                "dueInHours", 4
        ));

        return MakerDashboardDto.builder()
                .makerName(principal.getFullName())
                .branchName("Main Branch (ID: " + principal.getBranchId() + ")")
                .draftApplications(4)
                .submittedForVerification(12)
                .returnedForCorrection(1)
                .recentApplicationsCreated(recentApps)
                .pendingActionItems(actionItems)
                .build();
    }

    public CheckerDashboardDto getCheckerDashboard(UserPrincipal principal) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        List<Map<String, Object>> queue = new ArrayList<>();
        queue.add(Map.of(
                "applicationId", "APP-2026-0089",
                "customerName", "Amit Patel",
                "loanType", "BUSINESS_LOAN",
                "amount", 7500000.0,
                "cibilScore", 782,
                "kycStatus", "VERIFIED",
                "makerName", "Rohan Verma",
                "submittedAt", LocalDateTime.now().minusHours(3)
        ));
        queue.add(Map.of(
                "applicationId", "APP-2026-0090",
                "customerName", "Sunita Verma",
                "loanType", "AUTO_LOAN",
                "amount", 1200000.0,
                "cibilScore", 745,
                "kycStatus", "VERIFIED",
                "makerName", "Rohan Verma",
                "submittedAt", LocalDateTime.now().minusHours(6)
        ));

        List<Map<String, Object>> alerts = new ArrayList<>();
        alerts.add(Map.of(
                "applicationId", "APP-2026-0089",
                "alertType", "HIGH_EXPOSURE",
                "message", "Business loan exceeds Rs 50 Lakhs threshold. Requires dual approval."
        ));

        return CheckerDashboardDto.builder()
                .checkerName(principal.getFullName())
                .branchName("Approvals Desk (Branch " + principal.getBranchId() + ")")
                .pendingApprovalCount(8)
                .approvedTodayCount(14)
                .rejectedCount(2)
                .approvalQueue(queue)
                .highValueAlerts(alerts)
                .build();
    }

    public ViewerDashboardDto getViewerDashboard(UserPrincipal principal) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        List<Map<String, Object>> branchStats = new ArrayList<>();
        branchStats.add(Map.of("branchName", "Downtown Branch", "disbursedCr", 18.2, "npaPercent", 0.8));
        branchStats.add(Map.of("branchName", "Tech Hub Branch", "disbursedCr", 24.5, "npaPercent", 0.4));
        branchStats.add(Map.of("branchName", "North Regional Branch", "disbursedCr", 11.7, "npaPercent", 1.2));

        List<Map<String, Object>> trends = new ArrayList<>();
        trends.add(Map.of("month", "June 2026", "applications", 145, "disbursedCr", 32.1));
        trends.add(Map.of("month", "July 2026", "applications", 168, "disbursedCr", 36.4));
        trends.add(Map.of("month", "August 2026", "applications", 192, "disbursedCr", 42.0));

        return ViewerDashboardDto.builder()
                .organizationName(principal.getOrganizationCode() != null ? principal.getOrganizationCode() : "Bank Analytics")
                .totalPortfolioSize(450)
                .totalDisbursedAmountCr(110.8)
                .defaultRatePercentage(0.72)
                .portfolioByBranch(branchStats)
                .performanceTrends(trends)
                .build();
    }

    public CustomerDashboardDto getCustomerDashboard(UserPrincipal principal) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        List<Map<String, Object>> apps = new ArrayList<>();
        apps.add(Map.of(
                "applicationId", "APP-2026-CUST-101",
                "loanType", "HOME_LOAN",
                "requestedAmount", 3500000.0,
                "sanctionedAmount", 3500000.0,
                "interestRate", "8.65% p.a.",
                "tenureMonths", 240,
                "status", "SANCTIONED_PENDING_DISBURSAL",
                "nextEmiDueDate", "2026-10-05"
        ));

        List<Map<String, Object>> actionItems = new ArrayList<>();
        actionItems.add(Map.of(
                "action", "E-sign Loan Agreement for APP-2026-CUST-101",
                "deadline", "2026-09-25",
                "status", "PENDING_SIGNATURE"
        ));

        return CustomerDashboardDto.builder()
                .customerName(principal.getFullName())
                .customerCode(principal.getUserCode())
                .bankName(principal.getOrganizationCode())
                .activeApplicationsCount(1)
                .approvedLoansCount(1)
                .loanApplications(apps)
                .requiredActionItems(actionItems)
                .build();
    }
}
