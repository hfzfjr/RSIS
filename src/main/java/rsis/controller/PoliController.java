package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.AdminRS;
import rsis.model.Dokter;
import rsis.model.Poli;
import rsis.model.User;
import rsis.repository.AdminRSRepository;
import rsis.service.DokterService;
import rsis.service.PoliService;
import rsis.service.NotifikasiService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class PoliController {

    @Autowired
    private PoliService poliService;

    @Autowired
    private DokterService dokterService;

    @Autowired
    private AdminRSRepository adminRSRepository;

    @Autowired
    private NotifikasiService notifikasiService;

    private void addNotifikasiToModel(String userId, Model model) {
        try {
            var notifikasis = notifikasiService.getNotifikasiByPenerimaId(userId);
            model.addAttribute("notifikasi", notifikasis);
        } catch (Exception e) {
            model.addAttribute("notifikasi", List.of());
        }
    }

    // Poli Management
    @GetMapping("/kelola-poli")
    public String kelolaPoli(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        AdminRS admin = adminRSRepository.findByIdUser(user.getIdUser()).orElse(null);
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "kelola-poli");

        if (admin != null) {
            // Get notifications
            addNotifikasiToModel(admin.getIdUser(), model);
        } else {
            // Ensure notifikasi is always set even if admin is null
            model.addAttribute("notifikasi", List.of());
        }
        List<Poli> polis = poliService.getAllPoli();
        model.addAttribute("polis", polis);
        model.addAttribute("activeMenu", "kelola-poli");
        return "admin/kelola-poli";
    }

    @PostMapping("/poli/create")
    public String createPoli(@ModelAttribute Poli poli,
            @RequestParam(value = "dokterIds", required = false) List<String> dokterIds,
            RedirectAttributes redirectAttributes) {
        try {
            poliService.createPoli(poli, dokterIds);
            redirectAttributes.addFlashAttribute("success", "Poli berhasil ditambahkan!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-poli";
    }

    @GetMapping("/poli/unassigned-doctors")
    @ResponseBody
    public List<Dokter> getUnassignedDoctors() {
        try {
            return poliService.getDokterTanpaPoli();
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/poli/all-doctors")
    @ResponseBody
    public List<Dokter> getAllDoctors() {
        try {
            return dokterService.getAllDokter();
        } catch (Exception e) {
            return List.of();
        }
    }

    @PostMapping("/poli/update")
    public String updatePoli(@ModelAttribute Poli poli,
            @RequestParam(value = "dokterIds", required = false) List<String> dokterIds,
            RedirectAttributes redirectAttributes) {
        try {
            poliService.updatePoli(poli, dokterIds);
            redirectAttributes.addFlashAttribute("success", "Poli berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-poli";
    }

    @GetMapping("/poli/detail/{id}")
    @ResponseBody
    public java.util.Map<String, Object> getPoliDetail(@PathVariable String id) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            Poli poli = poliService.getPoliById(id)
                    .orElseThrow(() -> new RuntimeException("Poli tidak ditemukan"));
            List<Dokter> assignedDoctors = poliService.getDokterByPoli(id);
            List<Dokter> unassignedDoctors = poliService.getDokterTanpaPoli();

            response.put("success", true);
            response.put("poli", poli);
            response.put("assignedDoctors", assignedDoctors);
            response.put("unassignedDoctors", unassignedDoctors);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/poli/delete/{id}")
    public String deletePoli(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            poliService.deletePoli(id);
            redirectAttributes.addFlashAttribute("success", "Poli berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kelola-poli";
    }
}
