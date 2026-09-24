package com.bank.los.administration.administrator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Platform Administrator Profile Response")
public class AdministratorResponse {
    private Long id;
    private String empNo;
    private String username;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String email;
    private String mobile;
    private String designation;
    private String roleName;
    private String status;
    private Boolean isActive;
    private LocalDate lastLoginDate;
    private LocalDateTime createdAt;
}
