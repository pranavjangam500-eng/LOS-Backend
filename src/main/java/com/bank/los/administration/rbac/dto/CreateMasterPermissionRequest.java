package com.bank.los.administration.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMasterPermissionRequest {

    @NotBlank(message = "Permission code is required (e.g. LOOKUP_BANK_EXPORT)")
    @Size(max = 100, message = "Permission code must not exceed 100 characters")
    private String code;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotBlank(message = "Module is required (e.g. LOOKUP, USER, LOAN)")
    @Size(max = 60, message = "Module must not exceed 60 characters")
    private String module;

    @Builder.Default
    private Boolean isSystem = false;
}
