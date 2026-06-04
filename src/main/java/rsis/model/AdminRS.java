package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "admin_rs")
public class AdminRS extends User {

    @Id
    @Column(name = "id_admin", nullable = false, length = 10)
    private String idAdmin;

    @Column(name = "jabatan")
    private String jabatan;

    @Column(name = "nomor_hp")
    private String nomorHp;

    // Transient fields - only exist in users table, not in admin_rs table
    @Transient
    private String nama;
    @Transient
    private String email;
    @Transient
    private String password;
    @Transient
    private String role;

    public AdminRS() {
    }

    public void kelolaDataDokter(Dokter dokter) {
        // Placeholder - logic in service
    }

    public void kelolaDataPoli(Poli poli) {
        // Placeholder - logic in service
    }

    public void kelolaJadwal(JadwalPraktik jadwal) {
        // Placeholder - logic in service
    }

    public LaporanBulanan cetakLaporanBulanan(int bulan, int tahun) {
        return null;
    }

    public int getTotalPasienHariIni() {
        return 0;
    }

    public int getTotalPasienBulanIni() {
        return 0;
    }

    public List<Dokter> getDokterTersibuk() {
        return new ArrayList<>();
    }

    public Map<String, Integer> getPasienPerHari() {
        return new HashMap<>();
    }

    // Getters and Setters
    public String getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(String idAdmin) {
        this.idAdmin = idAdmin;
    }

    public String getJabatan() {
        return jabatan;
    }

    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
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
