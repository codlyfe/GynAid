package com.gynaid.backend.service;

import com.gynaid.backend.entity.User;
import com.gynaid.backend.entity.provider.ProviderPracticeInfo;
import com.gynaid.backend.repository.provider.ProviderPracticeInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProviderPracticeInfoService {
    
    private final ProviderPracticeInfoRepository practiceInfoRepository;
    
    public Optional<ProviderPracticeInfo> getPracticeInfo(Long providerId) {
        return practiceInfoRepository.findByProviderId(providerId);
    }
    
    public ProviderPracticeInfo createOrUpdatePracticeInfo(User provider, ProviderPracticeInfo practiceInfo) {
        Optional<ProviderPracticeInfo> existing = practiceInfoRepository.findByProviderId(provider.getId());
        
        if (existing.isPresent()) {
            ProviderPracticeInfo existingInfo = existing.get();
            updatePracticeInfoFields(existingInfo, practiceInfo);
            return practiceInfoRepository.save(existingInfo);
        } else {
            practiceInfo.setProvider(provider);
            return practiceInfoRepository.save(practiceInfo);
        }
    }
    
    public ProviderPracticeInfo updateConsultationFees(Long providerId, Double virtualFee, Double inPersonFee, Double homeVisitFee) {
        ProviderPracticeInfo practiceInfo = practiceInfoRepository.findByProviderId(providerId)
            .orElseThrow(() -> new RuntimeException("Practice info not found"));
        
        if (virtualFee != null) practiceInfo.setVirtualConsultationFee(virtualFee);
        if (inPersonFee != null) practiceInfo.setInPersonConsultationFee(inPersonFee);
        if (homeVisitFee != null) practiceInfo.setHomeVisitFee(homeVisitFee);
        
        return practiceInfoRepository.save(practiceInfo);
    }
    
    public ProviderPracticeInfo updatePaymentMethods(Long providerId, String paymentMethods, String mobileMoneyNumber, ProviderPracticeInfo.MobileMoneyProvider provider) {
        ProviderPracticeInfo practiceInfo = practiceInfoRepository.findByProviderId(providerId)
            .orElseThrow(() -> new RuntimeException("Practice info not found"));
        
        if (paymentMethods != null) practiceInfo.setPaymentMethods(paymentMethods);
        if (mobileMoneyNumber != null) practiceInfo.setMobileMoneyNumber(mobileMoneyNumber);
        if (provider != null) practiceInfo.setMobileMoneyProvider(provider);
        
        return practiceInfoRepository.save(practiceInfo);
    }
    
    public ProviderPracticeInfo updateStripeAccount(Long providerId, String stripeAccountId, Boolean verified) {
        ProviderPracticeInfo practiceInfo = practiceInfoRepository.findByProviderId(providerId)
            .orElseThrow(() -> new RuntimeException("Practice info not found"));
        
        if (stripeAccountId != null) practiceInfo.setStripeAccountId(stripeAccountId);
        if (verified != null) practiceInfo.setStripeAccountVerified(verified);
        
        return practiceInfoRepository.save(practiceInfo);
    }
    
    private void updatePracticeInfoFields(ProviderPracticeInfo existing, ProviderPracticeInfo updated) {
        if (updated.getPracticeName() != null) existing.setPracticeName(updated.getPracticeName());
        if (updated.getPracticeType() != null) existing.setPracticeType(updated.getPracticeType());
        if (updated.getPhysicalAddress() != null) existing.setPhysicalAddress(updated.getPhysicalAddress());
        if (updated.getPracticeLocation() != null) existing.setPracticeLocation(updated.getPracticeLocation());
        if (updated.getCity() != null) existing.setCity(updated.getCity());
        if (updated.getDistrict() != null) existing.setDistrict(updated.getDistrict());
        if (updated.getContactPhone() != null) existing.setContactPhone(updated.getContactPhone());
        if (updated.getContactEmail() != null) existing.setContactEmail(updated.getContactEmail());
        if (updated.getOperatingHours() != null) existing.setOperatingHours(updated.getOperatingHours());
        if (updated.getServicesOffered() != null) existing.setServicesOffered(updated.getServicesOffered());
    }
}
