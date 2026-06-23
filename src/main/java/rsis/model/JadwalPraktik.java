package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "jadwal_praktik")
public class JadwalPraktik {

    @Id
    @Column(name = "id_jadwal", nullable = false, length = 10)
    private String idJadwal;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private Dokter dokter;

    @Column(name = "hari", nullable = false)
    private String hari;

    @Column(name = "tanggal")
    private LocalDate tanggal;

    @Column(name = "jam_mulai", nullable = false)
    private LocalTime jamMulai;

    @Column(name = "jam_selesai", nullable = false)
    private LocalTime jamSelesai;

    @Column(name = "status_ketersediaan")
    private String statusKetersediaan;

    @Column(name = "kuota", nullable = false)
    private int kuota;

    @Column(name = "sisa_kuota", nullable = false)
    private int sisaKuota;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public JadwalPraktik() {
    }

    public Dokter getDokter() {
        return this.dokter;
    }

    public void updateStatus(String status) {
        this.statusKetersediaan = status;
    }

    public boolean cekTersedia() {
        return sisaKuota > 0 && "TERSEDIA".equals(statusKetersediaan);
    }

    public void tambahKuota() {
        this.sisaKuota++;
    }

    public void kurangiKuota() {
        if (sisaKuota > 0) {
            sisaKuota--;
            if (sisaKuota == 0) {
                this.statusKetersediaan = "PENUH";
            }
        }
    }

    // Getters and Setters
    public String getIdJadwal() {
        return idJadwal;
    }

    public void setIdJadwal(String idJadwal) {
        this.idJadwal = idJadwal;
    }

    public void setDokter(Dokter dokter) {
        this.dokter = dokter;
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

    public int getKuota() {
        return kuota;
    }

    public void setKuota(int kuota) {
        this.kuota = kuota;
    }

    public int getSisaKuota() {
        return sisaKuota;
    }

    public void setSisaKuota(int sisaKuota) {
        this.sisaKuota = sisaKuota;
    }

    public String getStatusKetersediaan() {
        if (sisaKuota <= 0) {
            return "PENUH";
        }
        
        LocalDate today = LocalDate.now();
        boolean isToday = false;
        
        if (this.tanggal != null) {
            isToday = this.tanggal.equals(today);
        } else if (this.hari != null) {
            String todayIndonesian = getIndonesianDayName(today.getDayOfWeek().toString());
            isToday = this.hari.equalsIgnoreCase(todayIndonesian);
        }
        
        if (isToday) {
            LocalTime now = LocalTime.now();
            if (this.jamMulai != null && this.jamSelesai != null) {
                if (now.isAfter(this.jamSelesai)) {
                    return "LIBUR";
                }
            }
        } else {
            if (this.tanggal != null && this.tanggal.isBefore(today)) {
                return "LIBUR";
            }
        }
        
        if ("LIBUR".equals(this.statusKetersediaan)) {
            return "LIBUR";
        }
        
        if ("PENUH".equals(this.statusKetersediaan)) {
            return "PENUH";
        }
        
        return "TERSEDIA";
    }

    private String getIndonesianDayName(String englishDay) {
        switch (englishDay.toUpperCase()) {
            case "MONDAY": return "Senin";
            case "TUESDAY": return "Selasa";
            case "WEDNESDAY": return "Rabu";
            case "THURSDAY": return "Kamis";
            case "FRIDAY": return "Jumat";
            case "SATURDAY": return "Sabtu";
            case "SUNDAY": return "Minggu";
            default: return "";
        }
    }

    public void setStatusKetersediaan(String statusKetersediaan) {
        this.statusKetersediaan = statusKetersediaan;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
