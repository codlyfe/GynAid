package com.gynassist.backend.controller.ai;

import com.gynassist.backend.entity.ai.VoiceInteraction;
import com.gynassist.backend.service.ai.VoiceIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/voice")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VoiceController {
    
    private final VoiceIntegrationService voiceIntegrationService;
    
    @PostMapping("/process")
    public ResponseEntity<VoiceInteraction> processVoiceInput(
            @RequestParam Long userId,
            @RequestBody Map<String, String> request) {
        
        String transcript = request.get("transcript");
        String languageCode = request.getOrDefault("language", "EN");
        
        if (transcript == null || transcript.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        VoiceInteraction.LanguageCode language = VoiceInteraction.LanguageCode.valueOf(languageCode);
        VoiceInteraction interaction = voiceIntegrationService.processVoiceQuery(userId, transcript, language);
        
        return ResponseEntity.ok(interaction);
    }
    
    @PostMapping("/log-symptoms")
    public ResponseEntity<Map<String, Object>> logSymptomsViaVoice(
            @RequestParam Long userId,
            @RequestBody Map<String, String> request) {
        
        String voiceInput = request.get("symptoms");
        if (voiceInput == null || voiceInput.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Map<String, Object> result = voiceIntegrationService.processSymptomLogging(userId, voiceInput);
        return ResponseEntity.ok(result);
    }
}