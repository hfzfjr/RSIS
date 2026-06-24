package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rsis.model.Appointment;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Poli;
import rsis.model.Spesialisasi;
import rsis.repository.AppointmentRepository;
import rsis.repository.DokterRepository;
import rsis.repository.PoliRepository;
import rsis.repository.SpesialisasiRepository;
import rsis.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DokterService {

    @Autowired
    private DokterRepository dokterRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpesialisasiRepository spesialisasiRepository;

    @Autowired
    private PoliRepository poliRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private IdGeneratorService idGeneratorService;

    public List<Appointment> getDaftarPasien(String dokterId) {
        return appointmentRepository.findByJadwal_Dokter_IdUser(dokterId);
    }

    public List<Appointment> getPendingAppointments(String dokterId) {
        return appointmentRepository.findPendingAppointmentsByDokterId(dokterId);
    }

    @Transactional
    public void konfirmasiAppointment(String appointmentId) {
        if (appointmentId == null) {
            throw new RuntimeException("Appointment ID cannot be null");
        }
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }
        Appointment appointment = appointmentOpt.get();
        appointment.konfirmasi();
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void tolakAppointment(String appointmentId, String alasan) {
        if (appointmentId == null) {
            throw new RuntimeException("Appointment ID cannot be null");
        }
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }
        Appointment appointment = appointmentOpt.get();
        appointment.tolak(alasan);
        appointmentRepository.save(appointment);

        // Restore quota
        JadwalPraktik jadwal = appointment.getJadwal();
        if (jadwal != null) {
            jadwal.tambahKuota();
            // Need to use JadwalPraktikService to save
        }
    }

    /**
     * Centralized method to populate transient fields of Dokter from User table
     * This method should be used by all services to avoid duplication
     */
    public Dokter enrichWithUserData(Dokter dokter) {
        String userId = dokter.getIdUser();
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                dokter.setNama(user.getNama());
                dokter.setEmail(user.getEmail());
                dokter.setNomorHp(user.getNomorHp());
                dokter.setPassword(user.getPassword());
                dokter.setRole(user.getRole());
            });
        }
        return dokter;
    }

    /**
     * Enrich a list of Dokter objects with user data
     */
    public List<Dokter> enrichAllWithUserData(List<Dokter> dokterList) {
        return dokterList.stream()
                .map(this::enrichWithUserData)
                .toList();
    }

    // ====================
    // Dokter Management
    // ====================

    @Transactional
    public Dokter createDokter(Dokter dokter) {
        dokter.setRole("DOKTER");
        return dokterRepository.save(dokter);
    }

    @Transactional
    public Dokter createDokter(String nama, String email, String password, String nomorHp,
            String spesialisasiId, String poliId, MultipartFile dokterImage) throws IOException {
        // Generate user ID
        String userId = idGeneratorService.generateDokterId(dokterRepository.count());

        // Generate nomor STR automatically
        String nomorStr = idGeneratorService.generateNomorStr(userId);

        // Encode password before saving
        String encodedPassword = authService.encodePassword(password);

        // Handle file upload
        String imageUrl = null;
        if (dokterImage != null && !dokterImage.isEmpty()) {
            imageUrl = fileStorageService.saveFile(dokterImage, userId);
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

    @Transactional
    public Dokter updateDokter(Dokter dokter) {
        if (dokter == null) {
            throw new RuntimeException("Dokter cannot be null");
        }
        return dokterRepository.save(dokter);
    }

    @Transactional
    public Dokter updateDokter(String idUser, String nama, String nomorHp, String nomorStr,
            String spesialisasiId, String poliId) {
        if (idUser == null) {
            throw new RuntimeException("User ID cannot be null");
        }
        Dokter existingDokter = dokterRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Dokter tidak ditemukan"));

        existingDokter.setNama(nama);
        existingDokter.setNomorHp(nomorHp);
        existingDokter.setNomorStr(nomorStr);

        if (spesialisasiId != null && !spesialisasiId.isEmpty()) {
            Spesialisasi spesialisasi = spesialisasiRepository.findById(spesialisasiId)
                    .orElseThrow(() -> new RuntimeException("Spesialisasi tidak ditemukan"));
            existingDokter.setSpesialisasi(spesialisasi);
        } else {
            existingDokter.setSpesialisasi(null);
        }

        if (poliId != null && !poliId.isEmpty()) {
            Poli poli = poliRepository.findById(poliId)
                    .orElseThrow(() -> new RuntimeException("Poli tidak ditemukan"));
            existingDokter.setPoli(poli);
        } else {
            existingDokter.setPoli(null);
        }

        return dokterRepository.save(existingDokter);
    }

    @Transactional
    public void deleteDokter(String dokterId) {
        if (dokterId == null) {
            throw new RuntimeException("Dokter ID cannot be null");
        }
        dokterRepository.deleteById(dokterId);
    }

    @Transactional
    public void softDeleteDokter(String dokterId) {
        if (dokterId == null) {
            throw new RuntimeException("Dokter ID cannot be null");
        }
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
        List<Dokter> dokters = dokterRepository.findAllActive();
        return enrichAllWithUserData(dokters);
    }

    public Optional<Dokter> getDokterById(String dokterId) {
        if (dokterId == null) {
            return Optional.empty();
        }
        return dokterRepository.findById(dokterId);
    }

    // ====================
    // Spesialisasi Management
    // ====================

    @Transactional
    public Spesialisasi createSpesialisasi(Spesialisasi spesialisasi) {
        if (spesialisasi == null) {
            throw new RuntimeException("Spesialisasi cannot be null");
        }
        return spesialisasiRepository.save(spesialisasi);
    }

    @Transactional
    public Spesialisasi updateSpesialisasi(Spesialisasi spesialisasi) {
        if (spesialisasi == null) {
            throw new RuntimeException("Spesialisasi cannot be null");
        }
        return spesialisasiRepository.save(spesialisasi);
    }

    @Transactional
    public void deleteSpesialisasi(String spesialisasiId) {
        if (spesialisasiId == null) {
            throw new RuntimeException("Spesialisasi ID cannot be null");
        }
        spesialisasiRepository.deleteById(spesialisasiId);
    }

    public List<Spesialisasi> getAllSpesialisasi() {
        return spesialisasiRepository.findAll();
    }
}
