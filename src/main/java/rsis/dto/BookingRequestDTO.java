package rsis.dto;

import jakarta.validation.constraints.NotBlank;

public class BookingRequestDTO {

    @NotBlank(message = "Jadwal ID is required")
    private String jadwalId;

    @NotBlank(message = "Pasien ID is required")
    private String pasienId;

    private String catatan;

    public BookingRequestDTO() {
    }

    public BookingRequestDTO(String jadwalId, String pasienId, String catatan) {
        this.jadwalId = jadwalId;
        this.pasienId = pasienId;
        this.catatan = catatan;
    }

    public String getJadwalId() {
        return jadwalId;
    }

    public void setJadwalId(String jadwalId) {
        this.jadwalId = jadwalId;
    }

    public String getPasienId() {
        return pasienId;
    }

    public void setPasienId(String pasienId) {
        this.pasienId = pasienId;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }
}
