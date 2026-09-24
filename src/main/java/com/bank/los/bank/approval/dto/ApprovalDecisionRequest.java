package com.bank.los.bank.approval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Maker-Checker Loan approval decision request")
public class ApprovalDecisionRequest {

    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "^(APPROVE|REJECT|RETURN_TO_MAKER)$", message = "Decision must be APPROVE, REJECT, or RETURN_TO_MAKER")
    private String decision;

    private Double sanctionedAmount;
    private String remarks;
}
