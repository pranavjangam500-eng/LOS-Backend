package com.bank.los.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authenticated user profile details")
public class UserProfileResponse {

    private Long   id;
    private String empNo;               // EmpNo from senior's design
    private String username;            // UserName (login credential)
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String email;
    private String mobile;
    private String role;
    private String userType;            // INTERNAL / STAFF / CUSTOMER
    private String status;              // OPERATIVE / NON_OPERATIVE / PENDING_VERIFICATION
    private String designation;
    private String organizationName;
    private String organizationCode;
    private String organizationType;    // BANK / NBFC
    private Long   branchId;
    private String branchName;
    private String branchCode;
    private Boolean multiBranchAccess;
    private Integer inactiveSessionTimeout;
    private LocalDate lastLoginDate;
    private LocalTime lastLoginTime;
}
