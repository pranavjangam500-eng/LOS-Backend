package com.bank.los.bank.audit.dto;

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
@Schema(description = "Response representing a bank activity audit log")
public class BankAuditLogResponse {
    private Long id;
    private Long userId;
    private String userCode;
    private String roleName;
    private String action;
    private String resource;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;
}
