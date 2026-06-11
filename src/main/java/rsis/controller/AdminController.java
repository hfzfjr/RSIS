package rsis.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Poli;
import rsis.model.Spesialisasi;
import rsis.service.AdminRSService;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminRSService adminRSService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
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
    public String kelolaDokter(Model model) {
        List<Dokter> dokters = adminRSService.getAllDokter();
        model.addAttribute("dokters", dokters);
        return "admin/kelola-dokter";
    }

    @PostMapping("/dokter/create")
    public String createDokter(@ModelAttribute Dokter dokter,
            RedirectAttributes redirectAttributes) {
        try {
            adminRSService.createDokter(dokter);
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
            adminRSService.deleteDokter(id);
            redirectAttributes.addFlashAttribute("success", "Dokter berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-dokter";
    }

    // Poli Management
    @GetMapping("/kelola-poli")
    public String kelolaPoli(Model model) {
        List<Poli> polis = adminRSService.getAllPoli();
        model.addAttribute("polis", polis);
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
    public String kelolaSpesialisasi(Model model) {
        List<Spesialisasi> spesialisasis = adminRSService.getAllSpesialisasi();
        model.addAttribute("spesialisasis", spesialisasis);
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
    public String kelolaJadwal(Model model) {
        List<JadwalPraktik> jadwals = adminRSService.getAllJadwal();
        List<Dokter> dokters = adminRSService.getAllDokter();
        model.addAttribute("jadwals", jadwals);
        model.addAttribute("dokters", dokters);
        return "admin/kelola-jadwal";
    }

    // Laporan
    @GetMapping("/laporan-bulanan")
    public String laporanBulanan(Model model) {
        // This will be handled by LaporanController
        return "redirect:/laporan";
    }
}
