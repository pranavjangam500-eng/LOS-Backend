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
    private Boolean canView = true;

    @Builder.Default
    private Boolean canAdd = true;

    @Builder.Default
    private Boolean canImportFromMaster = true;

    @Builder.Default
    private Boolean canEdit = true;

    @Builder.Default
    private Boolean canDelete = true;

    @Builder.Default
    private Boolean canActivate = true;

    @Builder.Default
    private Boolean canDeactivate = true;

    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Long modifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<LookupSubTypeResponse> subTypes = new ArrayList<>();
}
