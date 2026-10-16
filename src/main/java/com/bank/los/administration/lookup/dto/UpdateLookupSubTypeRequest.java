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
public class UpdateLookupSubTypeRequest {

    @JsonProperty("name")
    @JsonAlias({"subTypeDescription", "description"})
    @NotBlank(message = "Lookup sub-type name is required")
    @Size(max = 255, message = "Lookup sub-type name must not exceed 255 characters")
    private String name;

    private String status;

    private Boolean isFixed;

    private Boolean isActive;

    private Integer displayOrder;

    public static class UpdateLookupSubTypeRequestBuilder {
        public UpdateLookupSubTypeRequestBuilder subTypeDescription(String subTypeDescription) {
            this.name = subTypeDescription;
            return this;
        }

        public UpdateLookupSubTypeRequestBuilder description(String description) {
            this.name = description;
            return this;
        }
    }

    // Backward-compatibility accessors
    public String getSubTypeDescription() {
        return name;
    }

    public void setSubTypeDescription(String subTypeDescription) {
        if (this.name == null || this.name.isBlank()) {
            this.name = subTypeDescription;
        }
    }

    public Boolean resolveIsActive() {
        if (status != null && !status.isBlank()) {
            return !"INACTIVE".equalsIgnoreCase(status);
        }
        return isActive;
    }
}
