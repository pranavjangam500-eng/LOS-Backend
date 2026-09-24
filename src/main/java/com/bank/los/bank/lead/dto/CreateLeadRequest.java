package com.bank.los.bank.lead.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to capture a loan lead")
public class CreateLeadRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String loanProductType;
    private Double requestedAmount;
    private Long branchId;
    private String remarks;
}
