package com.bank.los.otp.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.los.otp.dto.OtpResponse;
import com.bank.los.otp.dto.SendOtpRequest;
import com.bank.los.otp.dto.VerifyOtpRequest;
import com.bank.los.otp.service.OtpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for standalone Email OTP generation and verification. Exposed
 * publicly on both /api/v1/otp and /api/otp.
 */
@Slf4j
@RestController("emailOtpController")
@RequestMapping({"/api/v1/otp", "/api/otp"})
@CrossOrigin(origins = "*")
@Tag(name = "Email OTP Authentication", description = "Standalone cryptographic Email OTP dispatch, attempt-tracking, and verification")
public class OtpController {

    private final OtpService otpService;

    public OtpController(@Qualifier("emailOtpService") OtpService otpService) {
        this.otpService = otpService;
    }

    /**
     * 1. POST /api/v1/otp/send or POST /api/otp/send Generates a 6-digit OTP,
     * stores its SHA-256 hash, and sends an HTML email.
     */
    @PostMapping("/send")
    @Operation(summary = "Send Email OTP", description = "Generates and sends a single-use 6-digit OTP to the specified email with a 60-second cooldown")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP sent successfully",
                content = @Content(schema = @Schema(implementation = OtpResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or validation failure",
                content = @Content(schema = @Schema(implementation = OtpResponse.class))),
        @ApiResponse(responseCode = "429", description = "Cooldown active (too many requests)",
                content = @Content(schema = @Schema(implementation = OtpResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error during email dispatch",
                content = @Content(schema = @Schema(implementation = OtpResponse.class)))
    })
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        try {
            OtpService.SendResult result = otpService.sendOtp(request.email());

            if (!result.success()) {
                if (result.cooldownRemaining() != null) {
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(OtpResponse.cooldown(result.message(), result.cooldownRemaining()));
                }
                return ResponseEntity.badRequest().body(OtpResponse.error(result.message()));
            }

            return ResponseEntity.ok(OtpResponse.success(result.message()));
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", request.email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(OtpResponse.error("Failed to send OTP email: " + e.getMessage()));
        }
    }

    /**
     * 2. POST /api/v1/otp/verify or POST /api/otp/verify Verifies the 6-digit
     * OTP code against the SHA-256 hash with attempt tracking and single-use
     * invalidation.
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify Email OTP", description = "Validates submitted OTP code against salted SHA-256 hash. Invalids after max 5 attempts or on success (single-use).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP successfully verified",
                content = @Content(schema = @Schema(implementation = OtpResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid code, expired code, or maximum attempts reached",
                content = @Content(schema = @Schema(implementation = OtpResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error during verification",
                content = @Content(schema = @Schema(implementation = OtpResponse.class)))
    })
    public ResponseEntity<OtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            OtpService.VerifyResult result = otpService.verifyOtp(request.email(), request.otp());

            if (!result.success()) {
                return ResponseEntity.badRequest().body(OtpResponse.error(result.message()));
            }

            return ResponseEntity.ok(OtpResponse.verified(request.email().trim().toLowerCase()));
        } catch (Exception e) {
            log.error("Error occurred while verifying OTP for {}: {}", request.email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(OtpResponse.error("Error occurred while verifying OTP: " + e.getMessage()));
        }
    }
}
