package rsis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/kelola-dokter")
    public String kelolaDokter() {
        return "admin/kelola-dokter";
    }

    @GetMapping("/kelola-poli")
    public String kelolaPoli() {
        return "admin/kelola-poli";
    }

    @GetMapping("/kelola-jadwal")
    public String kelolaJadwal() {
        return "admin/kelola-jadwal";
    }

    @GetMapping("/laporan-bulanan")
    public String laporanBulanan() {
        return "admin/laporan-bulanan";
    }
}
