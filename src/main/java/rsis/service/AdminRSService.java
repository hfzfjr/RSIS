package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Poli;
import rsis.model.Spesialisasi;
import rsis.repository.AppointmentRepository;
import rsis.repository.DokterRepository;
import rsis.repository.JadwalPraktikRepository;
import rsis.repository.PoliRepository;
import rsis.repository.SpesialisasiRepository;
import rsis.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    // Dokter Management
    @Transactional
    public Dokter createDokter(Dokter dokter) {
        dokter.setRole("DOKTER");
        return dokterRepository.save(dokter);
    }

    @Transactional
    public Dokter createDokter(String nama, String email, String password, String nomorHp, 
                               String spesialisasiId, String poliId, MultipartFile dokterImage) {
        // Generate user ID
        String userId = generateDokterId();
        
        // Generate nomor STR automatically
        String nomorStr = generateNomorStr();
        
        // Encode password before saving
        String encodedPassword = passwordEncoder.encode(password);
        
        // Handle file upload
        String imageUrl = null;
        if (dokterImage != null && !dokterImage.isEmpty()) {
            imageUrl = saveUploadedFile(dokterImage, userId);
        }
        
        // Create new Dokter
        Dokter dokter = new Dokter();
        dokter.setIdUser(userId);
        dokter.setNama(nama);
        dokter.setEmail(email);
        dokter.setPassword(encodedPassword);
        dokter.setNomorHp(nomorHp);
        dokter.setNomorStr(nomorStr);
        dokter.setRole("DOKTER");
        dokter.setDokterImage(imageUrl);
        
        // Set Spesialisasi
        if (spesialisasiId != null && !spesialisasiId.isEmpty()) {
            spesialisasiRepository.findById(spesialisasiId).ifPresent(dokter::setSpesialisasi);
        }
        
        // Set Poli
        if (poliId != null && !poliId.isEmpty()) {
            poliRepository.findById(poliId).ifPresent(dokter::setPoli);
        }
        
        // Save Dokter (this will also save the User parent due to JOINED inheritance)
        return dokterRepository.save(dokter);
    }

    private String saveUploadedFile(MultipartFile file, String userId) throws RuntimeException {
        try {
            // Create upload directory if it doesn't exist
            Path uploadDir = Paths.get(uploadPath, "dokter");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String uniqueFilename = userId + "_" + UUID.randomUUID().toString() + fileExtension;
            
            // Save file
            Path filePath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return relative URL path
            return "/uploads/dokter/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }
    }

    private String generateNomorStr() {
        // Generate STR number in format: STR-YYYYMMDD-XXXX
        // where XXXX is a sequential number for that day
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // Get the latest STR for today to determine the sequence number
        String prefix = "STR-" + dateStr + "-";
        Optional<Dokter> latestDokter = dokterRepository.findFirstByNomorStrStartingWithOrderByNomorStrDesc(prefix);
        
        int sequence = 1;
        if (latestDokter.isPresent()) {
            String latestStr = latestDokter.get().getNomorStr();
            String sequenceStr = latestStr.substring(prefix.length());
            try {
                sequence = Integer.parseInt(sequenceStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }
        
        return String.format("%s%04d", prefix, sequence);
    }

    private String generateDokterId() {
        Optional<String> latestId = userRepository.findLatestUserId();
        if (latestId.isPresent()) {
            String id = latestId.get();
            int num = Integer.parseInt(id.substring(4));
            return String.format("usr-%04d", num + 1);
        }
        return "usr-0001";
    }

    @Transactional
    public Dokter updateDokter(Dokter dokter) {
        return dokterRepository.save(dokter);
    }

    @Transactional
    public void deleteDokter(String dokterId) {
        dokterRepository.deleteById(dokterId);
    }

    @Transactional
    public void softDeleteDokter(String dokterId) {
        Optional<Dokter> dokterOpt = dokterRepository.findById(dokterId);
        if (dokterOpt.isPresent()) {
            Dokter dokter = dokterOpt.get();
            dokter.setIsActive(false);
            dokterRepository.save(dokter);
        } else {
            throw new RuntimeException("Dokter tidak ditemukan");
        }
    }

    public List<Dokter> getAllDokter() {
        List<Dokter> dokters = dokterRepository.findAll();
        // Populate transient fields from users table and filter only active doctors
        return dokters.stream()
                .filter(dokter -> dokter.getIsActive() != null && dokter.getIsActive())
                .map(dokter -> {
                    userRepository.findById(dokter.getIdUser()).ifPresent(user -> {
                        dokter.setNama(user.getNama());
                        dokter.setEmail(user.getEmail());
                        dokter.setPassword(user.getPassword());
                        dokter.setRole(user.getRole());
                    });
                    return dokter;
                }).toList();
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
        List<JadwalPraktik> jadwals = jadwalPraktikRepository.findAll();
        // Populate transient fields for each jadwal's dokter
        for (JadwalPraktik jadwal : jadwals) {
            if (jadwal.getDokter() != null) {
                Dokter dokter = jadwal.getDokter();
                userRepository.findById(dokter.getIdUser()).ifPresent(user -> {
                    dokter.setNama(user.getNama());
                    dokter.setEmail(user.getEmail());
                    dokter.setPassword(user.getPassword());
                    dokter.setRole(user.getRole());
                });
            }
        }
        return jadwals;
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
        List<Object[]> results = appointmentRepository.findBusiestDokterByMonth(month.atDay(1),
                month.plusMonths(1).atDay(1));
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
        List<Object[]> results = appointmentRepository.findPatientsPerDayByMonth(month.atDay(1),
                month.plusMonths(1).atDay(1));
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
