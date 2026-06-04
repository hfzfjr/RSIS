package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.dto.JadwalDTO;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Poli;
import rsis.repository.DokterRepository;
import rsis.repository.JadwalPraktikRepository;
import rsis.repository.PoliRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JadwalPraktikService {

    @Autowired
    private JadwalPraktikRepository jadwalPraktikRepository;

    @Autowired
    private DokterRepository dokterRepository;

    @Autowired
    private PoliRepository poliRepository;

    public List<JadwalPraktik> getAllJadwal() {
        return jadwalPraktikRepository.findAll();
    }

    public List<JadwalPraktik> getJadwalByDokterId(String dokterId) {
        return jadwalPraktikRepository.findByDokter_IdDokter(dokterId);
    }

    public List<JadwalPraktik> getJadwalByPoliId(String poliId) {
        return jadwalPraktikRepository.findByDokter_Poli_IdPoli(poliId);
    }

    public List<JadwalPraktik> getAvailableJadwal() {
        return jadwalPraktikRepository.findAllAvailableJadwal();
    }

    public List<JadwalDTO> getAvailableJadwalDTOs() {
        List<JadwalPraktik> jadwals = jadwalPraktikRepository.findAllAvailableJadwal();
        return jadwals.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<JadwalDTO> getJadwalDTOsByDokterId(String dokterId) {
        List<JadwalPraktik> jadwals = jadwalPraktikRepository.findAvailableJadwalByDokterId(dokterId);
        return jadwals.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public JadwalPraktik createJadwal(JadwalPraktik jadwal) {
        String idJadwal = generateJadwalId();
        jadwal.setIdJadwal(idJadwal);
        jadwal.setStatusKetersediaan("TERSEDIA");
        return jadwalPraktikRepository.save(jadwal);
    }

    @Transactional
    public JadwalPraktik updateJadwal(JadwalPraktik jadwal) {
        return jadwalPraktikRepository.save(jadwal);
    }

    @Transactional
    public void deleteJadwal(String jadwalId) {
        jadwalPraktikRepository.deleteById(jadwalId);
    }

    public Optional<JadwalPraktik> getJadwalById(String jadwalId) {
        return jadwalPraktikRepository.findById(jadwalId);
    }

    private JadwalDTO convertToDTO(JadwalPraktik jadwal) {
        Dokter dokter = jadwal.getDokter();
        Poli poli = dokter != null ? dokter.getPoli() : null;

        return new JadwalDTO(
                jadwal.getIdJadwal(),
                dokter != null ? dokter.getIdDokter() : null,
                dokter != null ? dokter.getIdDokter() : null,
                poli != null ? poli.getIdPoli() : null,
                jadwal.getHari(),
                null,
                jadwal.getJamMulai(),
                jadwal.getJamSelesai(),
                jadwal.getKuota(),
                jadwal.getSisaKuota(),
                jadwal.getStatusKetersediaan());
    }

    private String generateJadwalId() {
        return "jdw-" + System.currentTimeMillis();
    }
}
