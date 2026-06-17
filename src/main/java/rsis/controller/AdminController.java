package rsis.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.AdminRS;
import rsis.model.User;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Poli;
import rsis.model.Spesialisasi;
import rsis.repository.AdminRSRepository;
import rsis.repository.UserRepository;
import rsis.service.AdminRSService;
import rsis.service.NotifikasiService;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminRSService adminRSService;

    @Autowired
    private NotifikasiService notifikasiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRSRepository adminRSRepository;

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

        AdminRS admin = adminRSRepository.findByIdUser(user.getIdUser()).orElse(null);
        // Always set basic user attributes
        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "dashboard");

        if (admin != null) {
            // Get notifications
            addNotifikasiToModel(admin.getIdUser(), model);
        } else {
            // Ensure notifikasi is always set even if admin is null
            model.addAttribute("notifikasi", List.of());
        }
        model.addAttribute("totalPasienHariIni",
                safeDashboardValue("total pasien hari ini", adminRSService::getTotalPasienHariIni, 0L));
        model.addAttribute("totalPasienBulanIni",
                safeDashboardValue("total pasien bulan ini", adminRSService::getTotalPasienBulanIni, 0L));
        model.addAttribute("dokterTersibuk",
                safeDashboardValue("dokter tersibuk", adminRSService::getDokterTersibuk, "N/A"));
        model.addAttribute("pasienPerHari",
                safeDashboardValue("pasien per hari", adminRSService::getPasienPerHari, Collections.emptyMap()));
        model.addAttribute("totalDokter",
                safeDashboardValue("total dokter", adminRSService::getTotalDokter, 0L));
        model.addAttribute("totalPoli",
                safeDashboardValue("total poli", adminRSService::getTotalPoli, 0L));
        model.addAttribute("activeMenu", "dashboard");
        return "admin/dashboard";
    }

    private <T> T safeDashboardValue(String label, Supplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (RuntimeException ex) {
            log.warn("Gagal mengambil statistik dashboard admin: {}", label, ex);
            return fallback;
        }
    }

    // Dokter Management
    @GetMapping("/kelola-dokter")
    public String kelolaDokter(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AdminRS admin = adminRSRepository.findByIdUser(user.getIdUser()).orElse(null);
        // Always set basic user attributes
        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "kelola-dokter");

        if (admin != null) {
            // Get notifications
            addNotifikasiToModel(admin.getIdUser(), model);
        } else {
            // Ensure notifikasi is always set even if admin is null
            model.addAttribute("notifikasi", List.of());
        }
        List<Dokter> dokters = adminRSService.getAllDokter();
        List<Spesialisasi> spesialisasis = adminRSService.getAllSpesialisasi();
        List<Poli> polis = adminRSService.getAllPoli();
        model.addAttribute("dokters", dokters);
        model.addAttribute("spesialisasis", spesialisasis);
        model.addAttribute("polis", polis);
        model.addAttribute("activeMenu", "kelola-dokter");
        return "admin/kelola-dokter";
    }

    @PostMapping("/dokter/create")
    public String createDokter(
            @RequestParam String nama,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String nomorHp,
            @RequestParam String spesialisasi,
            @RequestParam String poli,
            @RequestParam(required = false) MultipartFile dokterImage,
            RedirectAttributes redirectAttributes) {
        try {
            adminRSService.createDokter(nama, email, password, nomorHp, spesialisasi, poli, dokterImage);
            redirectAttributes.addFlashAttribute("success", "Dokter berhasil ditambahkan!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-dokter";
    }

    @PostMapping("/dokter/update")
    public String updateDokter(@ModelAttribute Dokter dokter,
            RedirectAttributes redirectAttributes) {
        try {
            adminRSService.updateDokter(dokter);
            redirectAttributes.addFlashAttribute("success", "Dokter berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-dokter";
    }

    @PostMapping("/dokter/delete/{id}")
    public String deleteDokter(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            adminRSService.softDeleteDokter(id);
            redirectAttributes.addFlashAttribute("success", "Dokter berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-dokter";
    }

    // Poli Management
    @GetMapping("/kelola-poli")
    public String kelolaPoli(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AdminRS admin = adminRSRepository.findByIdUser(user.getIdUser()).orElse(null);
        // Always set basic user attributes
        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "kelola-poli");

        if (admin != null) {
            // Get notifications
            addNotifikasiToModel(admin.getIdUser(), model);
        } else {
            // Ensure notifikasi is always set even if admin is null
            model.addAttribute("notifikasi", List.of());
        }
        List<Poli> polis = adminRSService.getAllPoli();
        model.addAttribute("polis", polis);
        model.addAttribute("activeMenu", "kelola-poli");
        return "admin/kelola-poli";
    }

    @PostMapping("/poli/create")
    public String createPoli(@ModelAttribute Poli poli,
            RedirectAttributes redirectAttributes) {
        try {
            adminRSService.createPoli(poli);
            redirectAttributes.addFlashAttribute("success", "Poli berhasil ditambahkan!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-poli";
    }

    @PostMapping("/poli/update")
    public String updatePoli(@ModelAttribute Poli poli,
            RedirectAttributes redirectAttributes) {
        try {
            adminRSService.updatePoli(poli);
            redirectAttributes.addFlashAttribute("success", "Poli berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-poli";
    }

    @PostMapping("/poli/delete/{id}")
    public String deletePoli(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            adminRSService.deletePoli(id);
            redirectAttributes.addFlashAttribute("success", "Poli berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-poli";
    }

    // Spesialisasi Management
    @GetMapping("/kelola-spesialisasi")
    public String kelolaSpesialisasi(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AdminRS admin = adminRSRepository.findByIdUser(user.getIdUser()).orElse(null);
        // Always set basic user attributes
        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "kelola-spesialisasi");

        if (admin != null) {
            // Get notifications
            addNotifikasiToModel(admin.getIdUser(), model);
        } else {
            // Ensure notifikasi is always set even if admin is null
            model.addAttribute("notifikasi", List.of());
        }
        List<Spesialisasi> spesialisasis = adminRSService.getAllSpesialisasi();
        model.addAttribute("spesialisasis", spesialisasis);
        model.addAttribute("activeMenu", "kelola-spesialisasi");
        return "admin/kelola-spesialisasi";
    }

    @PostMapping("/spesialisasi/create")
    public String createSpesialisasi(@ModelAttribute Spesialisasi spesialisasi,
            RedirectAttributes redirectAttributes) {
        try {
            adminRSService.createSpesialisasi(spesialisasi);
            redirectAttributes.addFlashAttribute("success", "Spesialisasi berhasil ditambahkan!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-spesialisasi";
    }

    @PostMapping("/spesialisasi/delete/{id}")
    public String deleteSpesialisasi(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            adminRSService.deleteSpesialisasi(id);
            redirectAttributes.addFlashAttribute("success", "Spesialisasi berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-spesialisasi";
    }

    // Jadwal Management
    @GetMapping("/kelola-jadwal")
    public String kelolaJadwal(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AdminRS admin = adminRSRepository.findByIdUser(user.getIdUser()).orElse(null);
        // Always set basic user attributes
        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "kelola-jadwal");

        if (admin != null) {
            // Get notifications
            addNotifikasiToModel(admin.getIdUser(), model);
        } else {
            // Ensure notifikasi is always set even if admin is null
            model.addAttribute("notifikasi", List.of());
        }
        List<JadwalPraktik> jadwals = adminRSService.getAllJadwal();
        List<Dokter> dokters = adminRSService.getAllDokter();
        model.addAttribute("jadwals", jadwals);
        model.addAttribute("dokters", dokters);
        model.addAttribute("activeMenu", "kelola-jadwal");
        return "admin/kelola-jadwal";
    }

    // Laporan
    @GetMapping("/laporan-bulanan")
    public String laporanBulanan(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AdminRS admin = adminRSRepository.findByIdUser(user.getIdUser()).orElse(null);
        // Always set basic user attributes
        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "laporan");

        if (admin != null) {
            // Get notifications
            addNotifikasiToModel(admin.getIdUser(), model);
        } else {
            // Ensure notifikasi is always set even if admin is null
            model.addAttribute("notifikasi", List.of());
        }

        // This will be handled by LaporanController
        return "redirect:/laporan";
    }

    @GetMapping("/profil")
    public String profil(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AdminRS admin = adminRSRepository.findByIdUser(user.getIdUser()).orElse(null);
        // Always set basic user attributes
        model.addAttribute("nama", user.getNama());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "profil");
        model.addAttribute("idUser", user.getIdUser());
        model.addAttribute("nomorHp", user.getNomorHp() != null ? user.getNomorHp() : "");
        model.addAttribute("alamat", ""); // AdminRS doesn't have alamat field
        model.addAttribute("jabatan", admin != null && admin.getJabatan() != null ? admin.getJabatan() : "");

        if (admin != null) {
            // Get notifications
            addNotifikasiToModel(admin.getIdUser(), model);
        } else {
            // Ensure notifikasi is always set even if admin is null
            model.addAttribute("notifikasi", List.of());
        }

        return "admin/profil";
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
        return "redirect:/admin/profil";
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
                return "redirect:/admin/profil";
            }

            // Validate new password length
            if (passwordBaru.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "Kata sandi baru minimal 8 karakter");
                return "redirect:/admin/profil";
            }

            // Validate password confirmation
            if (!passwordBaru.equals(konfirmasiPassword)) {
                redirectAttributes.addFlashAttribute("error",
                        "Konfirmasi kata sandi baru harus sama dengan kata sandi baru");
                return "redirect:/admin/profil";
            }

            // Update password
            user.setPassword(passwordEncoder.encode(passwordBaru));
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("success", "Kata sandi berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/profil";
    }
}
