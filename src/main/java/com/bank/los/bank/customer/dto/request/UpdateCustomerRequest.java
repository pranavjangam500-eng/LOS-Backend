package com.bank.los.bank.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to update a customer")
public class UpdateCustomerRequest {

    private Long branchId;
    private String firstName;
    private String middleName;
    private String lastName;

    @Email(message = "Valid email is required")
    private String email;

    private String phone;
    private Boolean isActive;
}
