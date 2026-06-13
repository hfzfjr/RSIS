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
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dokter dokter = dokterRepository.findByIdUser(user.getIdUser()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "dashboard");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);
        }

        String dokterId = dokter != null ? dokter.getIdUser() : "";
        model.addAttribute("dokterId", dokterId);
        return "dokter/dashboard";
    }

    @GetMapping("/jadwal-praktik")
    public String jadwalPraktik(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dokter dokter = dokterRepository.findByIdUser(user.getIdUser()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "jadwal-praktik");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);

            String dokterId = dokter.getIdUser();
            List<JadwalPraktik> jadwals = dokterService.getJadwalByDokterId(dokterId);
            model.addAttribute("jadwals", jadwals);
        }

        return "dokter/jadwal-praktik";
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
        return "redirect:/dokter/jadwal-praktik";
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
        return "redirect:/dokter/jadwal-praktik";
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
        return "redirect:/dokter/jadwal-praktik";
    }

    @GetMapping("/daftar-pasien")
    public String daftarPasien(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dokter dokter = dokterRepository.findByIdUser(user.getIdUser()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "jadwal");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);

            String dokterId = dokter.getIdUser();
            List<Appointment> appointments = dokterService.getDaftarPasien(dokterId);
            model.addAttribute("appointments", appointments);
        }

        return "dokter/daftar-pasien";
    }

    @GetMapping("/appointment")
    public String appointment(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dokter dokter = dokterRepository.findByIdUser(user.getIdUser()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "appointment");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);

            String dokterId = dokter.getIdUser();
            List<Appointment> appointments = dokterService.getPendingAppointments(dokterId);
            model.addAttribute("appointments", appointments);
        }

        return "dokter/appointment";
    }

    @GetMapping("/profil")
    public String profil(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dokter dokter = dokterRepository.findByIdUser(user.getIdUser()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "profil");
            model.addAttribute("idUser", dokter.getIdUser());
            model.addAttribute("nomorHp", user.getNomorHp() != null ? user.getNomorHp() : "");
            model.addAttribute("alamat", ""); // Dokter doesn't have alamat field
            model.addAttribute("spesialisasi",
                    dokter.getSpesialisasi() != null ? dokter.getSpesialisasi().getNama() : "");
            model.addAttribute("poli", dokter.getPoli() != null ? dokter.getPoli().getNamaPoli() : "");
            model.addAttribute("nomorStr", dokter.getNomorStr() != null ? dokter.getNomorStr() : "");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);

            // Get statistics for dokter
            String dokterId = dokter.getIdUser();
            List<JadwalPraktik> jadwals = dokterService.getJadwalByDokterId(dokterId);
            model.addAttribute("totalJadwal", jadwals.size());

            List<Appointment> appointments = dokterService.getDaftarPasien(dokterId);
            model.addAttribute("totalPasien", appointments.size());
        } else {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "profil");
            model.addAttribute("idUser", user.getIdUser());
            model.addAttribute("nomorHp", "");
            model.addAttribute("alamat", "");
            model.addAttribute("spesialisasi", "");
            model.addAttribute("poli", "");
            model.addAttribute("nomorStr", "");
            model.addAttribute("totalJadwal", 0);
            model.addAttribute("totalPasien", 0);
        }

        return "dokter/profil";
    }

    @PostMapping("/profil")
    public String updateProfile(@RequestParam String namaLengkap,
            @RequestParam String nomorTelepon,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update nama and nomorHp in user
            user.setNama(namaLengkap);
            user.setNomorHp(nomorTelepon);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/profil";
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
                return "redirect:/dokter/profil";
            }

            // Validate new password length
            if (passwordBaru.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "Kata sandi baru minimal 8 karakter");
                return "redirect:/dokter/profil";
            }

            // Validate password confirmation
            if (!passwordBaru.equals(konfirmasiPassword)) {
                redirectAttributes.addFlashAttribute("error",
                        "Konfirmasi kata sandi baru harus sama dengan kata sandi baru");
                return "redirect:/dokter/profil";
            }

            // Update password
            user.setPassword(passwordEncoder.encode(passwordBaru));
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Kata sandi berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/profil";
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
        return "redirect:/dokter/appointment";
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
        return "redirect:/dokter/appointment";
    }
}
