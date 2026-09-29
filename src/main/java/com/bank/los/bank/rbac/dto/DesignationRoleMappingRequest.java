package com.bank.los.bank.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationRoleMappingRequest {

    @NotBlank(message = "Designation name is required (e.g. General Manager, Manager, Clerk)")
    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;

    @NotBlank(message = "Role name is required (e.g. ADMIN, MAKER, CHECKER, VIEWER)")
    private String roleName;

    @Builder.Default
    private Boolean isActive = true;
}
