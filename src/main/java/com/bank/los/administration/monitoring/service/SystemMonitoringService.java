package com.bank.los.administration.monitoring.service;

import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.LoginDirectoryRepository;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.administration.monitoring.dto.SystemMetricsResponse;
import com.bank.los.config.OrganizationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMonitoringService {

    private final OrganizationRepository organizationRepository;
    private final LoginDirectoryRepository loginDirectoryRepository;

    public SystemMetricsResponse getMetrics() {
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);

        List<Organization> orgs = organizationRepository.findAll();
        long activeBanks = orgs.stream().filter(o -> "BANK".equalsIgnoreCase(o.getType()) && "ACTIVE".equalsIgnoreCase(o.getStatus())).count();
        long activeNbfcs = orgs.stream().filter(o -> "NBFC".equalsIgnoreCase(o.getType()) && "ACTIVE".equalsIgnoreCase(o.getStatus())).count();
        long totalUsers = loginDirectoryRepository.count();

        Runtime rt = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("totalMemoryMb", rt.totalMemory() / (1024 * 1024));
        memory.put("freeMemoryMb", rt.freeMemory() / (1024 * 1024));
        memory.put("maxMemoryMb", rt.maxMemory() / (1024 * 1024));

        return SystemMetricsResponse.builder()
                .status("HEALTHY")
                .totalOrganizations(orgs.size())
                .totalActiveBanks(activeBanks)
                .totalActiveNbfcs(activeNbfcs)
                .totalGlobalUsers(totalUsers)
                .memoryUsage(memory)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
