package com.bank.los.administration.lookup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLookupSubTypeRequest {

    @NotBlank(message = "Sub-type description is required")
    @Size(max = 255, message = "Sub-type description must not exceed 255 characters")
    private String subTypeDescription;

    private Boolean isFixed;

    private Boolean isActive;

    private Integer displayOrder;
}
