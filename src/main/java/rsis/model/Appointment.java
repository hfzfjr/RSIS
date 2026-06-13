package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDate;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @Column(name = "id_appointment", nullable = false, length = 10)
    private String idAppointment;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_jadwal", nullable = false)
    private JadwalPraktik jadwal;

    @Column(name = "tanggal_booking", nullable = false)
    private LocalDate tanggalBooking;

    @Column(name = "status")
    private String status = "MENUNGGU";

    @Column(name = "nomor_antrian")
    private String nomorAntrian;

    @Column(name = "catatan")
    private String catatan;

    @Transient
    private String alasanTolak;

    public Appointment() {
    }

    public void konfirmasi() {
        this.status = "DIKONFIRMASI";
    }

    public void batalkan() {
        this.status = "DIBATALKAN";
        if (jadwal != null) {
            jadwal.tambahKuota();
        }
    }

    public void ubahJadwal(String jadwalBaruId) {
        // Placeholder - logic in service
    }

    public void tolak(String alasan) {
        this.status = "DITOLAK";
        this.alasanTolak = alasan;
    }

    public String getStatus() {
        return this.status;
    }

    public User getUser() {
        return user;
    }

    // Getters and Setters
    public String getIdAppointment() {
        return idAppointment;
    }

    public void setIdAppointment(String idAppointment) {
        this.idAppointment = idAppointment;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JadwalPraktik getJadwal() {
        return jadwal;
    }

    public void setJadwal(JadwalPraktik jadwal) {
        this.jadwal = jadwal;
    }

    public Dokter getDokter() {
        return jadwal != null ? jadwal.getDokter() : null;
    }

    public LocalDate getTanggalBooking() {
        return tanggalBooking;
    }

    public void setTanggalBooking(LocalDate tanggalBooking) {
        this.tanggalBooking = tanggalBooking;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNomorAntrian() {
        return nomorAntrian;
    }

    public void setNomorAntrian(String nomorAntrian) {
        this.nomorAntrian = nomorAntrian;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public String getAlasanTolak() {
        return alasanTolak;
    }

    public void setAlasanTolak(String alasanTolak) {
        this.alasanTolak = alasanTolak;
    }
}
