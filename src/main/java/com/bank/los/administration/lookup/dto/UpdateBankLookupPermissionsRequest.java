package com.bank.los.administration.lookup.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBankLookupPermissionsRequest {

    @Builder.Default
    @NotNull(message = "Permissions set cannot be null")
    private Set<String> permissions = new HashSet<>();
}
