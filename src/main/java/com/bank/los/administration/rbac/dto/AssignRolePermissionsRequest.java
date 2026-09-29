package com.bank.los.administration.rbac.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignRolePermissionsRequest {

    @NotEmpty(message = "Permission codes list cannot be empty")
    private List<String> permissionCodes;
}
