package com.gynaid.backend.error;

import com.gynaid.backend.service.ConsultationService.BookConsultationRequest;
import com.gynaid.backend.service.ConsultationService.BookingResponse;
import com.gynaid.backend.service.ConsultationService.PaymentRequest;
import com.gynaid.backend.service.ConsultationService.PaymentResponse;
import com.gynaid.backend.entity.Consultation;
import com.gynaid.backend.entity.HealthcareProvider;
import com.gynaid.backend.entity.User;
import com.gynaid.backend.repository.ConsultationRepository;
import com.gynaid.backend.repository.HealthcareProviderRepository;
import com.gynaid.backend.repository.UserRepository;
import com.gynaid.backend.service.ConsultationService;
import com.gynaid.backend.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for simulating and validating ConsultationService error scenarios
 * Tests the fixes applied to dependency injection, error handling, and type safety
 */
@ExtendWith(MockitoExtension.class)
public class ConsultationServiceErrorTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private HealthcareProviderRepository providerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private ConsultationService consultationService;

    private User testUser;
    private HealthcareProvider testProvider;
    private BookConsultationRequest validRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .build();

        // Setup test provider
        testProvider = HealthcareProvider.builder()
            .id(1L)
            .name("Dr. Smith")
            .email("doctor@example.com")
            .consultationFee(50.0) // Double field, not method
            .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
            .build();

        // Setup valid booking request
        validRequest = new BookConsultationRequest();
        validRequest.setProviderId(1L);
        validRequest.setScheduledDateTime(LocalDateTime.now().plusDays(1));
        validRequest.setType(Consultation.ConsultationType.VIDEO_CALL);
        validRequest.setClientNotes("Test consultation");
    }

    @Test
    void testSuccessfulBookingScenario() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
            .thenReturn(Optional.of(testUser));
        when(providerRepository.findById(testProvider.getId()))
            .thenReturn(Optional.of(testProvider));
        when(consultationRepository.save(any(Consultation.class)))
            .thenAnswer(invocation -> {
                Consultation consultation = invocation.getArgument(0);
                consultation.setId(1L);
                return consultation;
            });

        // Act
        BookingResponse response = consultationService.bookConsultation(validRequest, testUser.getEmail());

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getConsultationId());
        // Use setScale to match the service's rounding behavior
        BigDecimal expectedFee = BigDecimal.valueOf(50.0).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedAppFee = BigDecimal.valueOf(5.0).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = BigDecimal.valueOf(55.0).setScale(2, RoundingMode.HALF_UP);
        
        assertEquals(expectedFee, response.getConsultationFee());
        assertEquals(expectedAppFee, response.getAppFee());
        assertEquals(expectedTotal, response.getTotalAmount());
    }

    @Test
    void testUserNotFoundError() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com"))
            .thenReturn(Optional.empty());

        // Act
        BookingResponse response = consultationService.bookConsultation(validRequest, "nonexistent@example.com");

        // Assert - Service handles error gracefully, returning response instead of throwing
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("User not found"));
    }

    @Test
    void testProviderNotFoundError() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
            .thenReturn(Optional.of(testUser));
        when(providerRepository.findById(999L))
            .thenReturn(Optional.empty());

        validRequest.setProviderId(999L);

        // Act
        BookingResponse response = consultationService.bookConsultation(validRequest, testUser.getEmail());

        // Assert - Service handles error gracefully, returning response instead of throwing
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Provider not found"));
    }

    @Test
    void testPaymentProcessingError() {
        // Arrange - Create consultation for testing payment failure
        Consultation savedConsultation = Consultation.builder()
            .id(1L)
            .client(testUser)
            .provider(testProvider)
            .consultationFee(new BigDecimal("50.00"))
            .appFee(new BigDecimal("5.00"))
            .totalAmount(new BigDecimal("55.00"))
            .status(Consultation.ConsultationStatus.PENDING_PAYMENT)
            .paymentStatus(Consultation.PaymentStatus.PENDING)
            .build();

        when(consultationRepository.findById(1L))
            .thenReturn(Optional.of(savedConsultation));
        
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod(Consultation.PaymentMethod.MTN_MOBILE_MONEY);

        // Simulate non-existent consultation to trigger error
        PaymentRequest invalidPaymentRequest = new PaymentRequest();
        invalidPaymentRequest.setPaymentMethod(Consultation.PaymentMethod.MTN_MOBILE_MONEY);

        // Act - Try to process payment for non-existent consultation
        PaymentResponse response = consultationService.processPayment(999L, invalidPaymentRequest, testUser.getEmail());

        // Assert - Service should handle error gracefully
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Consultation not found"));
        assertEquals(Consultation.PaymentStatus.FAILED, response.getStatus());
    }

    @Test
    void testSuccessfulPaymentProcessing() {
        // Arrange
        Consultation savedConsultation = Consultation.builder()
            .id(1L)
            .client(testUser)
            .provider(testProvider)
            .consultationFee(new BigDecimal("50.00"))
            .appFee(new BigDecimal("5.00"))
            .totalAmount(new BigDecimal("55.00"))
            .status(Consultation.ConsultationStatus.PENDING_PAYMENT)
            .paymentStatus(Consultation.PaymentStatus.PENDING)
            .build();

        when(consultationRepository.findById(1L))
            .thenReturn(Optional.of(savedConsultation));
        
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod(Consultation.PaymentMethod.MTN_MOBILE_MONEY);

        // Act - Service processes payment internally

        // Act
        PaymentResponse response = consultationService.processPayment(1L, paymentRequest, testUser.getEmail());

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Payment successful. Consultation confirmed.", response.getMessage());
        assertNotNull(response.getTransactionId());
        assertEquals(Consultation.PaymentStatus.COMPLETED, response.getStatus());
        // Verify transaction ID starts with expected prefix
        assertTrue(response.getTransactionId().startsWith("TXN_"));
    }

    @Test
    void testProviderConsultationFeeHandling() {
        // Test the fix for double field access error
        // Previously this would fail because consultationFee was treated as a method
        testProvider.setConsultationFee(75.0); // Setting the Double field
        when(userRepository.findByEmail(testUser.getEmail()))
            .thenReturn(Optional.of(testUser));
        when(providerRepository.findById(testProvider.getId()))
            .thenReturn(Optional.of(testProvider));
        when(consultationRepository.save(any(Consultation.class)))
            .thenAnswer(invocation -> {
                Consultation consultation = invocation.getArgument(0);
                consultation.setId(2L);
                return consultation;
            });

        // Act
        BookingResponse response = consultationService.bookConsultation(validRequest, testUser.getEmail());

        // Assert - Verify correct fee calculation with proper precision
        assertTrue(response.isSuccess());
        BigDecimal expectedFee = BigDecimal.valueOf(75.0).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedAppFee = BigDecimal.valueOf(7.5).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = BigDecimal.valueOf(82.5).setScale(2, RoundingMode.HALF_UP);
        
        assertEquals(expectedFee, response.getConsultationFee());
        assertEquals(expectedAppFee, response.getAppFee());
        assertEquals(expectedTotal, response.getTotalAmount());
    }
}