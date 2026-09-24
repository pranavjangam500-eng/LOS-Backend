package com.bank.los.administration.monitoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "System and Multi-Organization Routing Health Metrics")
public class SystemMetricsResponse {
    private String status;
    private int totalOrganizations;
    private long totalActiveBanks;
    private long totalActiveNbfcs;
    private long totalGlobalUsers;
    private Map<String, Object> memoryUsage;
    private LocalDateTime timestamp;
}
