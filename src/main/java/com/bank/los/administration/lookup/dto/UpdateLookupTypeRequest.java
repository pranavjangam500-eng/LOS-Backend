package com.bank.los.administration.lookup.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLookupTypeRequest {

    @JsonProperty("name")
    @JsonAlias({"description"})
    @NotBlank(message = "Lookup type name is required")
    @Size(max = 255, message = "Lookup type name must not exceed 255 characters")
    private String name;

    private String status;

    private Boolean isFixed;

    private Boolean isActive;

    private List<String> permissions;

    public static class UpdateLookupTypeRequestBuilder {
        public UpdateLookupTypeRequestBuilder description(String description) {
            this.name = description;
            return this;
        }
    }

    // Backward-compatibility accessors
    public String getDescription() {
        return name;
    }

    public void setDescription(String description) {
        if (this.name == null || this.name.isBlank()) {
            this.name = description;
        }
    }

    public Boolean resolveIsActive() {
        if (status != null && !status.isBlank()) {
            return !"INACTIVE".equalsIgnoreCase(status);
        }
        return isActive;
    }
}
