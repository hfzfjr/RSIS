package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.Appointment;
import rsis.model.User;
import rsis.model.Dokter;
import rsis.model.Pasien;
import rsis.service.AppointmentService;
import rsis.service.UserService;
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
    private UserService userService;

    @Autowired
    private rsis.service.DokterService dokterService;

    private void addNotifikasiToModel(String userId, Model model) {
        try {
            var notifikasis = notifikasiService.getNotifikasiByPenerimaId(userId);
            model.addAttribute("notifikasi", notifikasis);
        } catch (Exception e) {
            model.addAttribute("notifikasi", List.of());
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Get pasien data
        Pasien pasien = pasienService.getPasienByIdUser(user.getIdUser()).orElse(null);
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

        // Auto-update status for past booking dates
        appointmentService.updateExpiredAppointments();

        // Get upcoming appointments (filter DIKONFIRMASI only, sort by jadwal.tanggal
        // ascending (closest to today first), limit to 2)
        List<Appointment> upcomingAppointments = appointments.stream()
                .filter(a -> "DIKONFIRMASI".equals(a.getStatus()))
                .sorted((a1, a2) -> {
                    if (a1.getJadwal() == null || a1.getJadwal().getTanggal() == null)
                        return 1;
                    if (a2.getJadwal() == null || a2.getJadwal().getTanggal() == null)
                        return -1;
                    return a1.getJadwal().getTanggal().compareTo(a2.getJadwal().getTanggal());
                })
                .limit(2)
                .toList();

        // Populate transient fields for dokter in each upcoming appointment
        for (Appointment appointment : upcomingAppointments) {
            if (appointment.getJadwal() != null && appointment.getJadwal().getDokter() != null) {
                dokterService.enrichWithUserData(appointment.getJadwal().getDokter());
            }
        }

        model.addAttribute("appointments", upcomingAppointments);

        return "pasien/dashboard";
    }

    @GetMapping("/profil")
    public String profil(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Pasien pasien = pasienService.getPasienByIdUser(user.getIdUser()).orElse(null);

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
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            User user = (User) model.getAttribute("currentUser");
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            Pasien pasien = pasienService.getPasienByIdUser(user.getIdUser())
                    .orElseThrow(() -> new RuntimeException("Pasien not found"));

            // Update nama and nomorHp in user
            user.setNama(namaLengkap);
            user.setNomorHp(nomorTelepon);
            userService.saveUser(user);

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
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            User user = (User) model.getAttribute("currentUser");
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            userService.changePassword(user.getIdUser(), passwordLama, passwordBaru, konfirmasiPassword);
            redirectAttributes.addFlashAttribute("success", "Kata sandi berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pasien/profil";
    }

    @GetMapping("/cari-dokter")
    public String searchDoctors(@RequestParam(required = false) String keyword,
            Model model) {
        // Get user and pasien data
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Pasien pasien = pasienService.getPasienByIdUser(user.getIdUser()).orElse(null);

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

}
