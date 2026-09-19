package com.bank.los.customer.dto.request;

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
public class UpdateCustomerRequest {

    @NotBlank(message = "First name is required")
    @Schema(description = "First name", example = "Amit")
    private String firstName;

    @Schema(description = "Middle name")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name", example = "Patel")
    private String lastName;

    @Schema(description = "Contact phone", example = "+919876543222")
    private String phone;
}
