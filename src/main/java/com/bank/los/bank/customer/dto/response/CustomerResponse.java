package com.bank.los.bank.customer.dto.response;

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
@Schema(description = "Response representing a customer")
public class CustomerResponse {

    private Long id;
    private String customerCode;
    private Long branchId;
    private String branchName;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
