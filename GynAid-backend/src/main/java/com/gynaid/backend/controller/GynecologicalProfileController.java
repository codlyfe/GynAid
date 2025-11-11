package com.gynaid.backend.controller;

import com.gynaid.backend.dto.ApiResponse;
import com.gynaid.backend.dto.client.GynecologicalProfileDto;
import com.gynaid.backend.service.GynecologicalProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/client/gynecological")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GynecologicalProfileController {
    
    private final GynecologicalProfileService gynecologicalProfileService;
    
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<GynecologicalProfileDto>> getGynecologicalProfile(Authentication auth) {
        String email = auth.getName();
        GynecologicalProfileDto profile = gynecologicalProfileService.getGynecologicalProfile(email);
        return ResponseEntity.ok(ApiResponse.<GynecologicalProfileDto>builder()
                .success(true)
                .data(profile)
                .message("Gynecological profile retrieved successfully")
                .build());
    }
    
    @PostMapping("/profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<GynecologicalProfileDto>> createOrUpdateGynecologicalProfile(
            @RequestBody GynecologicalProfileDto profileDto,
            Authentication auth) {
        String email = auth.getName();
        GynecologicalProfileDto profile = gynecologicalProfileService.createOrUpdateGynecologicalProfile(email, profileDto);
        return ResponseEntity.ok(ApiResponse.<GynecologicalProfileDto>builder()
                .success(true)
                .data(profile)
                .message("Gynecological profile updated successfully")
                .build());
    }
    
    @PostMapping("/cycles")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<GynecologicalProfileDto.CycleEntryDto>> addCycleEntry(
            @RequestBody GynecologicalProfileDto.CycleEntryDto cycleDto,
            Authentication auth) {
        String email = auth.getName();
        GynecologicalProfileDto.CycleEntryDto cycle = gynecologicalProfileService.addCycleEntry(email, cycleDto);
        return ResponseEntity.ok(ApiResponse.<GynecologicalProfileDto.CycleEntryDto>builder()
                .success(true)
                .data(cycle)
                .message("Cycle entry added successfully")
                .build());
    }
    
    @GetMapping("/predictions")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getPeriodPredictions(
            @RequestParam(defaultValue = "3") int monthsAhead,
            Authentication auth) {
        String email = auth.getName();
        List<LocalDate> predictions = gynecologicalProfileService.predictNextPeriods(email, monthsAhead);
        return ResponseEntity.ok(ApiResponse.<List<LocalDate>>builder()
                .success(true)
                .data(predictions)
                .message("Period predictions generated successfully")
                .build());
    }
}
