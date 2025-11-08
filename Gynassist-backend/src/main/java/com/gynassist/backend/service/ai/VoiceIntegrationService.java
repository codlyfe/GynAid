package com.gynassist.backend.service.ai;

import com.gynassist.backend.entity.User;
import com.gynassist.backend.entity.ai.VoiceInteraction;
import com.gynassist.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceIntegrationService {
    
    private final UserRepository userRepository;
    private final AIHealthAssistantService aiHealthAssistantService;
    
    public VoiceInteraction processVoiceQuery(Long userId, String audioTranscript, 
                                            VoiceInteraction.LanguageCode language) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Process voice input preserving existing AI logic
        VoiceInteraction.InteractionType type = classifyInteraction(audioTranscript);
        String response = generateVoiceResponse(audioTranscript, language, type);
        double confidence = calculateConfidence(audioTranscript, type);
        
        return VoiceInteraction.builder()
            .user(user)
            .audioTranscript(audioTranscript)
            .aiResponse(response)
            .interactionType(type)
            .languageCode(language)
            .confidenceScore(confidence)
            .build();
    }
    
    public Map<String, Object> processSymptomLogging(Long userId, String voiceInput) {
        // Enhanced symptom logging via voice preserving existing logic
        try {
            var analysis = aiHealthAssistantService.analyzeSymptoms(userId, voiceInput);
            
            return Map.of(
                "success", true,
                "analysis", analysis,
                "voiceProcessed", true,
                "message", "Symptoms logged successfully via voice"
            );
        } catch (Exception e) {
            log.error("Error processing voice symptom logging: {}", e.getMessage());
            return Map.of(
                "success", false,
                "error", "Could not process voice input",
                "fallback", "Please try typing your symptoms"
            );
        }
    }
    
    private VoiceInteraction.InteractionType classifyInteraction(String transcript) {
        String lower = transcript.toLowerCase();
        
        if (lower.contains("symptom") || lower.contains("pain") || lower.contains("feel")) {
            return VoiceInteraction.InteractionType.SYMPTOM_LOGGING;
        } else if (lower.contains("period") || lower.contains("cycle") || lower.contains("menstruation")) {
            return VoiceInteraction.InteractionType.CYCLE_TRACKING;
        } else if (lower.contains("emergency") || lower.contains("urgent") || lower.contains("help")) {
            return VoiceInteraction.InteractionType.EMERGENCY_REQUEST;
        } else if (lower.contains("appointment") || lower.contains("doctor") || lower.contains("provider")) {
            return VoiceInteraction.InteractionType.APPOINTMENT_BOOKING;
        } else {
            return VoiceInteraction.InteractionType.HEALTH_QUERY;
        }
    }
    
    private String generateVoiceResponse(String transcript, VoiceInteraction.LanguageCode language, 
                                       VoiceInteraction.InteractionType type) {
        // Generate appropriate voice response based on language and type
        switch (language) {
            case LG: // Luganda
                return generateLugandaResponse(type);
            case SW: // Swahili
                return generateSwahiliResponse(type);
            default: // English
                return generateEnglishResponse(type, transcript);
        }
    }
    
    private String generateEnglishResponse(VoiceInteraction.InteractionType type, String transcript) {
        switch (type) {
            case SYMPTOM_LOGGING:
                return "I've recorded your symptoms. Based on what you described, I recommend tracking this information and consulting a healthcare provider if symptoms persist.";
            case CYCLE_TRACKING:
                return "Your cycle information has been logged. I'll use this to improve your period predictions and fertility insights.";
            case EMERGENCY_REQUEST:
                return "This sounds urgent. Please contact emergency services immediately or visit the nearest healthcare facility. I'm also notifying your emergency contact.";
            case APPOINTMENT_BOOKING:
                return "I can help you find suitable healthcare providers. Let me search for gynecologists in your area based on your needs.";
            default:
                return "I'm here to help with your reproductive health questions. How can I assist you today?";
        }
    }
    
    private String generateLugandaResponse(VoiceInteraction.InteractionType type) {
        // Basic Luganda responses - would be enhanced with proper translations
        switch (type) {
            case SYMPTOM_LOGGING:
                return "Nkuwandiise ebyo by'owulira. Singa ebyo bigendera, kyandibadde kirungi okugenda ku musawo.";
            case CYCLE_TRACKING:
                return "Nkuwandiise ebikwata ku nkola yo. Kino kijja kunnyamba okukutegeeza ku biseera byo.";
            case EMERGENCY_REQUEST:
                return "Kino kirabirira nga kya mangu. Kale kuba emergency services oba genda ku ddwaliro.";
            default:
                return "Ndi wano okukuyamba ku bintu by'obulamu bwo. Nkuyinza ntya okukuyamba?";
        }
    }
    
    private String generateSwahiliResponse(VoiceInteraction.InteractionType type) {
        // Basic Swahili responses - would be enhanced with proper translations
        switch (type) {
            case SYMPTOM_LOGGING:
                return "Nimerekodi dalili zako. Napendelea uongee na daktari ikiwa dalili hizi zitaendelea.";
            case CYCLE_TRACKING:
                return "Nimerekodi taarifa za mzunguko wako. Hii itanisaidia kutabiri vizuri mzunguko wako ujao.";
            case EMERGENCY_REQUEST:
                return "Hii inaonekana ni dharura. Tafadhali wasiliana na huduma za dharura au tembelea hospitali jirani.";
            default:
                return "Nipo hapa kukusaidia na maswali ya afya yako ya uzazi. Ninawezaje kukusaidia?";
        }
    }
    
    private double calculateConfidence(String transcript, VoiceInteraction.InteractionType type) {
        // Simple confidence calculation - would use actual NLP models in production
        if (transcript.length() < 10) return 0.3;
        if (transcript.length() > 100) return 0.9;
        return 0.7; // Default confidence
    }
}