package com.gynaid.backend.service;

import com.gynaid.backend.entity.Appointment;
import com.gynaid.backend.entity.AppointmentAuditTrail;
import com.gynaid.backend.entity.User;
import com.gynaid.backend.repository.AppointmentRepository;
import com.gynaid.backend.repository.AppointmentAuditTrailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    // Add missing createAuditTrailEntry method
    private void createAuditTrailEntry(Appointment appointment, User user, AppointmentAuditTrail.AuditAction action,
                                     String description, String previousStatus, String newStatus) {
        // This is a placeholder implementation - in real app would create audit trail entry
        log.info("Creating audit trail: {} by {} for appointment {}", action, user.getEmail(), appointment.getId());
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Transactional
    public Appointment bookAppointment(BookAppointmentRequest request, User client) {
        log.info("Booking appointment for client: {}", client.getEmail());
        
        // This is a simplified implementation - in reality you'd need to:
        // 1. Validate provider availability
        // 2. Check for scheduling conflicts
        // 3. Handle payment processing
        
        Appointment appointment = Appointment.builder()
            .client(client)
            .provider(request.getProvider())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .notes(request.getNotes())
            .status(Appointment.AppointmentStatus.PENDING)
            .paymentStatus(Appointment.PaymentStatus.UNPAID)
            .build();
        
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment booked successfully with ID: {}", saved.getId());
        
        return saved;
    }

    @Transactional(readOnly = true)
    public UserAppointmentsResponse getClientAppointments(User client, int page, int size, Appointment.AppointmentStatus status) {
        log.info("Getting appointments for client: {}", client.getEmail());
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        Page<Appointment> appointments;
        
        if (status != null) {
            appointments = appointmentRepository.findByClientAndStatus(client, status, pageable);
        } else {
            appointments = appointmentRepository.findByClient(client, pageable);
        }
        
        return UserAppointmentsResponse.builder()
            .appointments(appointments.getContent())
            .totalPages(appointments.getTotalPages())
            .totalElements(appointments.getTotalElements())
            .currentPage(appointments.getNumber())
            .build();
    }

    /**
     * Provider approves an appointment
     */
    @Transactional
    public Appointment approveAppointment(Long appointmentId, User provider, String notes) {
        log.info("Provider {} approving appointment: {}", provider.getEmail(), appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Verify the provider owns this appointment
        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized: You can only manage your own appointments");
        }
        
        String previousStatus = appointment.getStatus().name();
        
        // Update appointment status
        appointment.setStatus(Appointment.AppointmentStatus.APPROVED);
        if (notes != null) {
            appointment.setProviderNotes(notes);
        }
        
        Appointment approved = appointmentRepository.save(appointment);
        
        // Add audit trail entry
        createAuditTrailEntry(appointment, provider, AppointmentAuditTrail.AuditAction.APPROVED,
                          "Appointment approved by provider", previousStatus, Appointment.AppointmentStatus.APPROVED.name());
        
        log.info("Appointment {} approved successfully", appointmentId);
        return approved;
    }

    /**
     * Provider declines an appointment
     */
    @Transactional
    public Appointment declineAppointment(Long appointmentId, User provider, String reason) {
        log.info("Provider {} declining appointment: {}", provider.getEmail(), appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Verify the provider owns this appointment
        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized: You can only manage your own appointments");
        }
        
        String previousStatus = appointment.getStatus().name();
        
        // Update appointment status
        appointment.setStatus(Appointment.AppointmentStatus.DECLINED);
        if (reason != null) {
            appointment.setProviderNotes("Declined: " + reason);
        }
        
        Appointment declined = appointmentRepository.save(appointment);
        
        // Add audit trail entry
        createAuditTrailEntry(appointment, provider, AppointmentAuditTrail.AuditAction.DECLINED,
                          "Appointment declined by provider: " + reason,
                          previousStatus, Appointment.AppointmentStatus.DECLINED.name());
        
        log.info("Appointment {} declined successfully", appointmentId);
        return declined;
    }

    /**
     * Cancel appointment
     */
    @Transactional
    public Appointment cancelAppointment(Long appointmentId, User client, String reason) {
        log.info("Cancelling appointment: {} for client: {}", appointmentId, client.getEmail());
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
        if (!appointment.getClient().getId().equals(client.getId())) {
            throw new RuntimeException("Unauthorized: Cannot cancel someone else's appointment");
        }
        
        appointment.cancel(reason);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment rescheduleAppointment(Long appointmentId, User client, LocalDateTime newStartTime, String reason) {
        log.info("Rescheduling appointment: {} for client: {}", appointmentId, client.getEmail());
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
        if (!appointment.getClient().getId().equals(client.getId())) {
            throw new RuntimeException("Unauthorized: Cannot reschedule someone else's appointment");
        }
        
        if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
            throw new RuntimeException("Can only reschedule pending appointments");
        }
        
        appointment.setStartTime(newStartTime);
        appointment.setEndTime(newStartTime.plusMinutes(30)); // Assuming 30-minute appointments
        appointment.addAuditEntry("RESCHEDULED", "Appointment rescheduled: " + reason);
        
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public UserAppointmentsResponse getProviderAppointments(User provider, int page, int size, Appointment.AppointmentStatus status) {
        log.info("Getting appointments for provider: {}", provider.getEmail());
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        Page<Appointment> appointments;
        
        if (status != null) {
            appointments = appointmentRepository.findByProviderAndStatus(provider, status, pageable);
        } else {
            appointments = appointmentRepository.findByProvider(provider, pageable);
        }
        
        return UserAppointmentsResponse.builder()
            .appointments(appointments.getContent())
            .totalPages(appointments.getTotalPages())
            .totalElements(appointments.getTotalElements())
            .currentPage(appointments.getNumber())
            .build();
    }

    @Transactional
    public Appointment approveAppointment(Long appointmentId, User provider) {
        log.info("Approving appointment: {} for provider: {}", appointmentId, provider.getEmail());
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized: Cannot approve someone else's appointment");
        }
        
        appointment.approve();
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment declineAppointmentSimple(Long appointmentId, User provider, String reason) {
        log.info("Declining appointment: {} for provider: {}", appointmentId, provider.getEmail());
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized: Cannot decline someone else's appointment");
        }
        
        appointment.decline();
        appointment.addAuditEntry("DECLINED", "Appointment declined: " + reason);
        
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment completeAppointment(Long appointmentId, User provider, String providerNotes) {
        log.info("Completing appointment: {} for provider: {}", appointmentId, provider.getEmail());
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized: Cannot complete someone else's appointment");
        }
        
        appointment.complete();
        appointment.setProviderNotes(providerNotes);
        
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment markNoShow(Long appointmentId, User provider) {
        log.info("Marking no-show for appointment: {} by provider: {}", appointmentId, provider.getEmail());
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
        if (!appointment.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized: Cannot mark no-show for someone else's appointment");
        }
        
        appointment.markNoShow();
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public AdminAppointmentsResponse getAllAppointments(int page, int size, Appointment.AppointmentStatus status, LocalDateTime from, LocalDateTime to) {
        log.info("Getting all appointments for admin");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        Page<Appointment> appointments;
        
        if (status != null && from != null && to != null) {
            appointments = appointmentRepository.findByStatusAndStartTimeBetween(status, from, to, pageable);
        } else if (status != null) {
            appointments = appointmentRepository.findByStatus(status, pageable);
        } else if (from != null && to != null) {
            appointments = appointmentRepository.findByStartTimeBetween(from, to, pageable);
        } else {
            appointments = appointmentRepository.findAll(pageable);
        }
        
        return AdminAppointmentsResponse.builder()
            .appointments(appointments.getContent())
            .totalPages(appointments.getTotalPages())
            .totalElements(appointments.getTotalElements())
            .currentPage(appointments.getNumber())
            .build();
    }

    @Transactional(readOnly = true)
    public List<AppointmentAuditTrail> getAuditTrail(Long appointmentId) {
        log.info("Getting audit trail for appointment: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
        return appointment.getAuditTrail();
    }

    @Transactional
    public Appointment adminOverride(Long appointmentId, Appointment.AppointmentStatus newStatus, User admin, String reason) {
        log.info("Admin override for appointment: {} to status: {}", appointmentId, newStatus);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        appointment.setStatus(newStatus);
        appointment.addAuditEntry("ADMIN_OVERRIDE", "Admin override: " + reason);
        
        return appointmentRepository.save(appointment);
    }

    // Request/Response DTOs
    @lombok.Builder
    public static class BookAppointmentRequest {
        private User provider;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String notes;

        public User getProvider() { return provider; }
        public void setProvider(User provider) { this.provider = provider; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class UserAppointmentsResponse {
        private List<Appointment> appointments;
        private int totalPages;
        private long totalElements;
        private int currentPage;

        public static class Builder {
            private UserAppointmentsResponse result = new UserAppointmentsResponse();

            public Builder appointments(List<Appointment> appointments) {
                result.appointments = appointments;
                return this;
            }

            public Builder totalPages(int totalPages) {
                result.totalPages = totalPages;
                return this;
            }

            public Builder totalElements(long totalElements) {
                result.totalElements = totalElements;
                return this;
            }

            public Builder currentPage(int currentPage) {
                result.currentPage = currentPage;
                return this;
            }

            public UserAppointmentsResponse build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public List<Appointment> getAppointments() { return appointments; }
        public int getTotalPages() { return totalPages; }
        public long getTotalElements() { return totalElements; }
        public int getCurrentPage() { return currentPage; }
    }

    public static class AdminAppointmentsResponse {
        private List<Appointment> appointments;
        private int totalPages;
        private long totalElements;
        private int currentPage;

        public static class Builder {
            private AdminAppointmentsResponse result = new AdminAppointmentsResponse();

            public Builder appointments(List<Appointment> appointments) {
                result.appointments = appointments;
                return this;
            }

            public Builder totalPages(int totalPages) {
                result.totalPages = totalPages;
                return this;
            }

            public Builder totalElements(long totalElements) {
                result.totalElements = totalElements;
                return this;
            }

            public Builder currentPage(int currentPage) {
                result.currentPage = currentPage;
                return this;
            }

            public AdminAppointmentsResponse build() {
                return result;
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public List<Appointment> getAppointments() { return appointments; }
        public int getTotalPages() { return totalPages; }
        public long getTotalElements() { return totalElements; }
        public int getCurrentPage() { return currentPage; }
    }
}