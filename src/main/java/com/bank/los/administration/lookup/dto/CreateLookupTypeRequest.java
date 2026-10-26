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
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class CreateLookupTypeRequest {

    @JsonProperty("code")
    @Size(max = 50, message = "Lookup type code must not exceed 50 characters")
    private String code;

    @JsonProperty("name")
    @JsonAlias({"description"})
    @NotBlank(message = "Lookup type name is required")
    @Size(max = 255, message = "Lookup type name must not exceed 255 characters")
    private String name;

    @Builder.Default
    private String status = "ACTIVE";

    @Builder.Default
    private Boolean isFixed = false;

    private List<String> permissions;

    public static class CreateLookupTypeRequestBuilder {
        public CreateLookupTypeRequestBuilder description(String description) {
            this.name = description;
            return this;
        }

        public CreateLookupTypeRequestBuilder code(String code) {
            this.code = code;
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

    public Boolean getIsActive() {
        return !"INACTIVE".equalsIgnoreCase(status);
    }

    public void setIsActive(Boolean isActive) {
        if (isActive != null) {
            this.status = isActive ? "ACTIVE" : "INACTIVE";
        }
    }
}
