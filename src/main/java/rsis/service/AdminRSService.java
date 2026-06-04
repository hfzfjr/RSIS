package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.dto.StatistikDTO;
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

    // Statistik
    public StatistikDTO getStatistikHariIni() {
        LocalDate today = LocalDate.now();
        Long totalPasienHariIni = appointmentRepository.countConfirmedAppointmentsByDate(today);
        Long totalPasienBulanIni = appointmentRepository.countConfirmedAppointmentsByMonth(
                today.getMonthValue(), today.getYear());

        String dokterTersibuk = getDokterTersibuk(today.getMonthValue(), today.getYear());
        Map<String, Long> pasienPerHari = getPasienPerHari(today.getMonthValue(), today.getYear());

        Long totalDokter = dokterRepository.count();
        Long totalPoli = poliRepository.count();

        return new StatistikDTO(
                totalPasienHariIni,
                totalPasienBulanIni,
                dokterTersibuk,
                pasienPerHari,
                totalPasienHariIni,
                0L,
                totalDokter,
                totalPoli);
    }

    public StatistikDTO getStatistikBulanan(int bulan, int tahun) {
        Long totalPasienBulanIni = appointmentRepository.countConfirmedAppointmentsByMonth(bulan, tahun);
        Long totalAppointmentBatal = appointmentRepository.countCanceledAppointmentsByMonth(bulan, tahun);

        String dokterTersibuk = getDokterTersibuk(bulan, tahun);
        Map<String, Long> pasienPerHari = getPasienPerHari(bulan, tahun);

        Long totalDokter = dokterRepository.count();
        Long totalPoli = poliRepository.count();

        return new StatistikDTO(
                0L,
                totalPasienBulanIni,
                dokterTersibuk,
                pasienPerHari,
                totalPasienBulanIni,
                totalAppointmentBatal,
                totalDokter,
                totalPoli);
    }

    private String getDokterTersibuk(int bulan, int tahun) {
        List<Object[]> results = appointmentRepository.findBusiestDokterByMonth(bulan, tahun);
        if (results.isEmpty()) {
            return "N/A";
        }
        Object[] result = results.get(0);
        String dokterId = (String) result[0];
        Optional<Dokter> dokterOpt = dokterRepository.findById(dokterId);
        return dokterOpt.map(d -> d.getIdDokter()).orElse("N/A");
    }

    private Map<String, Long> getPasienPerHari(int bulan, int tahun) {
        List<Object[]> results = appointmentRepository.findPatientsPerDayByMonth(bulan, tahun);
        Map<String, Long> pasienPerHari = new HashMap<>();
        for (Object[] result : results) {
            String date = result[0].toString();
            Long count = (Long) result[1];
            pasienPerHari.put(date, count);
        }
        return pasienPerHari;
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
