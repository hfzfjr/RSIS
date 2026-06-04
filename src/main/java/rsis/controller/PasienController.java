package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Pasien;
import rsis.service.PasienService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/pasien")
public class PasienController {

    @Autowired
    private PasienService pasienService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String pasienId = principal.getName();
        model.addAttribute("pasienId", pasienId);
        return "pasien/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String pasienId = principal.getName();
        Pasien pasien = pasienService.getPasienById(pasienId).orElse(null);
        model.addAttribute("pasien", pasien);
        return "pasien/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String nomorRekamMedis,
            @RequestParam String tanggalLahir,
            @RequestParam String alamat,
            @RequestParam String nomorHp,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String pasienId = principal.getName();
            pasienService.updateProfil(pasienId, nomorRekamMedis, tanggalLahir, alamat, nomorHp);
            redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pasien/profile";
    }

    @GetMapping("/cari-dokter")
    public String searchDoctors(@RequestParam(required = false) String keyword, Model model) {
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
    public String showDoctorSchedule(@PathVariable String dokterId, Model model) {
        List<JadwalPraktik> jadwals = pasienService.lihatJadwalDokter(dokterId);
        model.addAttribute("jadwals", jadwals);
        model.addAttribute("dokterId", dokterId);
        return "pasien/jadwal-dokter";
    }
}
