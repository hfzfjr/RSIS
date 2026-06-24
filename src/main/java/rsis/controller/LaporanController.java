package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rsis.model.User;
import rsis.repository.UserRepository;
import rsis.service.AdminRSService;
import rsis.service.LaporanBulananService;

import java.time.LocalDate;

@Controller
@RequestMapping("/laporan")
public class LaporanController {

    @Autowired
    private LaporanBulananService laporanBulananService;

    @Autowired
    private AdminRSService adminRSService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String laporanIndex(Model model) {
        LocalDate today = LocalDate.now();
        int bulan = today.getMonthValue();
        int tahun = today.getYear();

        model.addAttribute("totalPasienBulanIni", adminRSService.getTotalPasienBulanIni(bulan, tahun));
        model.addAttribute("totalDokter", adminRSService.getTotalDokter());
        model.addAttribute("totalPoli", adminRSService.getTotalPoli());
        model.addAttribute("bulan", bulan);
        model.addAttribute("tahun", tahun);
        return "laporan/index";
    }

    @GetMapping("/bulanan")
    public String laporanBulanan(@RequestParam(defaultValue = "#{T(java.time.LocalDate).now().monthValue}") int bulan,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().year}") int tahun,
            Model model) {
        model.addAttribute("totalPasienBulanIni", adminRSService.getTotalPasienBulanIni(bulan, tahun));
        model.addAttribute("totalDokter", adminRSService.getTotalDokter());
        model.addAttribute("totalPoli", adminRSService.getTotalPoli());
        model.addAttribute("bulan", bulan);
        model.addAttribute("tahun", tahun);
        return "laporan/bulanan";
    }

    @GetMapping("/admin")
    public String laporanBulananAdmin(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Always set basic user attributes
        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "laporan");

        // This will be handled by redirecting to the main laporan page
        return "redirect:/laporan";
    }

    @GetMapping("/export/pdf")
    @SuppressWarnings("null")
    public ResponseEntity<byte[]> exportPDF(@RequestParam int bulan,
            @RequestParam int tahun) {
        try {
            byte[] pdfBytes = laporanBulananService.generatePDF(bulan, tahun);

            String filename = String.format("laporan-bulanan-%d-%d.pdf", bulan, tahun);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCSV(@RequestParam int bulan,
            @RequestParam int tahun) {
        try {
            byte[] csvBytes = laporanBulananService.generateCSV(bulan, tahun);

            String filename = String.format("laporan-bulanan-%d-%d.csv", bulan, tahun);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csvBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bulanan/data")
    @ResponseBody
    public java.util.Map<String, Object> getLaporanBulananData(
            @RequestParam int bulan,
            @RequestParam int tahun) {
        try {
            return laporanBulananService.getLaporanBulanan(bulan, tahun);
        } catch (Exception e) {
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("totalPasien", 0);
            error.put("totalAppointment", 0);
            error.put("labelPeriode", "");
            error.put("visitStats", java.util.Collections.emptyList());
            error.put("busiestDoctors", java.util.Collections.emptyList());
            return error;
        }
    }
}
