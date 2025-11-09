package com.gynassist.backend.config;

import com.gynassist.backend.entity.HealthcareProvider;
import com.gynassist.backend.repository.HealthcareProviderRepository;
import com.gynassist.backend.util.LocationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
public class HealthcareProviderInitializer implements CommandLineRunner {

    private final HealthcareProviderRepository providerRepository;

    @Override
    public void run(String... args) throws Exception {
        if (providerRepository.count() == 0) {
            initializeUgandanProviders();
            initializeEastAfricanProviders();
            initializeGlobalProviders();
            System.out.println("Healthcare providers database initialized with " + providerRepository.count() + " providers");
        }
    }

    private void initializeUgandanProviders() {
        // Individual Specialists
        List<HealthcareProvider> ugandanSpecialists = Arrays.asList(
            // Infertility Specialists
            HealthcareProvider.builder()
                .name("Dr. Sarah Nakato")
                .email("dr.nakato@fertility.ug")
                .phoneNumber("+256701234567")
                .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY
                ))
                .location(LocationUtils.toPoint(0.3476, 32.5825))
                .address("Nakasero Medical Centre")
                .district("Kampala")
                .region("Central")
                .country("Uganda")
                .description("Leading fertility specialist with 15+ years experience in IVF and reproductive endocrinology")
                .licenseNumber("UG-DOC-001")
                .experienceYears(15)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.8)
                .reviewCount(95)
                .consultationFee(250000.0)
                .languages(Arrays.asList("English", "Luganda"))
                .services(Arrays.asList("IVF Consultation", "Fertility Assessment", "Hormonal Treatment", "PCOS Management"))
                .build(),

            HealthcareProvider.builder()
                .name("Dr. James Okello")
                .email("dr.okello@reprohealth.ug")
                .phoneNumber("+256702345678")
                .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.FAMILY_PLANNING
                ))
                .location(LocationUtils.toPoint(0.3136, 32.5811))
                .address("Case Medical Centre")
                .district("Kampala")
                .region("Central")
                .country("Uganda")
                .description("Reproductive health specialist focusing on male and female infertility treatment")
                .licenseNumber("UG-DOC-002")
                .experienceYears(12)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.6)
                .reviewCount(78)
                .consultationFee(200000.0)
                .languages(Arrays.asList("English", "Luganda", "Swahili"))
                .services(Arrays.asList("Male Infertility", "Sperm Analysis", "Fertility Counseling", "IUI Procedures"))
                .build(),

            // Endometriosis Specialists
            HealthcareProvider.builder()
                .name("Dr. Grace Namugga")
                .email("dr.namugga@endocare.ug")
                .phoneNumber("+256703456789")
                .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.ENDOMETRIOSIS_CARE,
                    HealthcareProvider.Specialization.GYNECOLOGIC_ONCOLOGY
                ))
                .location(LocationUtils.toPoint(0.3476, 32.5825))
                .address("Mengo Hospital")
                .district("Kampala")
                .region("Central")
                .country("Uganda")
                .description("Specialized endometriosis surgeon with expertise in laparoscopic procedures")
                .licenseNumber("UG-DOC-003")
                .experienceYears(18)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.9)
                .reviewCount(120)
                .consultationFee(300000.0)
                .languages(Arrays.asList("English", "Luganda"))
                .services(Arrays.asList("Endometriosis Surgery", "Laparoscopy", "Pain Management", "Fertility Preservation"))
                .build(),

            HealthcareProvider.builder()
                .name("Dr. Patricia Ssali")
                .email("dr.ssali@womenshealth.ug")
                .phoneNumber("+256704567890")
                .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.ENDOMETRIOSIS_CARE,
                    HealthcareProvider.Specialization.CYCLE_COMPLICATIONS
                ))
                .location(LocationUtils.toPoint(-0.6167, 30.6500))
                .address("Mbarara University Teaching Hospital")
                .district("Mbarara")
                .region("Western")
                .country("Uganda")
                .description("Gynecologist specializing in endometriosis and complex menstrual disorders")
                .licenseNumber("UG-DOC-004")
                .experienceYears(14)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.7)
                .reviewCount(85)
                .consultationFee(180000.0)
                .languages(Arrays.asList("English", "Runyankole", "Luganda"))
                .services(Arrays.asList("Endometriosis Treatment", "Hormonal Therapy", "Menstrual Disorders", "Pelvic Pain Management"))
                .build(),

            // Cycle Complications Specialists
            HealthcareProvider.builder()
                .name("Dr. Moses Kiggundu")
                .email("dr.kiggundu@hormonecare.ug")
                .phoneNumber("+256705678901")
                .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.CYCLE_COMPLICATIONS,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY
                ))
                .location(LocationUtils.toPoint(0.3476, 32.5825))
                .address("Kampala Hospital")
                .district("Kampala")
                .region("Central")
                .country("Uganda")
                .description("Reproductive endocrinologist specializing in PCOS, irregular cycles, and hormonal imbalances")
                .licenseNumber("UG-DOC-005")
                .experienceYears(16)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.5)
                .reviewCount(102)
                .consultationFee(220000.0)
                .languages(Arrays.asList("English", "Luganda"))
                .services(Arrays.asList("PCOS Treatment", "Irregular Periods", "Hormonal Testing", "Ovulation Induction"))
                .build(),

            // Reproductive Infections Specialists
            HealthcareProvider.builder()
                .name("Dr. Rebecca Namusoke")
                .email("dr.namusoke@sticare.ug")
                .phoneNumber("+256706789012")
                .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.REPRODUCTIVE_INFECTIONS,
                    HealthcareProvider.Specialization.FAMILY_PLANNING
                ))
                .location(LocationUtils.toPoint(2.7856, 32.2998))
                .address("Gulu Independent Hospital")
                .district("Gulu")
                .region("Northern")
                .country("Uganda")
                .description("Specialist in sexually transmitted infections and reproductive tract infections")
                .licenseNumber("UG-DOC-006")
                .experienceYears(13)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.4)
                .reviewCount(67)
                .consultationFee(150000.0)
                .languages(Arrays.asList("English", "Acholi", "Luganda"))
                .services(Arrays.asList("STI Testing", "HIV Care", "Contraceptive Services", "Infection Treatment"))
                .build(),

            HealthcareProvider.builder()
                .name("Dr. Andrew Ssekandi")
                .email("dr.ssekandi@infectcare.ug")
                .phoneNumber("+256707890123")
                .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.REPRODUCTIVE_INFECTIONS,
                    HealthcareProvider.Specialization.GENERAL_GYNECOLOGY
                ))
                .location(LocationUtils.toPoint(0.0236, 32.4669))
                .address("Entebbe Grade B Hospital")
                .district("Wakiso")
                .region("Central")
                .country("Uganda")
                .description("Gynecologist with expertise in complex reproductive infections and women's health")
                .licenseNumber("UG-DOC-007")
                .experienceYears(11)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.3)
                .reviewCount(54)
                .consultationFee(120000.0)
                .languages(Arrays.asList("English", "Luganda"))
                .services(Arrays.asList("Bacterial Infections", "Fungal Infections", "PID Treatment", "Preventive Care"))
                .build()
        );

        providerRepository.saveAll(ugandanSpecialists);

        // Major Ugandan Healthcare Providers
        List<HealthcareProvider> ugandanProviders = Arrays.asList(
            // Kampala - Major Hospitals
            HealthcareProvider.builder()
                .name("Mulago National Referral Hospital")
                .email("info@mulago.go.ug")
                .phoneNumber("+256414530692")
                .type(HealthcareProvider.ProviderType.HOSPITAL)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.GENERAL_GYNECOLOGY,
                    HealthcareProvider.Specialization.OBSTETRICS,
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.ENDOMETRIOSIS_CARE
                ))
                .location(LocationUtils.toPoint(0.3476, 32.5825)) // Kampala coordinates
                .address("Mulago Hill")
                .district("Kampala")
                .region("Central")
                .country("Uganda")
                .description("Uganda's largest national referral hospital with comprehensive reproductive health services")
                .licenseNumber("UG-HOSP-001")
                .experienceYears(50)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.2)
                .reviewCount(150)
                .consultationFee(50000.0)
                .languages(Arrays.asList("English", "Luganda", "Swahili"))
                .services(Arrays.asList("Emergency Care", "Maternity Services", "Fertility Treatment", "Gynecological Surgery"))
                .build(),

            HealthcareProvider.builder()
                .name("International Hospital Kampala (IHK)")
                .email("info@ihk.co.ug")
                .phoneNumber("+256312200400")
                .type(HealthcareProvider.ProviderType.HOSPITAL)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY,
                    HealthcareProvider.Specialization.GENERAL_GYNECOLOGY
                ))
                .location(LocationUtils.toPoint(0.3136, 32.5811))
                .address("Namuwongo")
                .district("Kampala")
                .region("Central")
                .country("Uganda")
                .description("Leading private hospital with advanced fertility and reproductive health services")
                .licenseNumber("UG-HOSP-002")
                .experienceYears(25)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.5)
                .reviewCount(200)
                .consultationFee(150000.0)
                .languages(Arrays.asList("English", "Luganda"))
                .services(Arrays.asList("IVF Treatment", "Fertility Counseling", "Reproductive Surgery", "Prenatal Care"))
                .build(),

            // Specialized Fertility Centers
            HealthcareProvider.builder()
                .name("Women's Hospital International and Fertility Centre")
                .email("info@whi.co.ug")
                .phoneNumber("+256414258251")
                .type(HealthcareProvider.ProviderType.SPECIALIST_CENTER)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY,
                    HealthcareProvider.Specialization.FAMILY_PLANNING
                ))
                .location(LocationUtils.toPoint(0.3476, 32.5825))
                .address("Bukoto")
                .district("Kampala")
                .region("Central")
                .country("Uganda")
                .description("Specialized fertility and women's health center with IVF capabilities")
                .licenseNumber("UG-SPEC-001")
                .experienceYears(15)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.7)
                .reviewCount(180)
                .consultationFee(200000.0)
                .languages(Arrays.asList("English", "Luganda"))
                .services(Arrays.asList("IVF", "ICSI", "Fertility Assessment", "Endometriosis Treatment"))
                .build(),

            // Regional Hospitals
            HealthcareProvider.builder()
                .name("Mbarara Regional Referral Hospital")
                .email("info@mbarara-hospital.go.ug")
                .phoneNumber("+256485420763")
                .type(HealthcareProvider.ProviderType.HOSPITAL)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.GENERAL_GYNECOLOGY,
                    HealthcareProvider.Specialization.OBSTETRICS,
                    HealthcareProvider.Specialization.CYCLE_COMPLICATIONS
                ))
                .location(LocationUtils.toPoint(-0.6167, 30.6500))
                .address("Mbarara Hill")
                .district("Mbarara")
                .region("Western")
                .country("Uganda")
                .description("Major regional hospital serving Western Uganda with comprehensive women's health services")
                .licenseNumber("UG-HOSP-003")
                .experienceYears(30)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.0)
                .reviewCount(120)
                .consultationFee(40000.0)
                .languages(Arrays.asList("English", "Runyankole", "Luganda"))
                .services(Arrays.asList("Maternity Care", "Gynecological Surgery", "Family Planning", "Emergency Obstetrics"))
                .build(),

            HealthcareProvider.builder()
                .name("Gulu Regional Referral Hospital")
                .email("info@gulu-hospital.go.ug")
                .phoneNumber("+256471432271")
                .type(HealthcareProvider.ProviderType.HOSPITAL)
                .scope(HealthcareProvider.GeographicScope.UGANDA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.GENERAL_GYNECOLOGY,
                    HealthcareProvider.Specialization.REPRODUCTIVE_INFECTIONS,
                    HealthcareProvider.Specialization.MATERNAL_FETAL_MEDICINE
                ))
                .location(LocationUtils.toPoint(2.7856, 32.2998))
                .address("Gulu Town")
                .district("Gulu")
                .region("Northern")
                .country("Uganda")
                .description("Regional referral hospital serving Northern Uganda with focus on maternal health")
                .licenseNumber("UG-HOSP-004")
                .experienceYears(25)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(3.8)
                .reviewCount(90)
                .consultationFee(35000.0)
                .languages(Arrays.asList("English", "Acholi", "Luganda"))
                .services(Arrays.asList("Maternal Health", "STI Treatment", "Family Planning", "Adolescent Health"))
                .build()
        );

        providerRepository.saveAll(ugandanProviders);
    }

    private void initializeEastAfricanProviders() {
        List<HealthcareProvider> eastAfricanProviders = Arrays.asList(
            // Kenya
            HealthcareProvider.builder()
                .name("Aga Khan University Hospital Nairobi")
                .email("info@aku.edu")
                .phoneNumber("+254203662000")
                .type(HealthcareProvider.ProviderType.HOSPITAL)
                .scope(HealthcareProvider.GeographicScope.EAST_AFRICA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY,
                    HealthcareProvider.Specialization.GYNECOLOGIC_ONCOLOGY
                ))
                .location(LocationUtils.toPoint(-1.2921, 36.8219))
                .address("Third Parklands Avenue")
                .district("Nairobi")
                .region("Nairobi")
                .country("Kenya")
                .description("Leading East African hospital with advanced reproductive medicine services")
                .licenseNumber("KE-HOSP-001")
                .experienceYears(40)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.6)
                .reviewCount(300)
                .consultationFee(8000.0) // KES
                .languages(Arrays.asList("English", "Swahili"))
                .services(Arrays.asList("Advanced IVF", "Genetic Counseling", "Oncofertility", "Reproductive Surgery"))
                .build(),

            // Tanzania
            HealthcareProvider.builder()
                .name("Muhimbili National Hospital")
                .email("info@mnh.or.tz")
                .phoneNumber("+255222150302")
                .type(HealthcareProvider.ProviderType.HOSPITAL)
                .scope(HealthcareProvider.GeographicScope.EAST_AFRICA)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.GENERAL_GYNECOLOGY,
                    HealthcareProvider.Specialization.MATERNAL_FETAL_MEDICINE,
                    HealthcareProvider.Specialization.REPRODUCTIVE_INFECTIONS
                ))
                .location(LocationUtils.toPoint(-6.8160, 39.2803))
                .address("United Nations Road")
                .district("Dar es Salaam")
                .region("Dar es Salaam")
                .country("Tanzania")
                .description("Tanzania's premier referral hospital with comprehensive women's health services")
                .licenseNumber("TZ-HOSP-001")
                .experienceYears(35)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.1)
                .reviewCount(180)
                .consultationFee(50000.0) // TZS
                .languages(Arrays.asList("English", "Swahili"))
                .services(Arrays.asList("High-Risk Pregnancy Care", "Reproductive Health", "STI Management", "Family Planning"))
                .build()
        );

        providerRepository.saveAll(eastAfricanProviders);
    }

    private void initializeGlobalProviders() {
        List<HealthcareProvider> globalProviders = Arrays.asList(
            // Telemedicine Providers
            HealthcareProvider.builder()
                .name("Global Reproductive Health Telemedicine")
                .email("consult@grht.com")
                .phoneNumber("+1-800-REPRO-HEALTH")
                .type(HealthcareProvider.ProviderType.TELEMEDICINE_PROVIDER)
                .scope(HealthcareProvider.GeographicScope.GLOBAL)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.ENDOMETRIOSIS_CARE,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY
                ))
                .address("Virtual Consultations")
                .district("Global")
                .region("Global")
                .country("Global")
                .description("24/7 telemedicine consultations with reproductive health specialists worldwide")
                .licenseNumber("GLOBAL-TELE-001")
                .experienceYears(10)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.4)
                .reviewCount(500)
                .consultationFee(100.0) // USD
                .languages(Arrays.asList("English", "French", "Spanish", "Swahili"))
                .services(Arrays.asList("Virtual Consultations", "Second Opinions", "Treatment Planning", "Follow-up Care"))
                .build(),

            HealthcareProvider.builder()
                .name("International Fertility Network")
                .email("info@ifn-global.org")
                .phoneNumber("+44-20-FERTILITY")
                .type(HealthcareProvider.ProviderType.SPECIALIST_CENTER)
                .scope(HealthcareProvider.GeographicScope.GLOBAL)
                .specializations(Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY
                ))
                .address("Network of Global Clinics")
                .district("Global")
                .region("Global")
                .country("Global")
                .description("International network providing fertility treatments and consultations globally")
                .licenseNumber("GLOBAL-NET-001")
                .experienceYears(20)
                .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
                .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
                .rating(4.3)
                .reviewCount(800)
                .consultationFee(150.0) // USD
                .languages(Arrays.asList("English", "French", "Spanish", "Arabic"))
                .services(Arrays.asList("Global IVF Network", "Cross-border Care", "Medical Tourism", "Specialist Referrals"))
                .build()
        );

        providerRepository.saveAll(globalProviders);
    }
}