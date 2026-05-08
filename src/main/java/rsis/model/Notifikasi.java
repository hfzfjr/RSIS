package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notifikasi")
public class Notifikasi {

    @Id
    @Column(name = "id_notifikasi", nullable = false, length = 10)
    private String idNotifikasi;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private AppUser penerima;

    @Column(name = "pesan", nullable = false)
    private String pesan;

    @Column(name = "tipe")
    private String tipe;

    @Column(name = "status")
    private String status = "BELUM_DIBACA";

    @Column(name = "tanggal_kirim")
    private Instant tanggalKirim;

    public Notifikasi() {
    }

    public void kirim() {
        this.tanggalKirim = Instant.now();
        // Logic in service
    }

    public String getStatus() {
        return this.status;
    }

    public void markAsRead() {
        this.status = "SUDAH_DIBACA";
    }

    // Getters and Setters
    public String getIdNotifikasi() {
        return idNotifikasi;
    }

    public void setIdNotifikasi(String idNotifikasi) {
        this.idNotifikasi = idNotifikasi;
    }

    public AppUser getPenerima() {
        return penerima;
    }

    public void setPenerima(AppUser penerima) {
        this.penerima = penerima;
    }

    public String getPesan() {
        return pesan;
    }

    public void setPesan(String pesan) {
        this.pesan = pesan;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getTanggalKirim() {
        return tanggalKirim;
    }

    public void setTanggalKirim(Instant tanggalKirim) {
        this.tanggalKirim = tanggalKirim;
    }
}
