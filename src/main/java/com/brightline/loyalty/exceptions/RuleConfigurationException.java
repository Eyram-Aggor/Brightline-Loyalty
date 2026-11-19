package com.brightline.loyalty.exceptions;

/** Thrown when the points rules are configured incorrectly, e.g. a tier has no rule. */
public class RuleConfigurationException extends LoyaltyException {

    public RuleConfigurationException(String message) {
        super(message);
    }
}