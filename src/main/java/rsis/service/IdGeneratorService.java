package rsis.service;

import org.springframework.stereotype.Service;

/**
 * Service untuk generate ID entity secara konsisten
 * Reusable across services untuk menghindari duplikasi logic
 */
@Service
public class IdGeneratorService {

    /**
     * Generate ID untuk dokter
     *
     * @param count Jumlah dokter yang sudah ada
     * @return ID dokter dalam format dkt-XXX
     */
    public String generateDokterId(long count) {
        return String.format("dkt-%03d", count + 1);
    }

    /**
     * Generate nomor STR
     *
     * @param idDokter ID dokter
     * @return Nomor STR dalam format STR-YYYYMMDD-XXXX
     */
    public String generateNomorStr(String idDokter) {
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequenceStr = idDokter.substring(4).toUpperCase(); // Extract numeric part from dkt-XXX
        return "STR-" + dateStr + "-" + sequenceStr;
    }

    /**
     * Generate ID untuk jadwal praktik
     * Menggunakan max ID yang ada + 1 untuk menghindari duplikasi
     *
     * @param maxIdNumber Nomor ID terbesar yang ada (misal: 342 dari jdw-342)
     * @return ID jadwal dalam format jdw-XXX
     */
    public String generateJadwalId(long maxIdNumber) {
        return String.format("jdw-%03d", maxIdNumber + 1);
    }

    /**
     * Generate ID untuk poli
     *
     * @param count Jumlah poli yang sudah ada
     * @return ID poli dalam format pli-XXX
     */
    public String generatePoliId(long count) {
        return String.format("pli-%03d", count + 1);
    }

    /**
     * Generate ID untuk appointment
     *
     * @param count Jumlah appointment yang sudah ada
     * @return ID appointment dalam format apt-XXX
     */
    public String generateAppointmentId(long count) {
        return String.format("apt-%03d", count + 1);
    }
}
