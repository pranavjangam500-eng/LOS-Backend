package com.bank.los.administration.lookup.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LookupTypeResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String status;
    private Boolean isFixed;
    private Boolean isActive;
    private Long subTypeCount;

    private String ownerType;
    private Long ownerBankId;

    @Builder.Default
    private Set<String> permissions = new java.util.HashSet<>();

    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Long modifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<LookupSubTypeResponse> subTypes;
}
