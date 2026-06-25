package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    // Getters and Setters
    public String getIdPoli() {
        return idPoli;
    }

    public void setIdPoli(String idPoli) {
        this.idPoli = idPoli;
    }

    public String getNamaPoli() {
        return namaPoli;
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
