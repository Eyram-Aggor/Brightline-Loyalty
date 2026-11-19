package com.brightline.loyalty;


public class InvalidPurchaseException extends LoyaltyException {

    public InvalidPurchaseException(String message) {
        super(message);
    }
}