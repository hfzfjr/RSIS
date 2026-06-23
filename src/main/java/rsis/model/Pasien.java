package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import rsis.model.interfaces.INotifiable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    public boolean isProfileComplete() {
        return nomorRekamMedis != null && !nomorRekamMedis.isEmpty()
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

    @Override
    public void terimaNotifikasi(Notifikasi notif) {
        // Implementasi spesifik untuk Pasien
        System.out.println("Pasien menerima notifikasi: " + notif.getPesan());
    }
}
