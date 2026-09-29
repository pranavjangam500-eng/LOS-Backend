package com.bank.los.bank.rbac.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankRolePermissionResponse {

    private Integer roleId;
    private String roleName;
    private String panel;
    private String description;
    private List<String> basePermissions;
    private List<PermissionOverrideResponse> overrides;
    private List<String> effectivePermissions;
}
