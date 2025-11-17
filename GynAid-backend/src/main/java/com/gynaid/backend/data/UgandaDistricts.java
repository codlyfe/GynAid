package com.gynaid.backend.data;

import java.util.Arrays;
import java.util.List;

/**
 * Uganda Districts and Regions Data
 * Used for geographic filtering and provider search
 */
public class UgandaDistricts {
    
    // Uganda's 15 regions and their districts
    public static final List<Region> UGANDA_REGIONS = Arrays.asList(
        new Region("Central", Arrays.asList(
            "Kampala", "Wakiso", "Mukono", "Kayunga", "Mubende", "Mityana", "Nakasongola", 
            "Luweero", "Nakaseke", "Lyantonde", "Masaka", "Kalangala", "Sembabule", 
            "Gomba", "Kalungu", "Butambala", "Mpigi", "Rakai", "Ssemabule", "Lwengo",
            "Kyotera", "Kiboga", "Kiryandongo", "Kumi", "Bukomansimbi", "Bugiri",
            "Bugweri", "Bugiri", "Busiki", "Naboom", "Buyende", "Kamuli", "Kaliro"
        )),
        new Region("Eastern", Arrays.asList(
            "Iganga", "Kamuli", "Bugiri", "Mayuge", "Jinja", "Kamul", "Bugweri", 
            "Busiki", "Luuka", "Kaliro", "Butaleja", "Budaka", "Pallisa", 
            "Kumi", "Soroti", "Katakwi", "Kapchorwa", "Kotido", "Nakapiripirit", 
            "Moroto", "Manafwa", "Mbale", "Sironko", "Bududa", "Tororo", 
            "Busia", "Butaleja", "Namayengo"
        )),
        new Region("Northern", Arrays.asList(
            "Gulu", "Lira", "Kitgum", "Nwoya", "Adjumani", "Arua", "Yumbe", 
            "Moyo", "Nebbi", "Zombo", "Apac", "Kole", "Oyam", "Lira", "Alebtong", 
            "Amolatar", "Dokolo", "Lokung", "Otuke", "Pader", "Agago", "Amuru", 
            "Lalogi", "Omora", "Karenga", "Bundibugyo", "Ntoroko", "Kasese", "Kiryandongo", 
            "Kitegela", "Kiryandongo"
        )),
        new Region("Western", Arrays.asList(
            "Mbarara", "Bushenyi", "Ntungamo", "Kabale", "Kisoro", "Rukungiri", 
            "Kanungu", "Kasese", "Buliisa", "Hoima", "Kibaale", "Kitara", 
            "Masindi", "Nebbi", "Bundibugyo", "Ntoroko", "Kasese", "Kamwenge", 
            "Kyenjojo", "Kabarore", "Rubirizi", "Sheema", "Mitooma", "Rubanda", 
            "Rwampara", "Ntoroko", "Kasese", "Kikuube", "Kiryandongo", "Kikube"
        ))
    );
    
    // All districts flattened
    public static final List<String> ALL_DISTRICTS = UGANDA_REGIONS.stream()
        .flatMap(region -> region.districts.stream())
        .distinct()
        .sorted()
        .toList();
    
    // Major cities and towns
    public static final List<String> MAJOR_CITIES = Arrays.asList(
        "Kampala", "Jinja", "Mbale", "Gulu", "Mbarara", "Lira", "Kasese", 
        "Hoima", "Kabale", "Soroti", "Arua", "Moroto", "Kitgum", "Mukono", 
        "Wakiso", "Bweyale", "Masindi", "Fort Portal", "Mubende", "Iganga"
    );
    
    // Common specializations for providers
    public static final List<String> GYNECOLOGY_SPECIALIZATIONS = Arrays.asList(
        "GENERAL_GYNECOLOGY",
        "OBSTETRICS",
        "INFERTILITY_SUPPORT",
        "ENDOMETRIOSIS_CARE",
        "CYCLE_COMPLICATIONS",
        "REPRODUCTIVE_INFECTIONS",
        "GYNECOLOGIC_ONCOLOGY",
        "REPRODUCTIVE_ENDOCRINOLOGY",
        "FAMILY_PLANNING",
        "MATERNAL_FETAL_MEDICINE",
        "PEDIATRIC_GYNECOLOGY",
        "MENOPAUSE_MANAGEMENT"
    );
    
    public static class Region {
        public final String name;
        public final List<String> districts;
        
        public Region(String name, List<String> districts) {
            this.name = name;
            this.districts = districts;
        }
        
        public String getName() { return name; }
        public List<String> getDistricts() { return districts; }
    }
    
    public static List<String> getDistrictsByRegion(String regionName) {
        return UGANDA_REGIONS.stream()
            .filter(region -> region.name.equalsIgnoreCase(regionName))
            .findFirst()
            .map(Region::getDistricts)
            .orElse(Arrays.asList());
    }
    
    public static String getRegionByDistrict(String district) {
        return UGANDA_REGIONS.stream()
            .filter(region -> region.districts.contains(district))
            .findFirst()
            .map(Region::getName)
            .orElse("Unknown");
    }
    
    public static boolean isValidDistrict(String district) {
        return ALL_DISTRICTS.contains(district);
    }
    
    public static boolean isMajorCity(String location) {
        return MAJOR_CITIES.contains(location);
    }
    
    public static List<String> getSpecializations() {
        return GYNECOLOGY_SPECIALIZATIONS;
    }
}