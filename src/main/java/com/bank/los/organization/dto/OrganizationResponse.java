package com.bank.los.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private Long id;
    private String name;
    private String code;
    private String type;
    private String status;
    private String contactEmail;
    private String contactPhone;
    private String dbName;
    private String dbHost;
    private Integer dbPort;
    private LocalDateTime createdAt;
}
