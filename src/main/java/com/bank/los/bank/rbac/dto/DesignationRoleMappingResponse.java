package com.bank.los.bank.rbac.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationRoleMappingResponse {

    private Long id;
    private String designation;
    private Integer roleId;
    private String roleName;
    private List<String> inheritedPermissions;
    private List<PermissionOverrideResponse> overrides;
    private List<String> effectivePermissions;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
