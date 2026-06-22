package rsis.dto;

import rsis.model.Dokter;

public class BusiestDoctorDTO {
    private Dokter dokter;
    private Long count;
    private Integer percentage;

    public BusiestDoctorDTO(Dokter dokter, Long count, Integer percentage) {
        this.dokter = dokter;
        this.count = count;
        this.percentage = percentage;
    }

    public Dokter getDokter() {
        return dokter;
    }

    public void setDokter(Dokter dokter) {
        this.dokter = dokter;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }
}
