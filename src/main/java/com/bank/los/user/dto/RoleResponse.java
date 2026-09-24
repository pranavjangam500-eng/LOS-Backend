package com.bank.los.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    @Schema(description = "Role ID", example = "1")
    private Integer id;

    @Schema(description = "Role name", example = "ADMIN")
    private String name;

    @Schema(description = "Portal panel", example = "BANK_NBFC")
    private String panel;

    @Schema(description = "Role description", example = "Bank/NBFC internal admin — configures org and manages users")
    private String description;
}
