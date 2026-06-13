package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.dto.BookingRequestDTO;
import rsis.model.Appointment;
import rsis.model.JadwalPraktik;
import rsis.model.Pasien;
import rsis.repository.AppointmentRepository;
import rsis.repository.JadwalPraktikRepository;
import rsis.repository.PasienRepository;
import rsis.repository.DokterRepository;
import rsis.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private JadwalPraktikRepository jadwalPraktikRepository;

    @Autowired
    private PasienRepository pasienRepository;

    @Autowired
    private DokterRepository dokterRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Appointment bookAppointment(BookingRequestDTO bookingRequest) {
        // Validate pasien exists and profile is complete
        Optional<Pasien> pasienOpt = pasienRepository.findById(bookingRequest.getPasienId());
        if (pasienOpt.isEmpty()) {
            throw new RuntimeException("Pasien not found");
        }

        Pasien pasien = pasienOpt.get();
        if (!pasien.isProfileComplete()) {
            throw new RuntimeException("Pasien profile is incomplete. Please complete your profile first.");
        }

        // Validate jadwal exists and has available quota
        Optional<JadwalPraktik> jadwalOpt = jadwalPraktikRepository.findById(bookingRequest.getJadwalId());
        if (jadwalOpt.isEmpty()) {
            throw new RuntimeException("Jadwal not found");
        }

        JadwalPraktik jadwal = jadwalOpt.get();
        if (!jadwal.cekTersedia()) {
            throw new RuntimeException("Jadwal is not available");
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setIdAppointment(generateAppointmentId());
        appointment.setUser(pasien);
        appointment.setJadwal(jadwal);
        appointment.setTanggalBooking(LocalDate.now());
        appointment.setStatus("MENUNGGU");
        appointment.setCatatan(bookingRequest.getCatatan());
        appointment.setNomorAntrian(generateNomorAntrian(jadwal));

        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Reduce quota
        jadwal.kurangiKuota();
        jadwalPraktikRepository.save(jadwal);

        return savedAppointment;
    }

    @Transactional
    public void cancelAppointment(String appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();

        // Only allow cancellation if status is MENUNGGU or DIKONFIRMASI
        if (!"MENUNGGU".equals(appointment.getStatus()) && !"DIKONFIRMASI".equals(appointment.getStatus())) {
            throw new RuntimeException("Cannot cancel appointment with status: " + appointment.getStatus());
        }

        appointment.batalkan();
        appointmentRepository.save(appointment);

        // Restore quota
        JadwalPraktik jadwal = appointment.getJadwal();
        jadwal.tambahKuota();
        jadwalPraktikRepository.save(jadwal);
    }

    @Transactional
    public void rescheduleAppointment(String appointmentId, String newJadwalId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();

        // Only allow reschedule if status is MENUNGGU
        if (!"MENUNGGU".equals(appointment.getStatus())) {
            throw new RuntimeException("Cannot reschedule appointment with status: " + appointment.getStatus());
        }

        // Validate new jadwal
        Optional<JadwalPraktik> newJadwalOpt = jadwalPraktikRepository.findById(newJadwalId);
        if (newJadwalOpt.isEmpty()) {
            throw new RuntimeException("New jadwal not found");
        }

        JadwalPraktik newJadwal = newJadwalOpt.get();
        if (!newJadwal.cekTersedia()) {
            throw new RuntimeException("New jadwal is not available");
        }

        // Restore old quota
        JadwalPraktik oldJadwal = appointment.getJadwal();
        oldJadwal.tambahKuota();
        jadwalPraktikRepository.save(oldJadwal);

        // Update appointment
        appointment.setJadwal(newJadwal);
        appointment.setNomorAntrian(generateNomorAntrian(newJadwal));
        appointmentRepository.save(appointment);

        // Reduce new quota
        newJadwal.kurangiKuota();
        jadwalPraktikRepository.save(newJadwal);
    }

    @Transactional
    public void confirmAppointment(String appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }

        Appointment appointment = appointmentOpt.get();
        appointment.konfirmasi();
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void rejectAppointment(String appointmentId, String alasan) {
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

    public List<Appointment> getAppointmentsByPasienId(String pasienId) {
        return appointmentRepository.findByUser_IdUser(pasienId);
    }

    public List<Appointment> getAppointmentsByDokterId(String dokterId) {
        return appointmentRepository.findByJadwal_Dokter_IdUser(dokterId);
    }

    public List<Appointment> getPendingAppointmentsByDokterId(String dokterId) {
        return appointmentRepository.findPendingAppointmentsByDokterId(dokterId);
    }

    public Optional<Appointment> getAppointmentById(String appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }

    public Optional<JadwalPraktik> getJadwalById(String jadwalId) {
        return jadwalPraktikRepository.findById(jadwalId);
    }

    public Optional<rsis.model.Dokter> getDokterById(String dokterId) {
        Optional<rsis.model.Dokter> dokterOpt = dokterRepository.findById(dokterId);
        if (dokterOpt.isPresent()) {
            rsis.model.Dokter dokter = dokterOpt.get();
            populateDokterWithUserData(dokter);
            return Optional.of(dokter);
        }
        return dokterOpt;
    }

    private void populateDokterWithUserData(rsis.model.Dokter dokter) {
        userRepository.findById(dokter.getIdUser()).ifPresent(user -> {
            dokter.setNama(user.getNama());
            dokter.setEmail(user.getEmail());
            dokter.setPassword(user.getPassword());
            dokter.setRole(user.getRole());
        });
    }

    public List<JadwalPraktik> getJadwalByDokterId(String dokterId) {
        return jadwalPraktikRepository.findByDokter_IdUser(dokterId);
    }

    private String generateAppointmentId() {
        return "apt-" + System.currentTimeMillis();
    }

    private String generateNomorAntrian(JadwalPraktik jadwal) {
        int nomor = jadwal.getKuota() - jadwal.getSisaKuota() + 1;
        return String.format("A%03d", nomor);
    }
}
