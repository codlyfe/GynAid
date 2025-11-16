package com.gynaid.backend.error;

import com.gynaid.backend.controller.ConsultationController.*;
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
        assertEquals(new BigDecimal("50.00"), response.getConsultationFee());
        assertEquals(new BigDecimal("5.00"), response.getAppFee());
        assertEquals(new BigDecimal("55.00"), response.getTotalAmount());
    }

    @Test
    void testUserNotFoundError() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com"))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            consultationService.bookConsultation(validRequest, "nonexistent@example.com");
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testProviderNotFoundError() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
            .thenReturn(Optional.of(testUser));
        when(providerRepository.findById(999L))
            .thenReturn(Optional.empty());

        validRequest.setProviderId(999L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            consultationService.bookConsultation(validRequest, testUser.getEmail());
        });
        assertEquals("Provider not found", exception.getMessage());
    }

    @Test
    void testNullRequestHandling() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            consultationService.bookConsultation(null, testUser.getEmail());
        });
    }

    @Test
    void testPaymentProcessingError() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
            .thenReturn(Optional.of(testUser));
        when(providerRepository.findById(testProvider.getId()))
            .thenReturn(Optional.of(testProvider));
        
        Consultation savedConsultation = Consultation.builder()
            .id(1L)
            .client(testUser)
            .provider(testProvider)
            .consultationFee(new BigDecimal("50.00"))
            .appFee(new BigDecimal("5.00"))
            .totalAmount(new BigDecimal("55.00"))
            .build();

        when(consultationRepository.findById(1L))
            .thenReturn(Optional.of(savedConsultation));
        
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod(Consultation.PaymentMethod.MTN_MOBILE_MONEY);

        PaymentService.PaymentResult failedResult = PaymentService.PaymentResult.builder()
            .success(false)
            .error("Payment gateway error")
            .build();

        when(paymentService.processPayment(any()))
            .thenReturn(failedResult);

        // Act
        PaymentResponse response = consultationService.processPayment(1L, paymentRequest, testUser.getEmail());

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Payment failed: Payment gateway error", response.getMessage());
        assertEquals(Consultation.PaymentStatus.FAILED, response.getStatus());
    }

    @Test
    void testUnauthorizedPaymentAccess() {
        // Arrange
        Consultation savedConsultation = Consultation.builder()
            .id(1L)
            .client(testUser)
            .provider(testProvider)
            .consultationFee(new BigDecimal("50.00"))
            .build();

        when(consultationRepository.findById(1L))
            .thenReturn(Optional.of(savedConsultation));

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod(Consultation.PaymentMethod.MTN_MOBILE_MONEY);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            consultationService.processPayment(1L, paymentRequest, "different@example.com");
        });
        assertEquals("Unauthorized access to consultation", exception.getMessage());
    }

    @Test
    void testNullPaymentMethod() {
        // Arrange
        Consultation savedConsultation = Consultation.builder()
            .id(1L)
            .client(testUser)
            .provider(testProvider)
            .build();

        when(consultationRepository.findById(1L))
            .thenReturn(Optional.of(savedConsultation));

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod(null);

        // Act & Assert - Should handle null gracefully
        assertDoesNotThrow(() -> {
            PaymentResponse response = consultationService.processPayment(1L, paymentRequest, testUser.getEmail());
            // Verify payment method is handled properly
            assertNotNull(response);
        });
    }

    @Test
    void testConsultationNotFoundPayment() {
        // Arrange
        when(consultationRepository.findById(999L))
            .thenReturn(Optional.empty());

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod(Consultation.PaymentMethod.MTN_MOBILE_MONEY);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            consultationService.processPayment(999L, paymentRequest, testUser.getEmail());
        });
        assertEquals("Consultation not found", exception.getMessage());
    }

    @Test
    void testGetPaymentMethodsForInvalidConsultation() {
        // Arrange
        when(consultationRepository.findById(999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            consultationService.getPaymentMethods(999L);
        });
        assertEquals("Consultation not found", exception.getMessage());
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
            .build();

        when(consultationRepository.findById(1L))
            .thenReturn(Optional.of(savedConsultation));
        
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod(Consultation.PaymentMethod.MTN_MOBILE_MONEY);

        PaymentService.PaymentResult successfulResult = PaymentService.PaymentResult.builder()
            .success(true)
            .transactionId("TXN123456789")
            .build();

        when(paymentService.processPayment(any()))
            .thenReturn(successfulResult);

        // Act
        PaymentResponse response = consultationService.processPayment(1L, paymentRequest, testUser.getEmail());

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Payment successful. Consultation confirmed.", response.getMessage());
        assertEquals("TXN123456789", response.getTransactionId());
        assertEquals(Consultation.PaymentStatus.COMPLETED, response.getStatus());
    }

    /**
     * Test the fix for double field access error
     * Previously this would fail because consultationFee was treated as a method
     */
    @Test
    void testProviderConsultationFeeHandling() {
        // Arrange
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

        // Assert - Verify correct fee calculation
        assertTrue(response.isSuccess());
        assertEquals(new BigDecimal("75.00"), response.getConsultationFee());
        assertEquals(new BigDecimal("7.50"), response.getAppFee());
        assertEquals(new BigDecimal("82.50"), response.getTotalAmount());
    }
}