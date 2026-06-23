package rsis.util;

import java.util.Map;

/**
 * Utility class untuk operasi tanggal dan waktu
 * Cross-cutting concern untuk format dan translate hari
 */
public class DateTimeUtil {
    
    private static final Map<String, String> DAY_MAP = Map.of(
        "MONDAY", "SENIN",
        "TUESDAY", "SELASA",
        "WEDNESDAY", "RABU",
        "THURSDAY", "KAMIS",
        "FRIDAY", "JUMAT",
        "SATURDAY", "SABTU",
        "SUNDAY", "MINGGU"
    );

    /**
     * Translate nama hari dari bahasa Inggris ke bahasa Indonesia
     * @param englishDay Nama hari dalam bahasa Inggris
     * @return Nama hari dalam bahasa Indonesia, atau original jika tidak ditemukan
     */
    public static String translateDayToIndonesian(String englishDay) {
        if (englishDay == null) {
            return englishDay;
        }
        return DAY_MAP.getOrDefault(englishDay.toUpperCase(), englishDay);
    }
}
