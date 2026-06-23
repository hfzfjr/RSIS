package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.Appointment;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Pasien;
import rsis.repository.AppointmentRepository;
import rsis.repository.DokterRepository;
import rsis.repository.JadwalPraktikRepository;
import rsis.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DokterService {

    @Autowired
    private DokterRepository dokterRepository;

    @Autowired
    private JadwalPraktikRepository jadwalPraktikRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    public List<JadwalPraktik> getJadwalByDokterId(String dokterId) {
        List<JadwalPraktik> jadwals = jadwalPraktikRepository.findByDokter_IdUser(dokterId);
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

    @Transactional
    public JadwalPraktik updateJadwal(JadwalPraktik jadwal) {
        return jadwalPraktikRepository.save(jadwal);
    }

    @Transactional
    public JadwalPraktik createJadwal(JadwalPraktik jadwal) {
        String idJadwal = generateJadwalId();
        jadwal.setIdJadwal(idJadwal);
        jadwal.setStatusKetersediaan("TERSEDIA");
        jadwal.setSisaKuota(jadwal.getKuota());
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

    public boolean cekKetersediaan(String jadwalId) {
        Optional<JadwalPraktik> jadwalOpt = jadwalPraktikRepository.findById(jadwalId);
        return jadwalOpt.map(JadwalPraktik::cekTersedia).orElse(false);
    }

    public List<Appointment> getDaftarPasien(String dokterId) {
        return appointmentRepository.findByJadwal_Dokter_IdUser(dokterId);
    }

    public List<Appointment> getPendingAppointments(String dokterId) {
        return appointmentRepository.findPendingAppointmentsByDokterId(dokterId);
    }

    @Transactional
    public void konfirmasiAppointment(String appointmentId) {
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
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }
        Appointment appointment = appointmentOpt.get();
        appointment.tolak(alasan);
        appointmentRepository.save(appointment);

        // Restore quota
        JadwalPraktik jadwal = appointment.getJadwal();
        jadwal.tambahKuota();
        jadwalPraktikRepository.save(jadwal);
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
     * Centralized method to populate transient fields of Dokter from User table
     * This method should be used by all services to avoid duplication
     */
    public Dokter enrichWithUserData(Dokter dokter) {
        userRepository.findById(dokter.getIdUser()).ifPresent(user -> {
            dokter.setNama(user.getNama());
            dokter.setEmail(user.getEmail());
            dokter.setNomorHp(user.getNomorHp());
            dokter.setPassword(user.getPassword());
            dokter.setRole(user.getRole());
        });
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
}
