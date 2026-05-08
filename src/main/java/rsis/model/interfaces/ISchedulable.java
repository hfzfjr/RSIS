package rsis.model.interfaces;

import rsis.model.JadwalPraktik;
import java.util.List;

public interface ISchedulable {
    List<JadwalPraktik> getJadwal();

    void updateJadwal(JadwalPraktik jadwal);

    boolean cekKetersediaan(String jadwalId);
}
