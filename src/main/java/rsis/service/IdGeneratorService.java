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
     * @param count Jumlah dokter yang sudah ada
     * @return ID dokter dalam format dkt-XXX
     */
    public String generateDokterId(long count) {
        return String.format("dkt-%03d", count + 1);
    }

    /**
     * Generate nomor STR
     * @param idDokter ID dokter
     * @return Nomor STR dalam format STR-ID
     */
    public String generateNomorStr(String idDokter) {
        return "STR-" + idDokter.toUpperCase();
    }

    /**
     * Generate ID untuk jadwal praktik
     * @param count Jumlah jadwal yang sudah ada
     * @return ID jadwal dalam format jdw-XXX
     */
    public String generateJadwalId(long count) {
        return String.format("jdw-%03d", count + 1);
    }

    /**
     * Generate ID untuk poli
     * @param count Jumlah poli yang sudah ada
     * @return ID poli dalam format pol-XXX
     */
    public String generatePoliId(long count) {
        return String.format("pol-%03d", count + 1);
    }

    /**
     * Generate ID untuk appointment
     * @param count Jumlah appointment yang sudah ada
     * @return ID appointment dalam format apt-XXX
     */
    public String generateAppointmentId(long count) {
        return String.format("apt-%03d", count + 1);
    }
}
