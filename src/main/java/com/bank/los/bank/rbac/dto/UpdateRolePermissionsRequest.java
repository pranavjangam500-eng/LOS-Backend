package com.bank.los.bank.rbac.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRolePermissionsRequest {

    @NotEmpty(message = "Permissions list cannot be empty")
    private List<String> permissions;
}
