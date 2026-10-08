package com.bank.los.otp.exception;

public class OtpInvalidException extends OtpException {
    private final int remainingAttempts;

    public OtpInvalidException(String message, int remainingAttempts) {
        super(message);
        this.remainingAttempts = remainingAttempts;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }
}
