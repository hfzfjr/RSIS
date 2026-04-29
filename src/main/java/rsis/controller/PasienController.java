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

    @GetMapping("/jadwal-dokter")
    public String jadwalDokter() {
        return "pasien/jadwal-dokter";
    }

    @GetMapping("/booking")
    public String booking() {
        return "pasien/booking";
    }

    @GetMapping("/riwayat-appointment")
    public String riwayatAppointment() {
        return "pasien/riwayat-appointment";
    }
}
