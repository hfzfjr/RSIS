package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.Appointment;
import rsis.model.AppUser;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Pasien;
import rsis.repository.UserRepository;
import rsis.repository.PasienRepository;
import rsis.service.AppointmentService;
import rsis.service.NotifikasiService;
import rsis.service.PasienService;

import java.util.List;

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
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get pasien data
        Pasien pasien = pasienRepository.findByEmail(appUser.getEmail()).orElse(null);
        if (pasien != null) {
            model.addAttribute("nama", appUser.getNama());
            model.addAttribute("role", appUser.getRole());
            model.addAttribute("nomorRekamMedis", pasien.getNomorRekamMedis());
            model.addAttribute("pasienId", pasien.getIdPasien());
        } else {
            model.addAttribute("nama", appUser.getNama());
            model.addAttribute("role", appUser.getRole());
        }

        // Set active menu
        model.addAttribute("activeMenu", "dashboard");

        // Get statistics
        String pasienId = pasien != null ? pasien.getIdPasien() : appUser.getIdUser();
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
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pasien pasien = pasienRepository.findByEmail(appUser.getEmail()).orElse(null);

        model.addAttribute("nama", appUser.getNama());
        model.addAttribute("email", appUser.getEmail());
        model.addAttribute("role", appUser.getRole());
        model.addAttribute("activeMenu", "profil");

        // Get notifications
        String userId = pasien != null ? pasien.getIdPasien() : appUser.getIdUser();
        addNotifikasiToModel(userId, model);

        if (pasien != null) {
            model.addAttribute("idPasien", pasien.getIdPasien());
            model.addAttribute("nomorRekamMedis", pasien.getNomorRekamMedis());
            model.addAttribute("nomorHp", pasien.getNomorHp());
            model.addAttribute("tanggalLahir", pasien.getTanggalLahir());
            model.addAttribute("alamat", pasien.getAlamat());
        }

        return "pasien/profil";
    }

    @PostMapping("/profil")
    public String updateProfile(@RequestParam String nomorRekamMedis,
            @RequestParam String tanggalLahir,
            @RequestParam String alamat,
            @RequestParam String nomorHp,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Pasien pasien = pasienRepository.findByEmail(appUser.getEmail())
                    .orElseThrow(() -> new RuntimeException("Pasien not found"));
            pasienService.updateProfil(pasien.getIdPasien(), nomorRekamMedis, tanggalLahir, alamat, nomorHp);
            redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui!");
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
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pasien pasien = pasienRepository.findByEmail(appUser.getEmail()).orElse(null);

        // Add navbar attributes
        model.addAttribute("nama", appUser.getNama());
        model.addAttribute("role", appUser.getRole());
        model.addAttribute("activeMenu", "cari-dokter");

        // Get notifications
        String userId = pasien != null ? pasien.getIdPasien() : appUser.getIdUser();
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
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("nama", appUser.getNama());
        model.addAttribute("role", appUser.getRole());
        model.addAttribute("activeMenu", "jadwal");

        // Get notifications
        addNotifikasiToModel(appUser.getIdUser(), model);

        // Get doctor schedule
        List<JadwalPraktik> jadwals = pasienService.lihatJadwalDokter(dokterId);
        model.addAttribute("jadwals", jadwals);
        model.addAttribute("dokterId", dokterId);
        return "pasien/jadwal-dokter";
    }

    @GetMapping("/jadwal-riwayat")
    public String jadwalRiwayat(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pasien pasien = pasienRepository.findByEmail(appUser.getEmail()).orElse(null);

        model.addAttribute("nama", appUser.getNama());
        model.addAttribute("role", appUser.getRole());
        model.addAttribute("activeMenu", "jadwal");

        // Get notifications
        String userId = pasien != null ? pasien.getIdPasien() : appUser.getIdUser();
        addNotifikasiToModel(userId, model);

        if (pasien != null) {
            model.addAttribute("nomorRekamMedis", pasien.getNomorRekamMedis());
        }

        // Get all appointments for the pasien
        String pasienId = pasien != null ? pasien.getIdPasien() : appUser.getIdUser();
        List<Appointment> appointments = appointmentService.getAppointmentsByPasienId(pasienId);
        model.addAttribute("appointments", appointments);

        return "pasien/jadwal-riwayat";
    }
}
