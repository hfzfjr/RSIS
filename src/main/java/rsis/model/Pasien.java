package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import rsis.model.interfaces.INotifiable;

import java.time.LocalDate;

@Entity
@Table(name = "pasien")
@PrimaryKeyJoinColumn(name = "id_user")
public class Pasien extends User implements INotifiable {

    @Column(name = "nomor_rekam_medis", unique = true)
    private String nomorRekamMedis;

    @Column(name = "tanggal_lahir")
    private LocalDate tanggalLahir;

    @Column(name = "alamat")
    private String alamat;

    public Pasien() {
        super();
    }

    public Pasien(String idUser, String nama, String email,
            String password, String nomorRekamMedis, LocalDate tanggalLahir, String alamat) {
        super(idUser, nama, email, password, "PASIEN");
        this.nomorRekamMedis = nomorRekamMedis;
        this.tanggalLahir = tanggalLahir;
        this.alamat = alamat;
    }

    // INotifiable interface implementation
    @Override
    public void terimaNotifikasi(Notifikasi notif) {
        // Logic in service
    }

    // Getters and Setters
    public boolean isProfileComplete() {
        return tanggalLahir != null
                && alamat != null && !alamat.isEmpty();
    }

    public String getNomorRekamMedis() {
        return nomorRekamMedis;
    }

    public void setNomorRekamMedis(String nomorRekamMedis) {
        this.nomorRekamMedis = nomorRekamMedis;
    }

    public LocalDate getTanggalLahir() {
        return tanggalLahir;
    }

    public void setTanggalLahir(LocalDate tanggalLahir) {
        this.tanggalLahir = tanggalLahir;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }
}
