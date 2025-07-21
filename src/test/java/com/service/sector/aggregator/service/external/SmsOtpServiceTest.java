package com.service.sector.aggregator.service.external;

import com.service.sector.aggregator.exceptions.InvalidPhoneNumberException;
import com.service.sector.aggregator.exceptions.SmsDeliveryException;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SmsOtpService}.
 */
class SmsOtpServiceTest {

    private static final String TOPIC_ARN = "arn:aws:sns:eu-north-1:123456789012:verification-codes";

    @Mock
    private SnsClient sns;

    private SmsOtpService service;
    private AutoCloseable mocks;      // to close MockitoAnnotations

    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        service = new SmsOtpService(Region.EU_NORTH_1);
        // Inject the mock SnsClient (replace the internally created one)
        setFinalField(service, "sns", sns);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    /* ------------------------------------------------------------------
     * newCode()
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("newCode returns the default test code for test numbers")
    void newCode_testPhone_returnsDefault() {
        assertEquals(SmsOtpService.DEFAULT_TEST_CODE,
                     service.newCode("+01234567890"));
    }

    @Test
    @DisplayName("newCode generates a six-digit zero-padded random string for real numbers")
    void newCode_realPhone_returnsSixDigits() {
        String code = service.newCode("+15551234567");

        assertAll(
                () -> assertEquals(6, code.length()),
                () -> assertTrue(code.matches("\\d{6}"), "Code is not numeric")
        );
    }

    /* ------------------------------------------------------------------
     * send()
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("send() skips publishing for test numbers")
    void send_testPhone_skipsPublish() {
        service.send("+09999999999", "444444");

        verify(sns, never()).publish(any(PublishRequest.class));
    }

    @Test
    @DisplayName("send() publishes correctly for valid E.164 numbers")
    void send_realPhone_publishesMessage() {
        when(sns.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("mid-1").build());

        String phone = "+15550001111";
        String code  = "654321";

        service.send(phone, code);

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(sns, times(1)).publish(captor.capture());

        PublishRequest req = captor.getValue();
        assertAll(
                () -> assertEquals(phone, req.phoneNumber()),
                () -> assertEquals("Your verification code: " + code, req.message()),
                () -> assertEquals("Transactional",
                                   req.messageAttributes()
                                      .get("AWS.SNS.SMS.SMSType")
                                      .stringValue())
        );
    }

    @Test
    @DisplayName("send() throws InvalidPhoneNumberException for malformed numbers")
    void send_invalidPhone_throws() {
        assertThrows(InvalidPhoneNumberException.class,
                     () -> service.send("123-not-e164", "111111"));
    }

    @Test
    @DisplayName("send() wraps SNS failures into SmsDeliveryException")
    void send_snsException_wrapped() {
        when(sns.publish(any(PublishRequest.class)))
                .thenThrow(SnsException.builder().message("boom").build());

        assertThrows(SmsDeliveryException.class,
                     () -> service.send("+15550002222", "222222"));
    }

    /* ------------------------------------------------------------------
     * isTestPhoneNumber()
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("isTestPhoneNumber detects numbers that start with +0")
    void isTestPhone_detection() {
        assertTrue (SmsOtpService.isTestPhoneNumber("+01234"));
        assertFalse(SmsOtpService.isTestPhoneNumber("+1555"));
    }

    /* ------------------------------------------------------------------
     * Reflection helper
     * ------------------------------------------------------------------ */

    private static void setFinalField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}