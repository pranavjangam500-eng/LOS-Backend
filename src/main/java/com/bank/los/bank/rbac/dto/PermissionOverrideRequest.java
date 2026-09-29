package com.bank.los.bank.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionOverrideRequest {

    @NotBlank(message = "Target type is required (ROLE or DESIGNATION)")
    @Pattern(regexp = "(?i)ROLE|DESIGNATION", message = "targetType must be either 'ROLE' or 'DESIGNATION'")
    private String targetType;

    @NotBlank(message = "Target name is required (e.g. General Manager, ADMIN)")
    @Size(max = 100, message = "Target name must not exceed 100 characters")
    private String targetName;

    @NotBlank(message = "Permission code is required (e.g. LOOKUP_BANK_DELETE)")
    @Size(max = 100, message = "Permission code must not exceed 100 characters")
    private String permissionCode;

    @NotBlank(message = "Effect is required (ALLOW or DENY)")
    @Pattern(regexp = "(?i)ALLOW|DENY", message = "effect must be either 'ALLOW' or 'DENY'")
    private String effect;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    @Builder.Default
    private Boolean isActive = true;
}
