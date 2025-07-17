package com.service.sector.aggregator.exceptions;

/**
 * Exception thrown when a phone number doesn't match the E.164 format.
 */
public class InvalidPhoneNumberException extends RuntimeException {
    public InvalidPhoneNumberException(String message) {
        super(message);
    }
}

