package rsis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/pasien")
public class PasienController {
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, Principal principal) {
        Object flash = session.getAttribute("FLASH_SUCCESS");
        if (flash != null) {
            model.addAttribute("successMessage", flash.toString());
            session.removeAttribute("FLASH_SUCCESS");
        }
        model.addAttribute("email", principal != null ? principal.getName() : null);
        return "pasien/dashboard";
    }

    @GetMapping("/cari-dokter")
    public String cariDokter() {
        return "pasien/cari-dokter";
    }

    @GetMapping("/jadwal-riwayat")
    public String jadwalRiwayat() {
        return "pasien/jadwal-riwayat";
    }

    @GetMapping("/booking")
    public String booking() {
        return "pasien/booking";
    }

    @GetMapping("/profil-informasi")
    public String profilInformasi() {
        return "pasien/profil-informasi";
    }

    @GetMapping("/profil-medis")
    public String profilMedis() {
        return "pasien/profil-medis";
    }

    @GetMapping("/profil-notifikasi")
    public String profilNotifikasi() {
        return "pasien/profil-notifikasi";
    }

    @GetMapping("/profil-keamanan")
    public String profilKeamanan() {
        return "pasien/profil-keamanan";
    }
}
