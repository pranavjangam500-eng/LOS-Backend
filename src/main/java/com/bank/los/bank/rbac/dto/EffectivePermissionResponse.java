package com.bank.los.bank.rbac.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EffectivePermissionResponse {

    private String targetType;
    private String targetName;
    private String roleName;
    private String designation;
    private List<String> basePermissions;
    private List<PermissionOverrideResponse> overrides;
    private List<String> effectivePermissions;
}
