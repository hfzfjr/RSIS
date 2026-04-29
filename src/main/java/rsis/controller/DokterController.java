package rsis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dokter")
public class DokterController {
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dokter/dashboard";
    }

    @GetMapping("/jadwal")
    public String jadwal() {
        return "dokter/jadwal";
    }

    @GetMapping("/daftar-pasien")
    public String daftarPasien() {
        return "dokter/daftar-pasien";
    }

    @GetMapping("/appointment")
    public String appointment() {
        return "dokter/appointment";
    }
}
