package com.bank.los.administration.lookup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLookupTypeRequest {

    @NotBlank(message = "Lookup code is required (e.g. 10001, 10008)")
    @Size(max = 50, message = "Lookup code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Builder.Default
    private Boolean isFixed = false;

    @Builder.Default
    private Boolean isActive = true;

    private java.util.List<String> permissions;
}
