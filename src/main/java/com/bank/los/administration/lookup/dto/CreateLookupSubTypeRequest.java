package com.bank.los.administration.lookup.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class CreateLookupSubTypeRequest {

    @JsonProperty("code")
    @JsonAlias({"subTypeCode"})
    @Size(max = 50, message = "Lookup sub-type code must not exceed 50 characters")
    private String code;

    @JsonProperty("name")
    @JsonAlias({"subTypeDescription", "description"})
    @NotBlank(message = "Lookup sub-type name is required")
    @Size(max = 255, message = "Lookup sub-type name must not exceed 255 characters")
    private String name;

    @Builder.Default
    private String status = "ACTIVE";

    @Builder.Default
    private Boolean isFixed = false;

    private Integer displayOrder;

    public static class CreateLookupSubTypeRequestBuilder {
        public CreateLookupSubTypeRequestBuilder subTypeDescription(String subTypeDescription) {
            this.name = subTypeDescription;
            return this;
        }

        public CreateLookupSubTypeRequestBuilder description(String description) {
            this.name = description;
            return this;
        }

        public CreateLookupSubTypeRequestBuilder code(String code) {
            this.code = code;
            return this;
        }

        public CreateLookupSubTypeRequestBuilder subTypeCode(String subTypeCode) {
            this.code = subTypeCode;
            return this;
        }
    }

    // Backward-compatibility accessors
    public String getSubTypeCode() {
        return code;
    }

    public void setSubTypeCode(String subTypeCode) {
        this.code = subTypeCode;
    }

    public String getSubTypeDescription() {
        return name;
    }

    public void setSubTypeDescription(String subTypeDescription) {
        if (this.name == null || this.name.isBlank()) {
            this.name = subTypeDescription;
        }
    }

    public Boolean getIsActive() {
        return !"INACTIVE".equalsIgnoreCase(status);
    }

    public void setIsActive(Boolean isActive) {
        if (isActive != null) {
            this.status = isActive ? "ACTIVE" : "INACTIVE";
        }
    }
}
