package rsis.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class JadwalDTO {
    
    private String id;
    private String dokterId;
    private String dokterNama;
    private String poliNama;
    private String hari;
    private LocalDate tanggal;
    private LocalTime jamMulai;
    private LocalTime jamSelesai;
    private Integer kuota;
    private Integer sisaKuota;
    private String statusKetersediaan;
    
    public JadwalDTO() {
    }
    
    public JadwalDTO(String id, String dokterId, String dokterNama, String poliNama, 
                     String hari, LocalDate tanggal, LocalTime jamMulai, LocalTime jamSelesai, 
                     Integer kuota, Integer sisaKuota, String statusKetersediaan) {
        this.id = id;
        this.dokterId = dokterId;
        this.dokterNama = dokterNama;
        this.poliNama = poliNama;
        this.hari = hari;
        this.tanggal = tanggal;
        this.jamMulai = jamMulai;
        this.jamSelesai = jamSelesai;
        this.kuota = kuota;
        this.sisaKuota = sisaKuota;
        this.statusKetersediaan = statusKetersediaan;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDokterId() {
        return dokterId;
    }
    
    public void setDokterId(String dokterId) {
        this.dokterId = dokterId;
    }
    
    public String getDokterNama() {
        return dokterNama;
    }
    
    public void setDokterNama(String dokterNama) {
        this.dokterNama = dokterNama;
    }
    
    public String getPoliNama() {
        return poliNama;
    }
    
    public void setPoliNama(String poliNama) {
        this.poliNama = poliNama;
    }
    
    public String getHari() {
        return hari;
    }
    
    public void setHari(String hari) {
        this.hari = hari;
    }
    
    public LocalDate getTanggal() {
        return tanggal;
    }
    
    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }
    
    public LocalTime getJamMulai() {
        return jamMulai;
    }
    
    public void setJamMulai(LocalTime jamMulai) {
        this.jamMulai = jamMulai;
    }
    
    public LocalTime getJamSelesai() {
        return jamSelesai;
    }
    
    public void setJamSelesai(LocalTime jamSelesai) {
        this.jamSelesai = jamSelesai;
    }
    
    public Integer getKuota() {
        return kuota;
    }
    
    public void setKuota(Integer kuota) {
        this.kuota = kuota;
    }
    
    public Integer getSisaKuota() {
        return sisaKuota;
    }
    
    public void setSisaKuota(Integer sisaKuota) {
        this.sisaKuota = sisaKuota;
    }
    
    public String getStatusKetersediaan() {
        return statusKetersediaan;
    }
    
    public void setStatusKetersediaan(String statusKetersediaan) {
        this.statusKetersediaan = statusKetersediaan;
    }
}
