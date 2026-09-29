package com.bank.los.administration.lookup.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LookupTypeResponse {

    private Long id;
    private String code;
    private String description;
    private Boolean isFixed;
    private Boolean isActive;

    @Builder.Default
    private java.util.Set<String> permissions = new java.util.HashSet<>();

    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Long modifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<LookupSubTypeResponse> subTypes = new ArrayList<>();
}
