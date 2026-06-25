package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "spesialisasi")
public class Spesialisasi {

    @Id
    @Column(name = "id_spesialisasi", nullable = false, length = 10)
    private String idSpesialisasi;

    @Column(name = "nama", nullable = false)
    private String nama;

    @Column(name = "deskripsi")
    private String deskripsi;

    public Spesialisasi() {
    }

    // Getters and Setters
    public String getIdSpesialisasi() {
        return idSpesialisasi;
    }

    public void setIdSpesialisasi(String idSpesialisasi) {
        this.idSpesialisasi = idSpesialisasi;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
}
