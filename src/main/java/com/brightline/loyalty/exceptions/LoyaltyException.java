package com.brightline.loyalty.exceptions;


public abstract class LoyaltyException extends RuntimeException {

    protected LoyaltyException(String message) {
        super(message);
    }

    protected LoyaltyException(String message, Throwable cause) {
        super(message, cause);
    }
}