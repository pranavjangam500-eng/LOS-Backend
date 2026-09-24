package com.bank.los.common.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;

@RestController
@Tag(name = "Health", description = "Application and Keep-Alive Health Check APIs")
public class HealthController {

    @GetMapping({"/health", "/api/v1/health"})
    @Operation(summary = "Public health check endpoint for monitoring and keep-alive pings")
    public ResponseEntity<HealthResponse> healthCheck() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        
        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .service("LOS-Backend")
                .timestamp(LocalDateTime.now())
                .uptimeSeconds(uptimeMs / 1000)
                .message("LOS Backend service is live and healthy")
                .build();

        return ResponseEntity.ok(response);
    }
}
