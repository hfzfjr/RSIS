package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import rsis.model.interfaces.INotifiable;
import rsis.model.interfaces.ISchedulable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dokter")
@PrimaryKeyJoinColumn(name = "id_user")
public class Dokter extends User implements INotifiable, ISchedulable {

    @Column(name = "nomor_str", unique = true)
    private String nomorStr;

    @ManyToOne
    @JoinColumn(name = "id_spesialisasi")
    private Spesialisasi spesialisasi;

    @ManyToOne
    @JoinColumn(name = "id_poli")
    private Poli poli;

    @Column(name = "dokter_image")
    private String dokterImage;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public Dokter() {
        super();
    }

    public Dokter(String idUser, String nama, String email,
            String password, String nomorStr, Spesialisasi spesialisasi, Poli poli) {
        super(idUser, nama, email, password, "DOKTER");
        this.nomorStr = nomorStr;
        this.spesialisasi = spesialisasi;
        this.poli = poli;
    }

    // ISchedulable interface implementation
    @Override
    public List<JadwalPraktik> getJadwal() {
        return new ArrayList<>();
    }

    @Override
    public void updateJadwal(JadwalPraktik jadwal) {
        // Logic in service
    }

    @Override
    public boolean cekKetersediaan(String jadwalId) {
        return false;
    }

    // INotifiable interface implementation
    @Override
    public void terimaNotifikasi(Notifikasi notif) {
        // Logic in service
    }

    // Getters and Setters
    public String getNomorStr() {
        return nomorStr;
    }

    public void setNomorStr(String nomorStr) {
        this.nomorStr = nomorStr;
    }

    public Spesialisasi getSpesialisasi() {
        return spesialisasi;
    }

    public void setSpesialisasi(Spesialisasi spesialisasi) {
        this.spesialisasi = spesialisasi;
    }

    public Poli getPoli() {
        return this.poli;
    }

    public void setPoli(Poli poli) {
        this.poli = poli;
    }

    public String getDokterImage() {
        return dokterImage;
    }

    public void setDokterImage(String dokterImage) {
        this.dokterImage = dokterImage;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
