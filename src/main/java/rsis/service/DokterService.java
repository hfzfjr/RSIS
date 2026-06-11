package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.Appointment;
import rsis.model.JadwalPraktik;
import rsis.model.Pasien;
import rsis.repository.AppointmentRepository;
import rsis.repository.DokterRepository;
import rsis.repository.JadwalPraktikRepository;

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

    public List<JadwalPraktik> getJadwalByDokterId(String dokterId) {
        return jadwalPraktikRepository.findByDokter_IdDokter(dokterId);
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
        return appointmentRepository.findByJadwal_Dokter_IdDokter(dokterId);
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
        return "jdw-" + System.currentTimeMillis();
    }
}
