package com.bank.los.otp.exception;

public class OtpMaxAttemptsExceededException extends OtpException {
    public OtpMaxAttemptsExceededException(String message) {
        super(message);
    }
}
