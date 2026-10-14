package com.bank.los.administration.lookup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lookup option dropdown item representing code and description")
public class LookupOptionResponse {

    @Schema(description = "Lookup option / sub-type code", example = "1")
    private String code;

    @Schema(description = "Lookup option description / label", example = "Clerk")
    private String description;
}
