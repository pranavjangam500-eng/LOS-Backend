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
public class BranchResponse {

    @Schema(description = "Branch ID", example = "1")
    private Long id;

    @Schema(description = "Branch name", example = "Mumbai Main Branch")
    private String name;

    @Schema(description = "Branch unique code", example = "HDFC01-BR-01")
    private String code;

    @Schema(description = "Street address", example = "Nariman Point")
    private String address;

    @Schema(description = "City", example = "Mumbai")
    private String city;

    @Schema(description = "State", example = "Maharashtra")
    private String state;

    @Schema(description = "Pincode", example = "400021")
    private String pincode;

    @Schema(description = "Branch status", example = "ACTIVE")
    private String status;
}
