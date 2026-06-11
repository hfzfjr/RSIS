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
import rsis.repository.DokterRepository;
import rsis.repository.UserRepository;
import rsis.service.DokterService;
import rsis.service.NotifikasiService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/dokter")
public class DokterController {

    @Autowired
    private DokterService dokterService;

    @Autowired
    private NotifikasiService notifikasiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DokterRepository dokterRepository;

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
        // Get user data for navbar
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dokter dokter = dokterRepository.findByEmail(appUser.getEmail()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", appUser.getNama());
            model.addAttribute("role", appUser.getRole());
            model.addAttribute("activeMenu", "dashboard");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);
        }

        String dokterId = dokter != null ? dokter.getIdDokter() : "";
        model.addAttribute("dokterId", dokterId);
        return "dokter/dashboard";
    }

    @GetMapping("/jadwal")
    public String jadwal(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dokter dokter = dokterRepository.findByEmail(appUser.getEmail()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", appUser.getNama());
            model.addAttribute("role", appUser.getRole());
            model.addAttribute("activeMenu", "jadwal");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);

            String dokterId = dokter.getIdDokter();
            List<JadwalPraktik> jadwals = dokterService.getJadwalByDokterId(dokterId);
            model.addAttribute("jadwals", jadwals);
        }

        return "dokter/jadwal";
    }

    @PostMapping("/jadwal/create")
    public String createJadwal(@ModelAttribute JadwalPraktik jadwal,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            // Dokter will be set in service layer based on logged-in user
            dokterService.createJadwal(jadwal);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil dibuat!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/jadwal";
    }

    @PostMapping("/jadwal/update")
    public String updateJadwal(@ModelAttribute JadwalPraktik jadwal,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.updateJadwal(jadwal);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/jadwal";
    }

    @PostMapping("/jadwal/delete/{id}")
    public String deleteJadwal(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.deleteJadwal(id);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/jadwal";
    }

    @GetMapping("/daftar-pasien")
    public String daftarPasien(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dokter dokter = dokterRepository.findByEmail(appUser.getEmail()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", appUser.getNama());
            model.addAttribute("role", appUser.getRole());
            model.addAttribute("activeMenu", "jadwal");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);

            String dokterId = dokter.getIdDokter();
            List<Appointment> appointments = dokterService.getDaftarPasien(dokterId);
            model.addAttribute("appointments", appointments);
        }

        return "dokter/daftar-pasien";
    }

    @GetMapping("/appointment/pending")
    public String pendingAppointments(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dokter dokter = dokterRepository.findByEmail(appUser.getEmail()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", appUser.getNama());
            model.addAttribute("role", appUser.getRole());
            model.addAttribute("activeMenu", "jadwal");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);

            String dokterId = dokter.getIdDokter();
            List<Appointment> appointments = dokterService.getPendingAppointments(dokterId);
            model.addAttribute("appointments", appointments);
        }

        return "dokter/appointment-pending";
    }

    @GetMapping("/profil")
    public String profil(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dokter dokter = dokterRepository.findByEmail(appUser.getEmail()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", appUser.getNama());
            model.addAttribute("role", appUser.getRole());
            model.addAttribute("activeMenu", "profil");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);
        }

        return "dokter/profil";
    }

    @PostMapping("/appointment/konfirmasi/{id}")
    public String konfirmasiAppointment(@PathVariable String id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.konfirmasiAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Appointment berhasil dikonfirmasi!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/appointment/pending";
    }

    @PostMapping("/appointment/tolak/{id}")
    public String tolakAppointment(@PathVariable String id,
            @RequestParam String alasan,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.tolakAppointment(id, alasan);
            redirectAttributes.addFlashAttribute("success", "Appointment berhasil ditolak!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/appointment/pending";
    }
}
