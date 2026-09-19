package com.bank.los.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

    @NotBlank(message = "Customer code is required")
    @Schema(description = "Customer ID/CIF code", example = "CUST-1001")
    private String customerCode;

    @Schema(description = "Assigned branch ID", example = "1")
    private Long branchId;

    @NotBlank(message = "First name is required")
    @Schema(description = "First name", example = "Amit")
    private String firstName;

    @Schema(description = "Middle name", example = "")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name", example = "Patel")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Customer email address", example = "amit.patel@gmail.com")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Customer mobile phone", example = "+919876543222")
    private String phone;

    @Schema(description = "Portal password", example = "Customer@123")
    private String password;
}
