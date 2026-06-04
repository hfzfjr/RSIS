package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.dto.JadwalDTO;
import rsis.model.JadwalPraktik;
import rsis.service.JadwalPraktikService;

import java.util.List;

@Controller
@RequestMapping("/jadwal")
public class JadwalController {

    @Autowired
    private JadwalPraktikService jadwalPraktikService;

    @GetMapping("/list")
    public String listJadwal(Model model) {
        List<JadwalPraktik> jadwals = jadwalPraktikService.getAllJadwal();
        model.addAttribute("jadwals", jadwals);
        return "jadwal/list";
    }

    @GetMapping("/available")
    public String availableJadwal(Model model) {
        List<JadwalDTO> jadwals = jadwalPraktikService.getAvailableJadwalDTOs();
        model.addAttribute("jadwals", jadwals);
        return "jadwal/available";
    }

    @GetMapping("/dokter/{dokterId}")
    public String jadwalByDokter(@PathVariable String dokterId, Model model) {
        List<JadwalDTO> jadwals = jadwalPraktikService.getJadwalDTOsByDokterId(dokterId);
        model.addAttribute("jadwals", jadwals);
        model.addAttribute("dokterId", dokterId);
        return "jadwal/dokter";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("jadwal", new JadwalPraktik());
        return "jadwal/create";
    }

    @PostMapping("/create")
    public String createJadwal(@ModelAttribute JadwalPraktik jadwal,
            RedirectAttributes redirectAttributes) {
        try {
            jadwalPraktikService.createJadwal(jadwal);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil dibuat!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jadwal/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        JadwalPraktik jadwal = jadwalPraktikService.getJadwalById(id).orElse(null);
        model.addAttribute("jadwal", jadwal);
        return "jadwal/edit";
    }

    @PostMapping("/update")
    public String updateJadwal(@ModelAttribute JadwalPraktik jadwal,
            RedirectAttributes redirectAttributes) {
        try {
            jadwalPraktikService.updateJadwal(jadwal);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jadwal/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteJadwal(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            jadwalPraktikService.deleteJadwal(id);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jadwal/list";
    }
}
