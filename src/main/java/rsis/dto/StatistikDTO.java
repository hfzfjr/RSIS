package rsis.dto;

import java.util.Map;

public class StatistikDTO {
    
    private Long totalPasienHariIni;
    private Long totalPasienBulanIni;
    private String dokterTersibuk;
    private Map<String, Long> pasienPerHari;
    private Long totalAppointmentSukses;
    private Long totalAppointmentBatal;
    private Long totalDokter;
    private Long totalPoli;
    
    public StatistikDTO() {
    }
    
    public StatistikDTO(Long totalPasienHariIni, Long totalPasienBulanIni, String dokterTersibuk, 
                        Map<String, Long> pasienPerHari, Long totalAppointmentSukses, 
                        Long totalAppointmentBatal, Long totalDokter, Long totalPoli) {
        this.totalPasienHariIni = totalPasienHariIni;
        this.totalPasienBulanIni = totalPasienBulanIni;
        this.dokterTersibuk = dokterTersibuk;
        this.pasienPerHari = pasienPerHari;
        this.totalAppointmentSukses = totalAppointmentSukses;
        this.totalAppointmentBatal = totalAppointmentBatal;
        this.totalDokter = totalDokter;
        this.totalPoli = totalPoli;
    }
    
    // Getters and Setters
    public Long getTotalPasienHariIni() {
        return totalPasienHariIni;
    }
    
    public void setTotalPasienHariIni(Long totalPasienHariIni) {
        this.totalPasienHariIni = totalPasienHariIni;
    }
    
    public Long getTotalPasienBulanIni() {
        return totalPasienBulanIni;
    }
    
    public void setTotalPasienBulanIni(Long totalPasienBulanIni) {
        this.totalPasienBulanIni = totalPasienBulanIni;
    }
    
    public String getDokterTersibuk() {
        return dokterTersibuk;
    }
    
    public void setDokterTersibuk(String dokterTersibuk) {
        this.dokterTersibuk = dokterTersibuk;
    }
    
    public Map<String, Long> getPasienPerHari() {
        return pasienPerHari;
    }
    
    public void setPasienPerHari(Map<String, Long> pasienPerHari) {
        this.pasienPerHari = pasienPerHari;
    }
    
    public Long getTotalAppointmentSukses() {
        return totalAppointmentSukses;
    }
    
    public void setTotalAppointmentSukses(Long totalAppointmentSukses) {
        this.totalAppointmentSukses = totalAppointmentSukses;
    }
    
    public Long getTotalAppointmentBatal() {
        return totalAppointmentBatal;
    }
    
    public void setTotalAppointmentBatal(Long totalAppointmentBatal) {
        this.totalAppointmentBatal = totalAppointmentBatal;
    }
    
    public Long getTotalDokter() {
        return totalDokter;
    }
    
    public void setTotalDokter(Long totalDokter) {
        this.totalDokter = totalDokter;
    }
    
    public Long getTotalPoli() {
        return totalPoli;
    }
    
    public void setTotalPoli(Long totalPoli) {
        this.totalPoli = totalPoli;
    }
}
