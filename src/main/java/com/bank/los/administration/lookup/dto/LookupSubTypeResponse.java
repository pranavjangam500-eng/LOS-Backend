package com.bank.los.administration.lookup.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LookupSubTypeResponse {

    private Long id;
    private String code;
    private String typeCode;
    private String lookupTypeCode;
    private String typeDescription;
    private String subTypeCode;
    private String name;
    private String description;
    private String subTypeDescription;
    private String status;
    private Boolean isFixed;
    private Boolean isActive;
    private Integer displayOrder;
    private String ownerType;
    private Long ownerBankId;
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Long modifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
