package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "poli")
public class Poli {

    @Id
    @Column(name = "id_poli", nullable = false, length = 10)
    private String idPoli;

    @Column(name = "nama_poli", nullable = false)
    private String namaPoli;

    @Column(name = "lokasi_ruangan")
    private String lokasiRuangan;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @jakarta.persistence.Transient
    private Integer jumlahDokter = 0;

    public Poli() {
    }

    public List<Dokter> getDokterList() {
        return new ArrayList<>();
    }

    public List<JadwalPraktik> getJadwalList() {
        return new ArrayList<>();
    }

    public String getNamaPoli() {
        return this.namaPoli;
    }

    public void tambahDokter(Dokter dokter) {
        // Placeholder - logic in service
    }

    public void hapusDokter(String dokterId) {
        // Placeholder - logic in service
    }

    // Getters and Setters
    public String getIdPoli() {
        return idPoli;
    }

    public void setIdPoli(String idPoli) {
        this.idPoli = idPoli;
    }

    public void setNamaPoli(String namaPoli) {
        this.namaPoli = namaPoli;
    }

    public String getLokasiRuangan() {
        return lokasiRuangan;
    }

    public void setLokasiRuangan(String lokasiRuangan) {
        this.lokasiRuangan = lokasiRuangan;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getJumlahDokter() {
        return jumlahDokter;
    }

    public void setJumlahDokter(Integer jumlahDokter) {
        this.jumlahDokter = jumlahDokter;
    }
}
