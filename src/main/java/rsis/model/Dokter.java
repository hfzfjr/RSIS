package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import rsis.model.interfaces.ISchedulable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dokter")
public class Dokter extends User implements ISchedulable {

    @Id
    @Column(name = "id_dokter", nullable = false, length = 10)
    private String idDokter;

    @Column(name = "id_user", nullable = false, length = 32)
    private String idUser;

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

    // Transient fields - only exist in users table, not in dokter table
    @Transient
    private String nama;
    @Transient
    private String email;
    @Transient
    private String password;
    @Transient
    private String role;

    public Dokter() {
    }

    @Override
    public List<JadwalPraktik> getJadwal() {
        return new ArrayList<>();
    }

    @Override
    public void updateJadwal(JadwalPraktik jadwal) {
        // Placeholder - logic in service
    }

    @Override
    public boolean cekKetersediaan(String jadwalId) {
        return false;
    }

    public void kelolaJadwal(String jadwalId, String status) {
        // Placeholder - logic in service
    }

    public List<Pasien> lihatDaftarPasien() {
        return new ArrayList<>();
    }

    public void konfirmasiAppointment(String appointmentId) {
        // Placeholder - logic in service
    }

    public void tolakAppointment(String appointmentId, String alasan) {
        // Placeholder - logic in service
    }

    public Poli getPoli() {
        return this.poli;
    }

    public Spesialisasi getSpesialisasi() {
        return this.spesialisasi;
    }

    // Getters and Setters
    public String getIdDokter() {
        return idDokter;
    }

    public void setIdDokter(String idDokter) {
        this.idDokter = idDokter;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getNomorStr() {
        return nomorStr;
    }

    public void setNomorStr(String nomorStr) {
        this.nomorStr = nomorStr;
    }

    public void setSpesialisasi(Spesialisasi spesialisasi) {
        this.spesialisasi = spesialisasi;
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

    // Implementations for abstract methods from User
    @Override
    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
