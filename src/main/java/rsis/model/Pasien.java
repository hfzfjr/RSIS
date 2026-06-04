package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pasien")
public class Pasien extends User {

    @Id
    @Column(name = "id_pasien", nullable = false, length = 10)
    private String idPasien;

    @Column(name = "nomor_rekam_medis", unique = true)
    private String nomorRekamMedis;

    @Column(name = "tanggal_lahir")
    private LocalDate tanggalLahir;

    @Column(name = "alamat")
    private String alamat;

    @Column(name = "nomor_hp")
    private String nomorHp;

    // Transient fields - only exist in users table, not in pasien table
    @Transient
    private String nama;
    @Transient
    private String email;
    @Transient
    private String password;
    @Transient
    private String role;

    public Pasien() {
    }

    // Business methods
    public List<Dokter> cariDokter(String spesialisasi) {
        return new ArrayList<>();
    }

    public List<JadwalPraktik> lihatJadwalDokter(String dokterId) {
        return new ArrayList<>();
    }

    public Appointment bookingAppointment(String jadwalId) {
        return null;
    }

    public void batalkanAppointment(String appointmentId) {
        // Placeholder - logic in service
    }

    public void rescheduleAppointment(String appointmentId, String jadwalBaruId) {
        // Placeholder - logic in service
    }

    public List<Appointment> getAppointmentList() {
        return new ArrayList<>();
    }

    // Getters and Setters
    public String getIdPasien() {
        return idPasien;
    }

    public void setIdPasien(String idPasien) {
        this.idPasien = idPasien;
    }

    public boolean isProfileComplete() {
        return getNomorHp() != null && !getNomorHp().isEmpty()
                && tanggalLahir != null
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
    public String getNomorHp() {
        return nomorHp;
    }

    public void setNomorHp(String nomorHp) {
        this.nomorHp = nomorHp;
    }

    @Override
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
