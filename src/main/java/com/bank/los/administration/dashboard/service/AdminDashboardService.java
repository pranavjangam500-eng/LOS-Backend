package com.bank.los.administration.dashboard.service;

import com.bank.los.administration.dashboard.dto.InternalAdminDashboardDto;
import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.InternalUserRepository;
import com.bank.los.administration.master.repository.LoginDirectoryRepository;
import com.bank.los.administration.master.repository.OrganizationRepository;
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
public class AdminDashboardService {

    private final OrganizationRepository organizationRepository;
    private final InternalUserRepository internalUserRepository;
    private final LoginDirectoryRepository loginDirectoryRepository;

    public InternalAdminDashboardDto getInternalAdminDashboard(UserPrincipal principal) {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        List<Organization> orgs = organizationRepository.findAll();
        long activeBanks = orgs.stream().filter(o -> "BANK".equalsIgnoreCase(o.getType()) && "ACTIVE".equalsIgnoreCase(o.getStatus())).count();
        long activeNbfcs = orgs.stream().filter(o -> "NBFC".equalsIgnoreCase(o.getType()) && "ACTIVE".equalsIgnoreCase(o.getStatus())).count();
        long totalUsers = loginDirectoryRepository.count();

        List<Map<String, Object>> orgList = new ArrayList<>();
        for (Organization org : orgs) {
            Map<String, Object> orgMap = new HashMap<>();
            orgMap.put("id", org.getId());
            orgMap.put("name", org.getName());
            orgMap.put("code", org.getCode());
            orgMap.put("type", org.getType());
            orgMap.put("status", org.getStatus());
            orgMap.put("dbName", org.getDbName());
            orgMap.put("dbHost", org.getDbHost());
            orgMap.put("dbPort", org.getDbPort());
            orgList.add(orgMap);
        }

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("databaseEngine", "PostgreSQL Multi-Organization");
        health.put("totalRoutingNodes", orgs.size());
        health.put("checkedAt", LocalDateTime.now());

        return InternalAdminDashboardDto.builder()
                .panelTitle("LOS Platform Master Administration")
                .totalOrganizations(orgs.size())
                .totalActiveBanks(activeBanks)
                .totalActiveNbfcs(activeNbfcs)
                .totalGlobalUsers(totalUsers)
                .registeredOrganizations(orgList)
                .systemHealth(health)
                .build();
    }
}
