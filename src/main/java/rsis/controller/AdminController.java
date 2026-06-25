package rsis.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import rsis.service.DokterService;
import rsis.service.PoliService;
import rsis.service.JadwalPraktikService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminRSService adminRSService;

    @Autowired
    private DokterService dokterService;

    @Autowired
    private PoliService poliService;

    @Autowired
    private JadwalPraktikService jadwalPraktikService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRSRepository adminRSRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void addJadwalStatsToModel(Model model) {
        try {
            Map<String, Long> jadwalStats = adminRSService.getJadwalStatistics();
            model.addAttribute("jadwalAktif", jadwalStats.getOrDefault("tersedia", 0L));
            model.addAttribute("jadwalPenuh", jadwalStats.getOrDefault("penuh", 0L));
            model.addAttribute("jadwalLibur", jadwalStats.getOrDefault("libur", 0L));
        } catch (Exception e) {
            model.addAttribute("jadwalAktif", 0);
            model.addAttribute("jadwalPenuh", 0);
            model.addAttribute("jadwalLibur", 0);
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "dashboard");

        model.addAttribute("totalPasienHariIni", adminRSService.getTotalPasienHariIni());
        model.addAttribute("totalPasienBulanIni", adminRSService.getTotalPasienBulanIni());
        model.addAttribute("dokterTersibuk", adminRSService.getDokterTersibuk());
        model.addAttribute("pasienPerHari", adminRSService.getPasienPerHari());
        model.addAttribute("totalDokter", adminRSService.getTotalDokter());
        model.addAttribute("totalPoli", adminRSService.getTotalPoli());
        model.addAttribute("totalAppointmentHariIni", adminRSService.getTotalAppointmentHariIni());
        model.addAttribute("totalAppointmentBulanIni", adminRSService.getTotalAppointmentBulanIni());
        model.addAttribute("appointmentPending", adminRSService.getAppointmentPending());
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        try {
            model.addAttribute("visitStats", adminRSService.getStatsByMonthAndYear(currentMonth, currentYear));
        } catch (RuntimeException e) {
            model.addAttribute("visitStats", Collections.emptyList());
        }
        try {
            model.addAttribute("busiestDoctors", adminRSService.getBusiestDoctorsOfMonth(currentMonth, currentYear));
        } catch (RuntimeException e) {
            model.addAttribute("busiestDoctors", Collections.emptyList());
        }
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("activeMenu", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/dashboard/visit-stats")
    @ResponseBody
    public Map<String, Object> getVisitStats(
            @RequestParam int month,
            @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("visitStats", adminRSService.getStatsByMonthAndYear(month, targetYear));
            response.put("busiestDoctors", adminRSService.getBusiestDoctorsOfMonth(month, targetYear));
        } catch (Exception e) {
            log.error("Failed to fetch visit statistics", e);
            response.put("visitStats", Collections.emptyList());
            response.put("busiestDoctors", Collections.emptyList());
        }
        return response;
    }

    // Dokter Management
    @GetMapping("/kelola-dokter")
    public String kelolaDokter(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "kelola-dokter");

        List<Dokter> dokters = dokterService.getAllDokter();
        List<Spesialisasi> spesialisasis = dokterService.getAllSpesialisasi();
        List<Poli> polis = poliService.getAllPoli();
        model.addAttribute("dokters", dokters);
        model.addAttribute("spesialisasis", spesialisasis);
        model.addAttribute("polis", polis);

        long totalDokter = dokters.size();
        long scheduledDokter = adminRSService.getScheduledDoctorsCountByDate(LocalDate.now());
        long liburDokter = Math.max(0, totalDokter - scheduledDokter);

        model.addAttribute("dokterTerdaftar", totalDokter);
        model.addAttribute("dokterTerjadwal", scheduledDokter);
        model.addAttribute("dokterLibur", liburDokter);

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
            dokterService.createDokter(nama, email, password, nomorHp, spesialisasi, poli, dokterImage);
            redirectAttributes.addFlashAttribute("success", "Dokter berhasil ditambahkan!");
        } catch (RuntimeException | IOException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-dokter";
    }

    @PostMapping("/dokter/update")
    public String updateDokter(
            @RequestParam String idUser,
            @RequestParam String nama,
            @RequestParam(required = false) String nomorHp,
            @RequestParam String nomorStr,
            @RequestParam String spesialisasi,
            @RequestParam String poli,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.updateDokter(idUser, nama, nomorHp, nomorStr, spesialisasi, poli);
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
            dokterService.softDeleteDokter(id);
            redirectAttributes.addFlashAttribute("success", "Dokter berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-dokter";
    }

    // Spesialisasi Management
    @GetMapping("/kelola-spesialisasi")
    public String kelolaSpesialisasi(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "kelola-spesialisasi");

        List<Spesialisasi> spesialisasis = dokterService.getAllSpesialisasi();
        model.addAttribute("spesialisasis", spesialisasis);
        model.addAttribute("activeMenu", "kelola-spesialisasi");
        return "admin/kelola-spesialisasi";
    }

    @PostMapping("/spesialisasi/create")
    public String createSpesialisasi(@ModelAttribute Spesialisasi spesialisasi,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.createSpesialisasi(spesialisasi);
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
            dokterService.deleteSpesialisasi(id);
            redirectAttributes.addFlashAttribute("success", "Spesialisasi berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-spesialisasi";
    }

    // Jadwal Management
    @GetMapping("/kelola-jadwal")
    public String kelolaJadwal(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "kelola-jadwal");

        List<JadwalPraktik> jadwals = jadwalPraktikService.getAllJadwalWithEnrichedDokter();
        List<Dokter> dokters = dokterService.getAllDokter();
        List<Poli> polis = poliService.getAllPoli();
        model.addAttribute("jadwals", jadwals);
        model.addAttribute("dokters", dokters);
        model.addAttribute("polis", polis);
        addJadwalStatsToModel(model);
        model.addAttribute("activeMenu", "kelola-jadwal");
        return "admin/kelola-jadwal";
    }

    @PostMapping("/jadwal/update")
    public String updateJadwal(
            @RequestParam String idJadwal,
            @RequestParam String idUser,
            @RequestParam String hari,
            @RequestParam(required = false) String tanggal,
            @RequestParam String jamMulai,
            @RequestParam String jamSelesai,
            @RequestParam String statusKetersediaan,
            @RequestParam int kuota,
            @RequestParam String idPoli,
            RedirectAttributes redirectAttributes) {
        try {
            LocalDate localTanggal = (tanggal != null && !tanggal.isEmpty()) ? LocalDate.parse(tanggal) : null;
            LocalTime localJamMulai = LocalTime.parse(jamMulai);
            LocalTime localJamSelesai = LocalTime.parse(jamSelesai);

            jadwalPraktikService.updateJadwal(idJadwal, idUser, hari, localTanggal, localJamMulai, localJamSelesai,
                    statusKetersediaan, kuota, idPoli);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil diperbarui!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-jadwal";
    }

    @PostMapping("/jadwal/create")
    public String createJadwal(
            @RequestParam String idUser,
            @RequestParam String hari,
            @RequestParam(required = false) String tanggal,
            @RequestParam String jamMulai,
            @RequestParam String jamSelesai,
            @RequestParam String statusKetersediaan,
            @RequestParam int kuota,
            @RequestParam String idPoli,
            RedirectAttributes redirectAttributes) {
        try {
            LocalDate localTanggal = (tanggal != null && !tanggal.isEmpty()) ? LocalDate.parse(tanggal) : null;
            LocalTime localJamMulai = LocalTime.parse(jamMulai);
            LocalTime localJamSelesai = LocalTime.parse(jamSelesai);

            jadwalPraktikService.createJadwal(idUser, hari, localTanggal, localJamMulai, localJamSelesai,
                    statusKetersediaan,
                    kuota, idPoli);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil ditambahkan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-jadwal";
    }

    @PostMapping("/jadwal/create-bulk")
    public String createBulkJadwal(
            @RequestParam String idUser,
            @RequestParam String idPoli,
            @RequestParam String mode,
            @RequestParam(required = false) String hari,
            @RequestParam(required = false) String tanggal,
            @RequestParam(required = false) List<String> hariList,
            @RequestParam(required = false) String tanggalMulai,
            @RequestParam(required = false) String sampaiYearMonth,
            @RequestParam String jamMulai,
            @RequestParam String jamSelesai,
            @RequestParam String statusKetersediaan,
            @RequestParam int kuota,
            RedirectAttributes redirectAttributes) {
        try {
            LocalTime localJamMulai = LocalTime.parse(jamMulai);
            LocalTime localJamSelesai = LocalTime.parse(jamSelesai);

            if ("recurring".equals(mode)) {
                LocalDate localTanggalMulai = (tanggalMulai != null && !tanggalMulai.isEmpty())
                        ? LocalDate.parse(tanggalMulai)
                        : LocalDate.now();
                jadwalPraktikService.createBulkRecurringJadwal(
                        idUser, idPoli, hariList, localTanggalMulai, sampaiYearMonth,
                        localJamMulai, localJamSelesai, statusKetersediaan, kuota);
                redirectAttributes.addFlashAttribute("success", "Jadwal berulang berhasil dibuat!");
            } else {
                LocalDate localTanggal = (tanggal != null && !tanggal.isEmpty()) ? LocalDate.parse(tanggal) : null;
                jadwalPraktikService.createJadwal(idUser, hari, localTanggal, localJamMulai, localJamSelesai,
                        statusKetersediaan, kuota, idPoli);
                redirectAttributes.addFlashAttribute("success", "Jadwal berhasil ditambahkan!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-jadwal";
    }

    @PostMapping("/jadwal/delete/{id}")
    public String deleteJadwal(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            jadwalPraktikService.softDeleteJadwal(id);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-jadwal";
    }

    @GetMapping("/profil")
    public String profil(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        AdminRS admin = adminRSRepository.findByIdUser(user.getIdUser()).orElse(null);
        model.addAttribute("email", user.getEmail());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "profil");
        model.addAttribute("idUser", user.getIdUser());
        model.addAttribute("nomorHp", user.getNomorHp() != null ? user.getNomorHp() : "");
        model.addAttribute("alamat", ""); // AdminRS doesn't have alamat field
        model.addAttribute("jabatan", admin != null && admin.getJabatan() != null ? admin.getJabatan() : "");

        return "admin/profil";
    }

    @PostMapping("/profil")
    public String updateProfile(@RequestParam String namaLengkap,
            @RequestParam String nomorTelepon,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            User user = (User) model.getAttribute("currentUser");
            if (user == null) {
                throw new RuntimeException("User not found");
            }

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
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            User user = (User) model.getAttribute("currentUser");
            if (user == null) {
                throw new RuntimeException("User not found");
            }

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