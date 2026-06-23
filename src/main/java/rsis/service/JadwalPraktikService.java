package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.dto.JadwalDTO;
import rsis.model.Appointment;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Poli;
import rsis.repository.AppointmentRepository;
import rsis.repository.DokterRepository;
import rsis.repository.JadwalPraktikRepository;
import rsis.repository.PoliRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<JadwalPraktik> getAllJadwal() {
        return jadwalPraktikRepository.findAll().stream()
                .filter(j -> j.getIsActive() == null || j.getIsActive())
                .toList();
    }

    public List<JadwalPraktik> getJadwalByDokterId(String dokterId) {
        return jadwalPraktikRepository.findByDokter_IdUser(dokterId).stream()
                .filter(j -> j.getIsActive() == null || j.getIsActive())
                .toList();
    }

    public List<JadwalPraktik> getJadwalByPoliId(String poliId) {
        return jadwalPraktikRepository.findByDokter_Poli_IdPoli(poliId).stream()
                .filter(j -> j.getIsActive() == null || j.getIsActive())
                .toList();
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
                dokter != null ? dokter.getIdUser() : null,
                dokter != null ? dokter.getIdUser() : null,
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
        Optional<String> latestId = jadwalPraktikRepository.findLatestJadwalId();
        if (latestId.isPresent()) {
            String id = latestId.get();
            try {
                int num = Integer.parseInt(id.substring(4));
                return String.format("jdw-%03d", num + 1);
            } catch (NumberFormatException e) {
                // fallback
            }
        }
        return "jdw-001";
    }

    /**
     * Get jadwal with dates mapped from appointments for a specific doctor
     * This builds a complex data structure that was previously done in controller
     * 
     * @param dokterId The doctor's user ID
     * @return List of maps containing jadwal data with appointment information
     */
    public java.util.List<Map<String, Object>> getJadwalWithDatesForDokter(String dokterId) {
        List<Appointment> appointments = appointmentRepository.findByJadwal_Dokter_IdUser(dokterId);
        java.util.List<Map<String, Object>> jadwalWithDates = new java.util.ArrayList<>();

        for (Appointment apt : appointments) {
            JadwalPraktik j = apt.getJadwal();
            if (j == null)
                continue;

            Map<String, Object> entry = new HashMap<>();
            entry.put("idJadwal", j.getIdJadwal());
            entry.put("hari", j.getHari());
            entry.put("tanggal", apt.getTanggalBooking() != null ? apt.getTanggalBooking().toString() : null);
            entry.put("jamMulai", j.getJamMulai() != null ? j.getJamMulai().toString() : null);
            entry.put("jamSelesai", j.getJamSelesai() != null ? j.getJamSelesai().toString() : null);
            entry.put("kuota", j.getKuota());
            entry.put("sisaKuota", j.getSisaKuota());
            entry.put("statusKetersediaan", j.getStatusKetersediaan());
            entry.put("appointmentId", apt.getIdAppointment());
            entry.put("appointmentStatus", apt.getStatus());
            entry.put("nomorAntrian", apt.getNomorAntrian());
            entry.put("catatan", apt.getCatatan());

            // Include poli data
            if (j.getDokter() != null && j.getDokter().getPoli() != null) {
                Map<String, Object> poli = new HashMap<>();
                poli.put("namaPoli", j.getDokter().getPoli().getNamaPoli());
                Map<String, Object> dokterMap = new HashMap<>();
                dokterMap.put("poli", poli);
                entry.put("dokter", dokterMap);
            }

            // Include pasien name
            if (apt.getUser() != null) {
                entry.put("namaPasien", apt.getUser().getNama());
            }

            jadwalWithDates.add(entry);
        }

        return jadwalWithDates;
    }
}
