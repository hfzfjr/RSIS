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
import rsis.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DokterService dokterService;

    @Autowired
    private IdGeneratorService idGeneratorService;

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
        // Check if there are any active appointments (MENUNGGU or DIKONFIRMASI)
        List<Appointment> activeAppointments = appointmentRepository.findByJadwal_IdJadwalAndStatusIn(
                jadwalId, List.of("MENUNGGU", "DIKONFIRMASI"));
        if (!activeAppointments.isEmpty()) {
            throw new RuntimeException("Cannot delete jadwal with active appointments");
        }
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
        java.util.Optional<String> latestId = jadwalPraktikRepository.findLatestJadwalId();
        if (latestId.isPresent()) {
            String id = latestId.get();
            // Extract numeric part from jdw-XXX
            String numericPart = id.substring(4); // Remove "jdw-"
            long maxIdNumber = Long.parseLong(numericPart);
            return idGeneratorService.generateJadwalId(maxIdNumber);
        } else {
            // No jadwal exists yet, start from 1
            return idGeneratorService.generateJadwalId(0);
        }
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

    // ====================
    // Extended Jadwal Management (from AdminRSService)
    // ====================

    @Transactional
    public JadwalPraktik createJadwal(String idUser, String hari, LocalDate tanggal, LocalTime jamMulai,
            LocalTime jamSelesai, String statusKetersediaan, int kuota, String idPoli) {
        JadwalPraktik newJadwal = new JadwalPraktik();
        newJadwal.setIdJadwal(generateJadwalId());

        if (idUser != null && !idUser.isEmpty()) {
            Dokter dokter = dokterRepository.findById(idUser)
                    .orElseThrow(() -> new RuntimeException("Dokter tidak ditemukan"));

            // Also update Dokter's poli if idPoli is provided
            if (idPoli != null && !idPoli.isEmpty()) {
                Poli poli = poliRepository.findById(idPoli)
                        .orElseThrow(() -> new RuntimeException("Poli tidak ditemukan"));
                dokter.setPoli(poli);
                dokterRepository.save(dokter);
            }

            newJadwal.setDokter(dokter);
        }

        newJadwal.setHari(hari != null ? hari.toUpperCase() : null);
        newJadwal.setTanggal(tanggal);
        newJadwal.setJamMulai(jamMulai);
        newJadwal.setJamSelesai(jamSelesai);
        newJadwal.setStatusKetersediaan(statusKetersediaan);
        newJadwal.setKuota(kuota);
        newJadwal.setSisaKuota(kuota);

        return jadwalPraktikRepository.save(newJadwal);
    }

    @Transactional
    public List<JadwalPraktik> createBulkRecurringJadwal(
            String idUser, String idPoli, List<String> hariList,
            LocalDate tanggalMulai, String sampaiYearMonth,
            LocalTime jamMulai, LocalTime jamSelesai,
            String statusKetersediaan, int kuota) {

        Dokter dokter = dokterRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Dokter tidak ditemukan"));

        // Update poli dokter jika idPoli diberikan
        if (idPoli != null && !idPoli.isEmpty()) {
            Poli poli = poliRepository.findById(idPoli)
                    .orElseThrow(() -> new RuntimeException("Poli tidak ditemukan"));
            dokter.setPoli(poli);
            dokterRepository.save(dokter);
        }

        // Parse batas akhir → hari terakhir bulan yang dipilih
        YearMonth ym = YearMonth.parse(sampaiYearMonth); // format: "2025-07"
        LocalDate tanggalAkhir = ym.atEndOfMonth();

        // Map nama hari Indonesia ke DayOfWeek Java
        Map<String, DayOfWeek> hariMap = new HashMap<>();
        hariMap.put("SENIN", DayOfWeek.MONDAY);
        hariMap.put("SELASA", DayOfWeek.TUESDAY);
        hariMap.put("RABU", DayOfWeek.WEDNESDAY);
        hariMap.put("KAMIS", DayOfWeek.THURSDAY);
        hariMap.put("JUMAT", DayOfWeek.FRIDAY);
        hariMap.put("SABTU", DayOfWeek.SATURDAY);
        hariMap.put("MINGGU", DayOfWeek.SUNDAY);

        Set<DayOfWeek> targetDays = hariList.stream()
                .map(h -> hariMap.get(h.toUpperCase()))
                .filter(d -> d != null)
                .collect(Collectors.toSet());

        // Map DayOfWeek balik ke nama hari Indonesia (untuk disimpan ke field hari)
        Map<DayOfWeek, String> dayToHariIndo = new HashMap<>();
        dayToHariIndo.put(DayOfWeek.MONDAY, "SENIN");
        dayToHariIndo.put(DayOfWeek.TUESDAY, "SELASA");
        dayToHariIndo.put(DayOfWeek.WEDNESDAY, "RABU");
        dayToHariIndo.put(DayOfWeek.THURSDAY, "KAMIS");
        dayToHariIndo.put(DayOfWeek.FRIDAY, "JUMAT");
        dayToHariIndo.put(DayOfWeek.SATURDAY, "SABTU");
        dayToHariIndo.put(DayOfWeek.SUNDAY, "MINGGU");

        List<JadwalPraktik> result = new ArrayList<>();
        LocalDate cur = tanggalMulai;

        while (!cur.isAfter(tanggalAkhir)) {
            if (targetDays.contains(cur.getDayOfWeek())) {
                JadwalPraktik jp = new JadwalPraktik();
                jp.setIdJadwal(generateJadwalId()); // dipanggil per item agar ID sequential tidak duplikat
                jp.setDokter(dokter);
                jp.setHari(dayToHariIndo.get(cur.getDayOfWeek()));
                jp.setTanggal(cur);
                jp.setJamMulai(jamMulai);
                jp.setJamSelesai(jamSelesai);
                jp.setStatusKetersediaan(statusKetersediaan);
                jp.setKuota(kuota);
                jp.setSisaKuota(kuota);
                jadwalPraktikRepository.save(jp); // save satu-satu agar generateJadwalId() baca ID terbaru
                result.add(jp);
            }
            cur = cur.plusDays(1);
        }

        return result;
    }

    @Transactional
    public JadwalPraktik updateJadwal(String idJadwal, String idUser, String hari, LocalDate tanggal,
            LocalTime jamMulai, LocalTime jamSelesai, String statusKetersediaan, int kuota, String idPoli) {
        JadwalPraktik existingJadwal = jadwalPraktikRepository.findById(idJadwal)
                .orElseThrow(() -> new RuntimeException("Jadwal tidak ditemukan"));

        if (idUser != null && !idUser.isEmpty()) {
            Dokter dokter = dokterRepository.findById(idUser)
                    .orElseThrow(() -> new RuntimeException("Dokter tidak ditemukan"));

            // Also update Dokter's poli if idPoli is provided
            if (idPoli != null && !idPoli.isEmpty()) {
                Poli poli = poliRepository.findById(idPoli)
                        .orElseThrow(() -> new RuntimeException("Poli tidak ditemukan"));
                dokter.setPoli(poli);
                dokterRepository.save(dokter);
            }

            existingJadwal.setDokter(dokter);
        }

        existingJadwal.setHari(hari != null ? hari.toUpperCase() : null);
        existingJadwal.setTanggal(tanggal);
        existingJadwal.setJamMulai(jamMulai);
        existingJadwal.setJamSelesai(jamSelesai);
        existingJadwal.setStatusKetersediaan(statusKetersediaan);

        // Adjust sisaKuota based on new kuota
        int selisih = kuota - existingJadwal.getKuota();
        int newSisa = existingJadwal.getSisaKuota() + selisih;
        if (newSisa < 0)
            newSisa = 0;
        existingJadwal.setSisaKuota(newSisa);
        existingJadwal.setKuota(kuota);

        return jadwalPraktikRepository.save(existingJadwal);
    }

    public List<JadwalPraktik> getAllJadwalWithEnrichedDokter() {
        List<JadwalPraktik> jadwals = jadwalPraktikRepository.findAll().stream()
                .filter(j -> j.getIsActive() == null || j.getIsActive())
                .toList();
        // Populate transient fields for each active jadwal's dokter
        for (JadwalPraktik jadwal : jadwals) {
            if (jadwal.getDokter() != null) {
                dokterService.enrichWithUserData(jadwal.getDokter());
            }
        }
        return jadwals;
    }

    @Transactional
    public void softDeleteJadwal(String idJadwal) {
        JadwalPraktik existingJadwal = jadwalPraktikRepository.findById(idJadwal)
                .orElseThrow(() -> new RuntimeException("Jadwal tidak ditemukan"));
        existingJadwal.setIsActive(false);
        jadwalPraktikRepository.save(existingJadwal);
    }
}
