package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.Appointment;
import rsis.model.User;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Pasien;
import rsis.repository.UserRepository;
import rsis.repository.PasienRepository;
import rsis.service.AppointmentService;
import rsis.service.NotifikasiService;
import rsis.service.PasienService;
import rsis.dto.BookingRequestDTO;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pasien")
public class PasienController {

    @Autowired
    private PasienService pasienService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotifikasiService notifikasiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasienRepository pasienRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void addNotifikasiToModel(String userId, Model model) {
        try {
            var notifikasis = notifikasiService.getNotifikasiByPenerimaId(userId);
            model.addAttribute("notifikasi", notifikasis);
        } catch (Exception e) {
            model.addAttribute("notifikasi", List.of());
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get pasien data
        Pasien pasien = pasienRepository.findByIdUser(user.getIdUser()).orElse(null);
        if (pasien != null) {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("role", user.getRole());
            model.addAttribute("nomorRekamMedis", pasien.getNomorRekamMedis());
            model.addAttribute("pasienId", pasien.getIdUser());
        } else {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("role", user.getRole());
        }

        // Set active menu
        model.addAttribute("activeMenu", "dashboard");

        // Get statistics
        String pasienId = pasien != null ? pasien.getIdUser() : user.getIdUser();
        List<Appointment> appointments = appointmentService.getAppointmentsByPasienId(pasienId);
        model.addAttribute("totalAppointment", appointments.size());

        List<Dokter> allDokters = pasienService.cariDokter(null);
        model.addAttribute("totalDokter", allDokters.size());

        // Get notifications
        addNotifikasiToModel(pasienId, model);

        // Get notifications count
        try {
            var notifikasis = notifikasiService.getNotifikasiByPenerimaId(pasienId);
            model.addAttribute("totalNotifikasi", notifikasis.size());
        } catch (Exception e) {
            model.addAttribute("totalNotifikasi", 0);
        }

        // Get available doctors for today (limit to 4 for display)
        List<Dokter> availableDoctors = allDokters.stream()
                .limit(4)
                .toList();
        model.addAttribute("dokters", availableDoctors);
        model.addAttribute("dokterTersedia", availableDoctors.size());

        // Get upcoming appointments (limit to 2 for display)
        List<Appointment> upcomingAppointments = appointments.stream()
                .limit(2)
                .toList();
        model.addAttribute("appointments", upcomingAppointments);

        return "pasien/dashboard";
    }

    @GetMapping("/profil")
    public String profil(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pasien pasien = pasienRepository.findByIdUser(user.getIdUser()).orElse(null);

        model.addAttribute("nama", user.getNama());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "profil");

        // Get notifications
        String userId = pasien != null ? pasien.getIdUser() : user.getIdUser();
        model.addAttribute("idUser", userId);
        addNotifikasiToModel(userId, model);

        if (pasien != null) {
            model.addAttribute("idUser", pasien.getIdUser());
            model.addAttribute("nomorRekamMedis", pasien.getNomorRekamMedis());
            model.addAttribute("nomorHp", user.getNomorHp() != null ? user.getNomorHp() : "");
            model.addAttribute("tanggalLahir",
                    pasien.getTanggalLahir() != null ? pasien.getTanggalLahir().toString() : "");
            model.addAttribute("alamat", pasien.getAlamat() != null ? pasien.getAlamat() : "");

            // Get statistics for pasien
            List<Appointment> appointments = appointmentService.getAppointmentsByPasienId(pasien.getIdUser());
            model.addAttribute("totalKunjungan", appointments.size());
            model.addAttribute("totalResep", 0); // TODO: Implement when resep feature is available
        } else {
            model.addAttribute("idUser", user.getIdUser());
            model.addAttribute("nomorHp", user.getNomorHp() != null ? user.getNomorHp() : "");
            model.addAttribute("tanggalLahir", "");
            model.addAttribute("nomorRekamMedis", "");
            model.addAttribute("alamat", "");
            model.addAttribute("totalKunjungan", 0);
            model.addAttribute("totalResep", 0);
        }

        return "pasien/profil";
    }

    @PostMapping("/profil")
    public String updateProfile(@RequestParam String namaLengkap,
            @RequestParam String nomorTelepon,
            @RequestParam String tanggalLahir,
            @RequestParam String alamat,
            @RequestParam String nomorRekamMedis,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Pasien pasien = pasienRepository.findByIdUser(user.getIdUser())
                    .orElseThrow(() -> new RuntimeException("Pasien not found"));

            // Update nama and nomorHp in user
            user.setNama(namaLengkap);
            user.setNomorHp(nomorTelepon);
            userRepository.save(user);

            // Update pasien fields
            pasienService.updateProfil(pasien.getIdUser(), namaLengkap, nomorRekamMedis, tanggalLahir, alamat);
            redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pasien/profil";
    }

    @PostMapping("/profil/password")
    public String changePassword(@RequestParam String passwordLama,
            @RequestParam String passwordBaru,
            @RequestParam String konfirmasiPassword,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate old password
            if (!passwordEncoder.matches(passwordLama, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Kata sandi lama tidak sesuai");
                return "redirect:/pasien/profil";
            }

            // Validate new password length
            if (passwordBaru.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "Kata sandi baru minimal 8 karakter");
                return "redirect:/pasien/profil";
            }

            // Validate password confirmation
            if (!passwordBaru.equals(konfirmasiPassword)) {
                redirectAttributes.addFlashAttribute("error",
                        "Konfirmasi kata sandi baru harus sama dengan kata sandi baru");
                return "redirect:/pasien/profil";
            }

            // Update password
            user.setPassword(passwordEncoder.encode(passwordBaru));
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Kata sandi berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pasien/profil";
    }

    @GetMapping("/cari-dokter")
    public String searchDoctors(@RequestParam(required = false) String keyword,
            @AuthenticationPrincipal UserDetails principal,
            Model model) {
        // Get user and pasien data
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pasien pasien = pasienRepository.findByIdUser(user.getIdUser()).orElse(null);

        // Add navbar attributes
        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "cari-dokter");

        // Get notifications
        String userId = pasien != null ? pasien.getIdUser() : user.getIdUser();
        addNotifikasiToModel(userId, model);

        if (pasien != null) {
            model.addAttribute("nomorRekamMedis", pasien.getNomorRekamMedis());
        }

        // Search doctors
        List<Dokter> dokters;
        if (keyword == null || keyword.isEmpty()) {
            dokters = pasienService.cariDokter(null);
        } else {
            dokters = pasienService.cariDokter(keyword);
        }
        model.addAttribute("dokters", dokters);
        model.addAttribute("keyword", keyword);
        return "pasien/cari-dokter";
    }

    @GetMapping("/jadwal-dokter/{dokterId}")
    public String showDoctorSchedule(@PathVariable String dokterId,
            @AuthenticationPrincipal UserDetails principal,
            Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "jadwal");

        // Get notifications
        addNotifikasiToModel(user.getIdUser(), model);

        // Get doctor schedule
        List<JadwalPraktik> jadwals = pasienService.lihatJadwalDokter(dokterId);
        model.addAttribute("jadwals", jadwals);
        model.addAttribute("dokterId", dokterId);
        return "pasien/jadwal-dokter";
    }

    @GetMapping("/jadwal-riwayat")
    public String jadwalRiwayat(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pasien pasien = pasienRepository.findByIdUser(user.getIdUser()).orElse(null);

        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "jadwal");

        // Get notifications
        String userId = pasien != null ? pasien.getIdUser() : user.getIdUser();
        addNotifikasiToModel(userId, model);

        if (pasien != null) {
            model.addAttribute("nomorRekamMedis", pasien.getNomorRekamMedis());
        }

        // Get all appointments for the pasien
        String pasienId = pasien != null ? pasien.getIdUser() : user.getIdUser();
        List<Appointment> appointments = appointmentService.getAppointmentsByPasienId(pasienId);
        model.addAttribute("appointments", appointments);

        return "pasien/jadwal-riwayat";
    }

    @GetMapping("/booking")
    public String showBookingForm(@AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String jadwalId,
            @RequestParam(required = false) String dokterId,
            Model model) {
        // Add navbar attributes
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pasien pasien = pasienRepository.findByIdUser(user.getIdUser()).orElse(null);
        String userId = pasien != null ? pasien.getIdUser() : user.getIdUser();

        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "booking");

        // Get notifications
        addNotifikasiToModel(userId, model);

        // If jadwalId is provided, fetch doctor and schedule data
        if (jadwalId != null && !jadwalId.isEmpty()) {
            Optional<JadwalPraktik> jadwalOpt = appointmentService.getJadwalById(jadwalId);
            if (jadwalOpt.isPresent()) {
                JadwalPraktik jadwal = jadwalOpt.get();
                model.addAttribute("jadwal", jadwal);
                model.addAttribute("dokter", jadwal.getDokter());
                model.addAttribute("spesialisasi", jadwal.getDokter().getSpesialisasi());
                model.addAttribute("poli", jadwal.getDokter().getPoli());

                // Pre-fill bookingRequest with jadwalId
                BookingRequestDTO bookingRequest = new BookingRequestDTO();
                bookingRequest.setJadwalId(jadwalId);
                model.addAttribute("bookingRequest", bookingRequest);
            } else {
                model.addAttribute("error", "Jadwal tidak ditemukan");
                model.addAttribute("bookingRequest", new BookingRequestDTO());
            }
        } else if (dokterId != null && !dokterId.isEmpty()) {
            // If dokterId is provided, fetch doctor data and available schedules
            Optional<rsis.model.Dokter> dokterOpt = appointmentService.getDokterById(dokterId);
            if (dokterOpt.isPresent()) {
                rsis.model.Dokter dokter = dokterOpt.get();
                model.addAttribute("dokter", dokter);
                model.addAttribute("spesialisasi", dokter.getSpesialisasi());
                model.addAttribute("poli", dokter.getPoli());

                // Fetch available schedules for this doctor
                List<JadwalPraktik> availableJadwal = appointmentService.getJadwalByDokterId(dokterId);
                model.addAttribute("availableDates", availableJadwal);

                model.addAttribute("bookingRequest", new BookingRequestDTO());
            } else {
                model.addAttribute("error", "Dokter tidak ditemukan");
                model.addAttribute("bookingRequest", new BookingRequestDTO());
            }
        } else {
            model.addAttribute("bookingRequest", new BookingRequestDTO());
        }

        return "pasien/booking";
    }

    @PostMapping("/booking")
    public String bookAppointment(@ModelAttribute BookingRequestDTO bookingRequest,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            // Get pasien data
            User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Pasien pasien = pasienRepository.findByIdUser(user.getIdUser())
                    .orElseThrow(() -> new RuntimeException("Pasien not found"));

            // Set pasienId from logged-in user
            bookingRequest.setPasienId(pasien.getIdUser());

            Appointment appointment = appointmentService.bookAppointment(bookingRequest);

            // Send notification to pasien
            notifikasiService.kirimNotifikasi(pasien.getIdUser(),
                    "Appointment berhasil dibuat dengan ID: " + appointment.getIdAppointment(),
                    "BOOKING");

            redirectAttributes.addFlashAttribute("success", "Appointment berhasil dibuat!");
            return "redirect:/pasien/jadwal-riwayat";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/pasien/booking";
        }
    }
}
