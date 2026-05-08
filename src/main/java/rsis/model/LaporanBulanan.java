package rsis.model;

import java.io.File;

public class LaporanBulanan {

    private String idLaporan;
    private int bulan;
    private int tahun;
    private int totalPasien;
    private int totalAppointment;
    private int totalBatal;

    public LaporanBulanan() {
    }

    public void generate() {
        // Placeholder - logic in service
    }

    public File exportPDF() {
        // Placeholder - logic in LaporanBulananService
        return null;
    }

    public File exportCSV() {
        // Placeholder - logic in LaporanBulananService
        return null;
    }

    public String getSummary() {
        return String.format("Laporan Bulan %d Tahun %d: Total Pasien=%d, Appointment=%d, Batal=%d",
                bulan, tahun, totalPasien, totalAppointment, totalBatal);
    }

    // Getters and Setters
    public String getIdLaporan() {
        return idLaporan;
    }

    public void setIdLaporan(String idLaporan) {
        this.idLaporan = idLaporan;
    }

    public int getBulan() {
        return bulan;
    }

    public void setBulan(int bulan) {
        this.bulan = bulan;
    }

    public int getTahun() {
        return tahun;
    }

    public void setTahun(int tahun) {
        this.tahun = tahun;
    }

    public int getTotalPasien() {
        return totalPasien;
    }

    public void setTotalPasien(int totalPasien) {
        this.totalPasien = totalPasien;
    }

    public int getTotalAppointment() {
        return totalAppointment;
    }

    public void setTotalAppointment(int totalAppointment) {
        this.totalAppointment = totalAppointment;
    }

    public int getTotalBatal() {
        return totalBatal;
    }

    public void setTotalBatal(int totalBatal) {
        this.totalBatal = totalBatal;
    }
}
