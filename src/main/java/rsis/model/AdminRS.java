package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "admin_rs")
@PrimaryKeyJoinColumn(name = "id_user")
public class AdminRS extends User {

    @Column(name = "jabatan")
    private String jabatan;

    public AdminRS() {
        super();
    }

    public AdminRS(String idUser, String nama, String email,
            String password, String jabatan) {
        super(idUser, nama, email, password, "ADMIN_RS");
        this.jabatan = jabatan;
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
    public String getJabatan() {
        return jabatan;
    }

    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
    }

    public boolean hasManagementAccess() {
        return jabatan != null && !jabatan.isEmpty();
    }

    @Override
    public void terimaNotifikasi(Notifikasi notif) {
        // Implementasi spesifik untuk AdminRS
        System.out.println("Admin menerima notifikasi: " + notif.getPesan());
    }
}
