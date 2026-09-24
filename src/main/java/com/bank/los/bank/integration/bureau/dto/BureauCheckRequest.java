package com.bank.los.bank.integration.bureau.dto;

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
@Schema(description = "Credit Bureau check request")
public class BureauCheckRequest {

    @NotBlank(message = "PAN number is required")
    private String panNumber;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String dateOfBirth;
    private String mobileNumber;
    private String bureauType; // CIBIL, EXPERIAN, EQUIFAX, CRIF
}
