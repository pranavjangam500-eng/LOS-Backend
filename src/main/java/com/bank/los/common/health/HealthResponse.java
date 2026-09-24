package com.bank.los.common.health;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload for application health check and keep-alive monitoring")
public class HealthResponse {

    @Schema(description = "Health status of application", example = "UP")
    private String status;

    @Schema(description = "Service name", example = "LOS-Backend")
    private String service;

    @Schema(description = "Current server timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Application uptime in seconds", example = "3600")
    private long uptimeSeconds;

    @Schema(description = "Status summary message", example = "LOS Backend service is live and healthy")
    private String message;
}
