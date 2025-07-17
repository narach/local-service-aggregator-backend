package com.service.sector.aggregator.exceptions;

/**
 * Exception thrown when SMS delivery fails through the SNS service.
 */
public class SmsDeliveryException extends RuntimeException {
    public SmsDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
