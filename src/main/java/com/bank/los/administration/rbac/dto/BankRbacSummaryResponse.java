package com.bank.los.administration.rbac.dto;

import com.bank.los.bank.rbac.dto.BankRolePermissionResponse;
import com.bank.los.bank.rbac.dto.DesignationRoleMappingResponse;
import com.bank.los.bank.rbac.dto.PermissionOverrideResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankRbacSummaryResponse {

    private String bankCode;
    private String institutionName;
    private String dbName;
    private List<BankRolePermissionResponse> roles;
    private List<DesignationRoleMappingResponse> designations;
    private List<PermissionOverrideResponse> permissionOverrides;
}
