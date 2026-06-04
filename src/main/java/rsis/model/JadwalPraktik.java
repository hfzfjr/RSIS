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
    @JoinColumn(name = "id_dokter", nullable = false)
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
        return statusKetersediaan;
    }

    public void setStatusKetersediaan(String statusKetersediaan) {
        this.statusKetersediaan = statusKetersediaan;
    }
}
