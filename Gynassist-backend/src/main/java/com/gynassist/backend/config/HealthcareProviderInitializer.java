package com.gynassist.backend.config;

import com.gynassist.backend.entity.HealthcareProvider;
import com.gynassist.backend.repository.HealthcareProviderRepository;
import com.gynassist.backend.util.LocationUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

@Component
@Order(2)
public class HealthcareProviderInitializer implements CommandLineRunner {

    private final HealthcareProviderRepository providerRepository;

    @Autowired
    public HealthcareProviderInitializer(HealthcareProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

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
            createInfertilitySpecialist(
                "Dr. Sarah Nakato",
                "dr.nakato@fertility.ug",
                "+256701234567",
                "Nakasero Medical Centre",
                "Kampala",
                "Central",
                0.3476, 32.5825,
                15,
                4.8,
                95,
                250000.0,
                "UG-DOC-001",
                Arrays.asList("IVF Consultation", "Fertility Assessment", "Hormonal Treatment", "PCOS Management"),
                Arrays.asList("English", "Luganda")
            ),
            createInfertilitySpecialist(
                "Dr. James Okello",
                "dr.okello@reprohealth.ug",
                "+256702345678",
                "Case Medical Centre",
                "Kampala",
                "Central",
                0.3136, 32.5811,
                12,
                4.6,
                78,
                200000.0,
                "UG-DOC-002",
                Arrays.asList("Male Infertility", "Sperm Analysis", "Fertility Counseling", "IUI Procedures"),
                Arrays.asList("English", "Luganda", "Swahili")
            ),
            // Endometriosis Specialists
            createEndometriosisSpecialist(
                "Dr. Grace Namugga",
                "dr.namugga@endocare.ug",
                "+256703456789",
                "Mengo Hospital",
                "Kampala",
                "Central",
                0.3476, 32.5825,
                18,
                4.9,
                120,
                300000.0,
                "UG-DOC-003",
                Arrays.asList("Endometriosis Surgery", "Laparoscopy", "Pain Management", "Fertility Preservation"),
                Arrays.asList("English", "Luganda")
            ),
            createEndometriosisSpecialist(
                "Dr. Patricia Ssali",
                "dr.ssali@womenshealth.ug",
                "+256704567890",
                "Mbarara University Teaching Hospital",
                "Mbarara",
                "Western",
                -0.6167, 30.6500,
                14,
                4.7,
                85,
                180000.0,
                "UG-DOC-004",
                Arrays.asList("Endometriosis Treatment", "Hormonal Therapy", "Menstrual Disorders", "Pelvic Pain Management"),
                Arrays.asList("English", "Runyankole", "Luganda")
            ),
            // Cycle Complications Specialists
            createCycleComplicationsSpecialist(
                "Dr. Moses Kiggundu",
                "dr.kiggundu@hormonecare.ug",
                "+256705678901",
                "Kampala Hospital",
                "Kampala",
                "Central",
                0.3476, 32.5825,
                16,
                4.5,
                102,
                220000.0,
                "UG-DOC-005",
                Arrays.asList("PCOS Treatment", "Irregular Periods", "Hormonal Testing", "Ovulation Induction"),
                Arrays.asList("English", "Luganda")
            ),
            // Reproductive Infections Specialists
            createReproductiveInfectionsSpecialist(
                "Dr. Rebecca Namusoke",
                "dr.namusoke@sticare.ug",
                "+256706789012",
                "Gulu Independent Hospital",
                "Gulu",
                "Northern",
                2.7856, 32.2998,
                13,
                4.4,
                67,
                150000.0,
                "UG-DOC-006",
                Arrays.asList("STI Testing", "HIV Care", "Contraceptive Services", "Infection Treatment"),
                Arrays.asList("English", "Acholi", "Luganda")
            ),
            createReproductiveInfectionsSpecialist(
                "Dr. Andrew Ssekandi",
                "dr.ssekandi@infectcare.ug",
                "+256707890123",
                "Entebbe Grade B Hospital",
                "Wakiso",
                "Central",
                0.0236, 32.4669,
                11,
                4.3,
                54,
                120000.0,
                "UG-DOC-007",
                Arrays.asList("Bacterial Infections", "Fungal Infections", "PID Treatment", "Preventive Care"),
                Arrays.asList("English", "Luganda")
            )
        );

        providerRepository.saveAll(ugandanSpecialists);

        // Major Ugandan Healthcare Providers
        List<HealthcareProvider> ugandanProviders = Arrays.asList(
            createHospital(
                "Mulago National Referral Hospital",
                "info@mulago.go.ug",
                "+256414530692",
                "Mulago Hill",
                "Kampala",
                "Central",
                0.3476, 32.5825,
                50,
                4.2,
                150,
                50000.0,
                "UG-HOSP-001",
                Arrays.asList(
                    HealthcareProvider.Specialization.GENERAL_GYNECOLOGY,
                    HealthcareProvider.Specialization.OBSTETRICS,
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.ENDOMETRIOSIS_CARE
                ),
                Arrays.asList("Emergency Care", "Maternity Services", "Fertility Treatment", "Gynecological Surgery"),
                Arrays.asList("English", "Luganda", "Swahili")
            ),
            createHospital(
                "International Hospital Kampala (IHK)",
                "info@ihk.co.ug",
                "+256312200400",
                "Namuwongo",
                "Kampala",
                "Central",
                0.3136, 32.5811,
                25,
                4.5,
                200,
                150000.0,
                "UG-HOSP-002",
                Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY,
                    HealthcareProvider.Specialization.GENERAL_GYNECOLOGY
                ),
                Arrays.asList("IVF Treatment", "Fertility Counseling", "Reproductive Surgery", "Prenatal Care"),
                Arrays.asList("English", "Luganda")
            ),
            createSpecialistCenter(
                "Women's Hospital International and Fertility Centre",
                "info@whi.co.ug",
                "+256414258251",
                "Bukoto",
                "Kampala",
                "Central",
                0.3476, 32.5825,
                15,
                4.7,
                180,
                200000.0,
                "UG-SPEC-001",
                Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY,
                    HealthcareProvider.Specialization.FAMILY_PLANNING
                ),
                Arrays.asList("IVF", "ICSI", "Fertility Assessment", "Endometriosis Treatment"),
                Arrays.asList("English", "Luganda")
            ),
            createHospital(
                "Mbarara Regional Referral Hospital",
                "info@mbarara-hospital.go.ug",
                "+256485420763",
                "Mbarara Hill",
                "Mbarara",
                "Western",
                -0.6167, 30.6500,
                30,
                4.0,
                120,
                40000.0,
                "UG-HOSP-003",
                Arrays.asList(
                    HealthcareProvider.Specialization.GENERAL_GYNECOLOGY,
                    HealthcareProvider.Specialization.OBSTETRICS,
                    HealthcareProvider.Specialization.CYCLE_COMPLICATIONS
                ),
                Arrays.asList("Maternity Care", "Gynecological Surgery", "Family Planning", "Emergency Obstetrics"),
                Arrays.asList("English", "Runyankole", "Luganda")
            ),
            createHospital(
                "Gulu Regional Referral Hospital",
                "info@gulu-hospital.go.ug",
                "+256471432271",
                "Gulu Town",
                "Gulu",
                "Northern",
                2.7856, 32.2998,
                25,
                3.8,
                90,
                35000.0,
                "UG-HOSP-004",
                Arrays.asList(
                    HealthcareProvider.Specialization.GENERAL_GYNECOLOGY,
                    HealthcareProvider.Specialization.REPRODUCTIVE_INFECTIONS,
                    HealthcareProvider.Specialization.MATERNAL_FETAL_MEDICINE
                ),
                Arrays.asList("Maternal Health", "STI Treatment", "Family Planning", "Adolescent Health"),
                Arrays.asList("English", "Acholi", "Luganda")
            )
        );

        providerRepository.saveAll(ugandanProviders);
    }

    private void initializeEastAfricanProviders() {
        List<HealthcareProvider> eastAfricanProviders = Arrays.asList(
            createHospital(
                "Aga Khan University Hospital Nairobi",
                "info@aku.edu",
                "+254203662000",
                "Third Parklands Avenue",
                "Nairobi",
                "Nairobi",
                -1.2921, 36.8219,
                40,
                4.6,
                300,
                8000.0,
                "KE-HOSP-001",
                Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY,
                    HealthcareProvider.Specialization.GYNECOLOGIC_ONCOLOGY
                ),
                Arrays.asList("Advanced IVF", "Genetic Counseling", "Oncofertility", "Reproductive Surgery"),
                Arrays.asList("English", "Swahili")
            ),
            createHospital(
                "Muhimbili National Hospital",
                "info@mnh.or.tz",
                "+255222150302",
                "United Nations Road",
                "Dar es Salaam",
                "Dar es Salaam",
                -6.8160, 39.2803,
                35,
                4.1,
                180,
                50000.0,
                "TZ-HOSP-001",
                Arrays.asList(
                    HealthcareProvider.Specialization.GENERAL_GYNECOLOGY,
                    HealthcareProvider.Specialization.MATERNAL_FETAL_MEDICINE,
                    HealthcareProvider.Specialization.REPRODUCTIVE_INFECTIONS
                ),
                Arrays.asList("High-Risk Pregnancy Care", "Reproductive Health", "STI Management", "Family Planning"),
                Arrays.asList("English", "Swahili")
            )
        );

        providerRepository.saveAll(eastAfricanProviders);
    }

    private void initializeGlobalProviders() {
        List<HealthcareProvider> globalProviders = Arrays.asList(
            createTelemedicineProvider(
                "Global Reproductive Health Telemedicine",
                "consult@grht.com",
                "+1-800-REPRO-HEALTH",
                "Virtual Consultations",
                "Global",
                "Global",
                10,
                4.4,
                500,
                100.0,
                "GLOBAL-TELE-001",
                Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.ENDOMETRIOSIS_CARE,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY
                ),
                Arrays.asList("Virtual Consultations", "Second Opinions", "Treatment Planning", "Follow-up Care"),
                Arrays.asList("English", "French", "Spanish", "Swahili")
            ),
            createTelemedicineProvider(
                "International Fertility Network",
                "info@ifn-global.org",
                "+44-20-FERTILITY",
                "Network of Global Clinics",
                "Global",
                "Global",
                20,
                4.3,
                800,
                150.0,
                "GLOBAL-NET-001",
                Arrays.asList(
                    HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                    HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY
                ),
                Arrays.asList("Global IVF Network", "Cross-border Care", "Medical Tourism", "Specialist Referrals"),
                Arrays.asList("English", "French", "Spanish", "Arabic")
            )
        );

        providerRepository.saveAll(globalProviders);
    }

    // Helper methods to create different types of providers
    private HealthcareProvider createInfertilitySpecialist(String name, String email, String phone, String address,
                                                          String district, String region, double lat, double lng,
                                                          int experienceYears, double rating, int reviewCount,
                                                          double consultationFee, String licenseNumber,
                                                          List<String> services, List<String> languages) {
        return HealthcareProvider.builder()
            .name(name)
            .email(email)
            .phoneNumber(phone)
            .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
            .scope(HealthcareProvider.GeographicScope.UGANDA)
            .specializations(Arrays.asList(
                HealthcareProvider.Specialization.INFERTILITY_SUPPORT,
                HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY
            ))
            .location(LocationUtils.toPoint(lat, lng))
            .address(address)
            .district(district)
            .region(region)
            .country("Uganda")
            .description("Leading fertility specialist with " + experienceYears + "+ years experience in IVF and reproductive endocrinology")
            .licenseNumber(licenseNumber)
            .experienceYears(experienceYears)
            .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
            .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
            .rating(rating)
            .reviewCount(reviewCount)
            .consultationFee(consultationFee)
            .languages(languages)
            .services(services)
            .build();
    }

    private HealthcareProvider createEndometriosisSpecialist(String name, String email, String phone, String address,
                                                            String district, String region, double lat, double lng,
                                                            int experienceYears, double rating, int reviewCount,
                                                            double consultationFee, String licenseNumber,
                                                            List<String> services, List<String> languages) {
        return HealthcareProvider.builder()
            .name(name)
            .email(email)
            .phoneNumber(phone)
            .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
            .scope(HealthcareProvider.GeographicScope.UGANDA)
            .specializations(Arrays.asList(
                HealthcareProvider.Specialization.ENDOMETRIOSIS_CARE,
                HealthcareProvider.Specialization.GYNECOLOGIC_ONCOLOGY
            ))
            .location(LocationUtils.toPoint(lat, lng))
            .address(address)
            .district(district)
            .region(region)
            .country("Uganda")
            .description("Specialized endometriosis surgeon with expertise in laparoscopic procedures")
            .licenseNumber(licenseNumber)
            .experienceYears(experienceYears)
            .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
            .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
            .rating(rating)
            .reviewCount(reviewCount)
            .consultationFee(consultationFee)
            .languages(languages)
            .services(services)
            .build();
    }

    private HealthcareProvider createCycleComplicationsSpecialist(String name, String email, String phone, String address,
                                                                 String district, String region, double lat, double lng,
                                                                 int experienceYears, double rating, int reviewCount,
                                                                 double consultationFee, String licenseNumber,
                                                                 List<String> services, List<String> languages) {
        return HealthcareProvider.builder()
            .name(name)
            .email(email)
            .phoneNumber(phone)
            .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
            .scope(HealthcareProvider.GeographicScope.UGANDA)
            .specializations(Arrays.asList(
                HealthcareProvider.Specialization.CYCLE_COMPLICATIONS,
                HealthcareProvider.Specialization.REPRODUCTIVE_ENDOCRINOLOGY
            ))
            .location(LocationUtils.toPoint(lat, lng))
            .address(address)
            .district(district)
            .region(region)
            .country("Uganda")
            .description("Reproductive endocrinologist specializing in PCOS, irregular cycles, and hormonal imbalances")
            .licenseNumber(licenseNumber)
            .experienceYears(experienceYears)
            .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
            .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
            .rating(rating)
            .reviewCount(reviewCount)
            .consultationFee(consultationFee)
            .languages(languages)
            .services(services)
            .build();
    }

    private HealthcareProvider createReproductiveInfectionsSpecialist(String name, String email, String phone, String address,
                                                                      String district, String region, double lat, double lng,
                                                                      int experienceYears, double rating, int reviewCount,
                                                                      double consultationFee, String licenseNumber,
                                                                      List<String> services, List<String> languages) {
        return HealthcareProvider.builder()
            .name(name)
            .email(email)
            .phoneNumber(phone)
            .type(HealthcareProvider.ProviderType.INDIVIDUAL_DOCTOR)
            .scope(HealthcareProvider.GeographicScope.UGANDA)
            .specializations(Arrays.asList(
                HealthcareProvider.Specialization.REPRODUCTIVE_INFECTIONS,
                HealthcareProvider.Specialization.FAMILY_PLANNING
            ))
            .location(LocationUtils.toPoint(lat, lng))
            .address(address)
            .district(district)
            .region(region)
            .country("Uganda")
            .description("Specialist in sexually transmitted infections and reproductive tract infections")
            .licenseNumber(licenseNumber)
            .experienceYears(experienceYears)
            .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
            .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
            .rating(rating)
            .reviewCount(reviewCount)
            .consultationFee(consultationFee)
            .languages(languages)
            .services(services)
            .build();
    }

    private HealthcareProvider createHospital(String name, String email, String phone, String address,
                                             String district, String region, double lat, double lng,
                                             int experienceYears, double rating, int reviewCount,
                                             double consultationFee, String licenseNumber,
                                             List<HealthcareProvider.Specialization> specializations,
                                             List<String> services, List<String> languages) {
        return HealthcareProvider.builder()
            .name(name)
            .email(email)
            .phoneNumber(phone)
            .type(HealthcareProvider.ProviderType.HOSPITAL)
            .scope(HealthcareProvider.GeographicScope.UGANDA)
            .specializations(specializations)
            .location(LocationUtils.toPoint(lat, lng))
            .address(address)
            .district(district)
            .region(region)
            .country("Uganda")
            .description("Major healthcare facility with comprehensive reproductive health services")
            .licenseNumber(licenseNumber)
            .experienceYears(experienceYears)
            .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
            .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
            .rating(rating)
            .reviewCount(reviewCount)
            .consultationFee(consultationFee)
            .languages(languages)
            .services(services)
            .build();
    }

    private HealthcareProvider createSpecialistCenter(String name, String email, String phone, String address,
                                                      String district, String region, double lat, double lng,
                                                      int experienceYears, double rating, int reviewCount,
                                                      double consultationFee, String licenseNumber,
                                                      List<HealthcareProvider.Specialization> specializations,
                                                      List<String> services, List<String> languages) {
        return HealthcareProvider.builder()
            .name(name)
            .email(email)
            .phoneNumber(phone)
            .type(HealthcareProvider.ProviderType.SPECIALIST_CENTER)
            .scope(HealthcareProvider.GeographicScope.UGANDA)
            .specializations(specializations)
            .location(LocationUtils.toPoint(lat, lng))
            .address(address)
            .district(district)
            .region(region)
            .country("Uganda")
            .description("Specialized center focusing on advanced reproductive health treatments")
            .licenseNumber(licenseNumber)
            .experienceYears(experienceYears)
            .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
            .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
            .rating(rating)
            .reviewCount(reviewCount)
            .consultationFee(consultationFee)
            .languages(languages)
            .services(services)
            .build();
    }

    private HealthcareProvider createTelemedicineProvider(String name, String email, String phone, String address,
                                                          String district, String region, int experienceYears, double rating,
                                                          int reviewCount, double consultationFee, String licenseNumber,
                                                          List<HealthcareProvider.Specialization> specializations,
                                                          List<String> services, List<String> languages) {
        return HealthcareProvider.builder()
            .name(name)
            .email(email)
            .phoneNumber(phone)
            .type(HealthcareProvider.ProviderType.TELEMEDICINE_PROVIDER)
            .scope(HealthcareProvider.GeographicScope.GLOBAL)
            .specializations(specializations)
            .address(address)
            .district(district)
            .region(region)
            .country("Global")
            .description("24/7 telemedicine consultations with reproductive health specialists worldwide")
            .licenseNumber(licenseNumber)
            .experienceYears(experienceYears)
            .verificationStatus(HealthcareProvider.VerificationStatus.VERIFIED)
            .availabilityStatus(HealthcareProvider.AvailabilityStatus.AVAILABLE)
            .rating(rating)
            .reviewCount(reviewCount)
            .consultationFee(consultationFee)
            .languages(languages)
            .services(services)
            .build();
    }
}