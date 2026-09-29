package com.bank.los.administration.lookup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLookupSubTypeRequest {

    @NotBlank(message = "Sub-type code is required (e.g. 1, 2, PERSONAL_LOAN)")
    @Size(max = 50, message = "Sub-type code must not exceed 50 characters")
    private String subTypeCode;

    @NotBlank(message = "Sub-type description is required")
    @Size(max = 255, message = "Sub-type description must not exceed 255 characters")
    private String subTypeDescription;

    @Builder.Default
    private Boolean isFixed = false;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Integer displayOrder = 0;
}
