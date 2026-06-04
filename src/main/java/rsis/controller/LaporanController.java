package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rsis.dto.StatistikDTO;
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

    @GetMapping
    public String laporanIndex(Model model) {
        LocalDate today = LocalDate.now();
        int bulan = today.getMonthValue();
        int tahun = today.getYear();

        StatistikDTO statistik = adminRSService.getStatistikBulanan(bulan, tahun);
        model.addAttribute("statistik", statistik);
        model.addAttribute("bulan", bulan);
        model.addAttribute("tahun", tahun);
        return "laporan/index";
    }

    @GetMapping("/bulanan")
    public String laporanBulanan(@RequestParam(defaultValue = "#{T(java.time.LocalDate).now().monthValue}") int bulan,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().year}") int tahun,
            Model model) {
        StatistikDTO statistik = adminRSService.getStatistikBulanan(bulan, tahun);
        model.addAttribute("statistik", statistik);
        model.addAttribute("bulan", bulan);
        model.addAttribute("tahun", tahun);
        return "laporan/bulanan";
    }

    @GetMapping("/export/pdf")
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
}
