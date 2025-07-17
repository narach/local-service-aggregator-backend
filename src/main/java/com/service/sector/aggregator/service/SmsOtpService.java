package com.service.sector.aggregator.service;
import com.service.sector.aggregator.exceptions.InvalidPhoneNumberException;
import com.service.sector.aggregator.exceptions.SmsDeliveryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.security.SecureRandom;
import java.util.Map;

@Service
public class SmsOtpService implements AutoCloseable {
    public static final String TEST_PHONE_PREFIX = "+0";
    public static final String DEFAULT_TEST_CODE = "123456";

    private final SnsClient sns;
    private final SecureRandom rnd = new SecureRandom();
    private final String topicArn;

    private static final String SMS_TYPE_ATTRIBUTE = "AWS.SNS.SMS.SMSType";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_CODE = 1_000_000;

    public SmsOtpService(
            @Value("${aws.sns.region}") Region region,
            @Value("${aws.sns.topic.verification-codes}") String topicArn
    ) {
        sns = SnsClient.builder()
                .region(region)
                .build();
        this.topicArn = topicArn;
    }

    /** Returns a zero-padded six–digit string, e.g. "034921". */
    public String newCode(String phoneNumber
    ) {
        if (isTestPhoneNumber(phoneNumber)) {
            return DEFAULT_TEST_CODE;
        }
        return String.format("%0" + CODE_LENGTH + "d", rnd.nextInt(MAX_CODE));

    }

    /**
     * Sends the code; number must be in E.164, e.g. "+447911123456".
     * @throws InvalidPhoneNumberException if phone number format is invalid
     * @throws SmsDeliveryException if SMS delivery fails
     */
    public void send(String phoneE164, String code) {
        validatePhoneNumber(phoneE164);

        // Skip SMS sending for test numbers
        if (isTestPhoneNumber(phoneE164)) {
            return;
        }

        try {
            // Publish directly to phone number
            PublishRequest publishRequest = PublishRequest.builder()
                    .phoneNumber(phoneE164)  // Direct to phone number
                    .message(formatMessage(code))
                    .messageAttributes(createMessageAttributes())
                    .build();

            sns.publish(publishRequest);
        } catch (SnsException e) {
            throw new SmsDeliveryException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    private void validatePhoneNumber(String phoneE164) {
        if (phoneE164 == null ||
                (!isTestPhoneNumber(phoneE164) && !phoneE164.matches("\\+[1-9]\\d{1,14}"))) {
            throw new InvalidPhoneNumberException("Phone number must be in E.164 format: " + phoneE164);
        }
    }

    /**
     * Checks if the phone number is a test number (starts with +0)
     */
    public static boolean isTestPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.startsWith(TEST_PHONE_PREFIX);
    }

    private String formatMessage(String code) {
        return "Your verification code: " + code;
    }

    private Map<String, MessageAttributeValue> createMessageAttributes() {
        return Map.of(
                SMS_TYPE_ATTRIBUTE,
                MessageAttributeValue.builder()
                        .stringValue("Transactional")
                        .dataType("String")
                        .build()
        );
    }

    @Override
    public void close() {
        if (sns != null) {
            sns.close();
        }
    }
}