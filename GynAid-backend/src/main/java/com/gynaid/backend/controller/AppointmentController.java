package com.gynaid.backend.controller;

import com.gynaid.backend.entity.Appointment;
import com.gynaid.backend.entity.AppointmentAuditTrail;
import com.gynaid.backend.entity.User;
import com.gynaid.backend.service.AppointmentService;
import com.gynaid.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/appointments")
@PreAuthorize("hasAnyRole('CLIENT', 'PROVIDER', 'ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    // CLIENT ENDPOINTS
    @PostMapping("/book")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @RequestBody BookAppointmentRequest request,
            Authentication authentication) {
        
        try {
            User client = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Convert controller request to service request format
            AppointmentService.BookAppointmentRequest serviceRequest = AppointmentService.BookAppointmentRequest.builder()
                .provider(userService.findById(request.getProviderId())
                    .orElseThrow(() -> new RuntimeException("Provider not found")))
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .notes(request.getNotes())
                .build();
            
            Appointment booking = appointmentService.bookAppointment(serviceRequest, client);
            
            Map<String, Object> response = new HashMap<>();
            response.put("appointmentId", booking.getId());
            response.put("status", booking.getStatus().name());
            response.put("message", "Appointment booked successfully");
            response.put("startTime", booking.getStartTime());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error booking appointment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Booking failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> getMyAppointments(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Appointment.AppointmentStatus status) {
        
        try {
            User client = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            AppointmentService.UserAppointmentsResponse response =
                appointmentService.getClientAppointments(client, page, size, status);
            
            Map<String, Object> result = new HashMap<>();
            result.put("appointments", response.getAppointments());
            result.put("totalPages", response.getTotalPages());
            result.put("totalElements", response.getTotalElements());
            result.put("currentPage", response.getCurrentPage());
            
            return new ResponseEntity<>(result, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error getting client appointments", e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Failed to get appointments: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestBody CancelAppointmentRequest request,
            Authentication authentication) {
        
        try {
            User client = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Appointment appointment = appointmentService.cancelAppointment(
                appointmentId, client, request.getReason());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", appointment.getStatus().name());
            response.put("message", "Appointment cancelled successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error cancelling appointment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Cancellation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{appointmentId}/reschedule")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Object>> rescheduleAppointment(
            @PathVariable Long appointmentId,
            @RequestBody RescheduleAppointmentRequest request,
            Authentication authentication) {
        
        try {
            User client = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Appointment appointment = appointmentService.rescheduleAppointment(
                appointmentId, client, request.getNewStartTime(), request.getReason());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newStartTime", appointment.getStartTime());
            response.put("newEndTime", appointment.getEndTime());
            response.put("message", "Appointment rescheduled successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error rescheduling appointment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Reschedule failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // PROVIDER ENDPOINTS
    @GetMapping("/provider/pending")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> getPendingAppointments(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            User provider = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            AppointmentService.UserAppointmentsResponse response =
                appointmentService.getProviderAppointments(provider, page, size, Appointment.AppointmentStatus.PENDING);
            
            Map<String, Object> result = new HashMap<>();
            result.put("appointments", response.getAppointments());
            result.put("totalPages", response.getTotalPages());
            result.put("totalElements", response.getTotalElements());
            result.put("currentPage", response.getCurrentPage());
            
            return new ResponseEntity<>(result, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error getting provider pending appointments", e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Failed to get pending appointments: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{appointmentId}/approve")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> approveAppointment(
            @PathVariable Long appointmentId,
            Authentication authentication) {
        
        try {
            User provider = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Appointment appointment = appointmentService.approveAppointment(appointmentId, provider);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", appointment.getStatus().name());
            response.put("message", "Appointment approved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error approving appointment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Approval failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{appointmentId}/decline")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> declineAppointment(
            @PathVariable Long appointmentId,
            @RequestBody DeclineAppointmentRequest request,
            Authentication authentication) {
        
        try {
            User provider = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Appointment appointment = appointmentService.declineAppointment(
                appointmentId, provider, request.getReason());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", appointment.getStatus().name());
            response.put("message", "Appointment declined");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error declining appointment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Decline failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{appointmentId}/complete")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> completeAppointment(
            @PathVariable Long appointmentId,
            @RequestBody CompleteAppointmentRequest request,
            Authentication authentication) {
        
        try {
            User provider = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Appointment appointment = appointmentService.completeAppointment(
                appointmentId, provider, request.getProviderNotes());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", appointment.getStatus().name());
            response.put("message", "Appointment completed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error completing appointment", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Completion failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{appointmentId}/no-show")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Map<String, Object>> markNoShow(
            @PathVariable Long appointmentId,
            Authentication authentication) {
        
        try {
            User provider = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Appointment appointment = appointmentService.markNoShow(appointmentId, provider);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", appointment.getStatus().name());
            response.put("message", "Marked as no-show");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error marking no-show", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to mark no-show: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ADMIN ENDPOINTS
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Appointment.AppointmentStatus status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        
        try {
            LocalDateTime from = dateFrom != null ? LocalDateTime.parse(dateFrom) : null;
            LocalDateTime to = dateTo != null ? LocalDateTime.parse(dateTo) : null;
            
            AppointmentService.AdminAppointmentsResponse response =
                appointmentService.getAllAppointments(page, size, status, from, to);
            
            Map<String, Object> result = new HashMap<>();
            result.put("appointments", response.getAppointments());
            result.put("totalPages", response.getTotalPages());
            result.put("totalElements", response.getTotalElements());
            result.put("currentPage", response.getCurrentPage());
            
            return new ResponseEntity<>(result, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error getting all appointments", e);
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Failed to get appointments: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{appointmentId}/audit-trail")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    public ResponseEntity<List<AppointmentAuditTrail>> getAuditTrail(@PathVariable Long appointmentId) {
        try {
            List<AppointmentAuditTrail> auditTrail = appointmentService.getAuditTrail(appointmentId);
            return ResponseEntity.ok(auditTrail);
        } catch (Exception e) {
            log.error("Error getting audit trail", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{appointmentId}/admin/override")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminOverride(
            @PathVariable Long appointmentId,
            @RequestBody AdminOverrideRequest request,
            Authentication authentication) {
        
        try {
            User admin = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Appointment appointment = appointmentService.adminOverride(
                appointmentId, request.getNewStatus(), admin, request.getReason());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", appointment.getStatus().name());
            response.put("message", "Appointment status overridden");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in admin override", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Override failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // DTOs
    @lombok.Data
    public static class BookAppointmentRequest {
        private Long providerId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String notes;
    }

    @lombok.Data
    public static class CancelAppointmentRequest {
        private String reason;
    }

    @lombok.Data
    public static class RescheduleAppointmentRequest {
        private LocalDateTime newStartTime;
        private LocalDateTime newEndTime;
        private String reason;
    }

    @lombok.Data
    public static class DeclineAppointmentRequest {
        private String reason;
    }

    @lombok.Data
    public static class CompleteAppointmentRequest {
        private String providerNotes;
    }

    @lombok.Data
    public static class AdminOverrideRequest {
        private Appointment.AppointmentStatus newStatus;
        private String reason;
    }
}