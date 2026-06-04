package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.Appointment;
import rsis.model.JadwalPraktik;
import rsis.service.DokterService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/dokter")
public class DokterController {

    @Autowired
    private DokterService dokterService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String dokterId = principal.getName();
        model.addAttribute("dokterId", dokterId);
        return "dokter/dashboard";
    }

    @GetMapping("/jadwal")
    public String jadwal(Model model, Principal principal) {
        String dokterId = principal.getName();
        List<JadwalPraktik> jadwals = dokterService.getJadwalByDokterId(dokterId);
        model.addAttribute("jadwals", jadwals);
        return "dokter/jadwal";
    }

    @PostMapping("/jadwal/create")
    public String createJadwal(@ModelAttribute JadwalPraktik jadwal,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            // Dokter will be set in service layer based on logged-in user
            dokterService.createJadwal(jadwal);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil dibuat!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/jadwal";
    }

    @PostMapping("/jadwal/update")
    public String updateJadwal(@ModelAttribute JadwalPraktik jadwal,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.updateJadwal(jadwal);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/jadwal";
    }

    @PostMapping("/jadwal/delete/{id}")
    public String deleteJadwal(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.deleteJadwal(id);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/jadwal";
    }

    @GetMapping("/daftar-pasien")
    public String daftarPasien(Model model, Principal principal) {
        String dokterId = principal.getName();
        List<Appointment> appointments = dokterService.getDaftarPasien(dokterId);
        model.addAttribute("appointments", appointments);
        return "dokter/daftar-pasien";
    }

    @GetMapping("/appointment/pending")
    public String pendingAppointments(Model model, Principal principal) {
        String dokterId = principal.getName();
        List<Appointment> appointments = dokterService.getPendingAppointments(dokterId);
        model.addAttribute("appointments", appointments);
        return "dokter/appointment-pending";
    }

    @PostMapping("/appointment/konfirmasi/{id}")
    public String konfirmasiAppointment(@PathVariable String id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.konfirmasiAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Appointment berhasil dikonfirmasi!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/appointment/pending";
    }

    @PostMapping("/appointment/tolak/{id}")
    public String tolakAppointment(@PathVariable String id,
            @RequestParam String alasan,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.tolakAppointment(id, alasan);
            redirectAttributes.addFlashAttribute("success", "Appointment berhasil ditolak!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/appointment/pending";
    }
}
