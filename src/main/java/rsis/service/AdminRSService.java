package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Poli;
import rsis.model.Spesialisasi;
import rsis.repository.AppointmentRepository;
import rsis.repository.DokterRepository;
import rsis.repository.JadwalPraktikRepository;
import rsis.repository.PoliRepository;
import rsis.repository.SpesialisasiRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminRSService {

    @Autowired
    private DokterRepository dokterRepository;

    @Autowired
    private PoliRepository poliRepository;

    @Autowired
    private SpesialisasiRepository spesialisasiRepository;

    @Autowired
    private JadwalPraktikRepository jadwalPraktikRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Dokter Management
    @Transactional
    public Dokter createDokter(Dokter dokter) {
        String idDokter = generateDokterId();
        dokter.setIdDokter(idDokter);
        dokter.setRole("DOKTER");
        return dokterRepository.save(dokter);
    }

    @Transactional
    public Dokter updateDokter(Dokter dokter) {
        return dokterRepository.save(dokter);
    }

    @Transactional
    public void deleteDokter(String dokterId) {
        dokterRepository.deleteById(dokterId);
    }

    public List<Dokter> getAllDokter() {
        return dokterRepository.findAll();
    }

    public Optional<Dokter> getDokterById(String dokterId) {
        return dokterRepository.findById(dokterId);
    }

    // Poli Management
    @Transactional
    public Poli createPoli(Poli poli) {
        String idPoli = generatePoliId();
        poli.setIdPoli(idPoli);
        return poliRepository.save(poli);
    }

    @Transactional
    public Poli updatePoli(Poli poli) {
        return poliRepository.save(poli);
    }

    @Transactional
    public void deletePoli(String poliId) {
        poliRepository.deleteById(poliId);
    }

    public List<Poli> getAllPoli() {
        return poliRepository.findAll();
    }

    // Spesialisasi Management
    @Transactional
    public Spesialisasi createSpesialisasi(Spesialisasi spesialisasi) {
        return spesialisasiRepository.save(spesialisasi);
    }

    @Transactional
    public Spesialisasi updateSpesialisasi(Spesialisasi spesialisasi) {
        return spesialisasiRepository.save(spesialisasi);
    }

    @Transactional
    public void deleteSpesialisasi(String spesialisasiId) {
        spesialisasiRepository.deleteById(spesialisasiId);
    }

    public List<Spesialisasi> getAllSpesialisasi() {
        return spesialisasiRepository.findAll();
    }

    // Jadwal Management
    @Transactional
    public JadwalPraktik updateJadwal(JadwalPraktik jadwal) {
        return jadwalPraktikRepository.save(jadwal);
    }

    public List<JadwalPraktik> getAllJadwal() {
        return jadwalPraktikRepository.findAll();
    }

    // Statistik Methods (as per class diagram AdminRS)
    public Long getTotalPasienHariIni() {
        LocalDate today = LocalDate.now();
        return appointmentRepository.countConfirmedAppointmentsByDate(today);
    }

    public Long getTotalPasienBulanIni() {
        LocalDate today = LocalDate.now();
        return getTotalPasienBulanIni(today.getMonthValue(), today.getYear());
    }

    public Long getTotalPasienBulanIni(int bulan, int tahun) {
        YearMonth month = YearMonth.of(tahun, bulan);
        return appointmentRepository.countConfirmedAppointmentsByMonth(month.atDay(1), month.plusMonths(1).atDay(1));
    }

    public String getDokterTersibuk() {
        LocalDate today = LocalDate.now();
        return getDokterTersibuk(today.getMonthValue(), today.getYear());
    }

    public String getDokterTersibuk(int bulan, int tahun) {
        YearMonth month = YearMonth.of(tahun, bulan);
        List<Object[]> results = appointmentRepository.findBusiestDokterByMonth(month.atDay(1), month.plusMonths(1).atDay(1));
        if (results.isEmpty()) {
            return "N/A";
        }
        Object[] result = results.get(0);
        String dokterId = (String) result[0];
        Optional<Dokter> dokterOpt = dokterRepository.findById(dokterId);
        return dokterOpt.map(d -> d.getNama()).orElse("N/A");
    }

    public Map<String, Long> getPasienPerHari() {
        LocalDate today = LocalDate.now();
        return getPasienPerHari(today.getMonthValue(), today.getYear());
    }

    public Map<String, Long> getPasienPerHari(int bulan, int tahun) {
        YearMonth month = YearMonth.of(tahun, bulan);
        List<Object[]> results = appointmentRepository.findPatientsPerDayByMonth(month.atDay(1), month.plusMonths(1).atDay(1));
        Map<String, Long> pasienPerHari = new HashMap<>();
        for (Object[] result : results) {
            String date = result[0].toString();
            Long count = ((Number) result[1]).longValue();
            pasienPerHari.put(date, count);
        }
        return pasienPerHari;
    }

    public Long getTotalDokter() {
        return dokterRepository.count();
    }

    public Long getTotalPoli() {
        return poliRepository.count();
    }

    private String generateDokterId() {
        Optional<String> latestId = dokterRepository.findLatestDokterId();
        if (latestId.isPresent()) {
            String id = latestId.get();
            int num = Integer.parseInt(id.substring(4));
            return String.format("dkt-%04d", num + 1);
        }
        return "dkt-0001";
    }

    private String generatePoliId() {
        Optional<String> latestId = poliRepository.findLatestPoliId();
        if (latestId.isPresent()) {
            String id = latestId.get();
            int num = Integer.parseInt(id.substring(4));
            return String.format("pli-%04d", num + 1);
        }
        return "pli-0001";
    }
}
