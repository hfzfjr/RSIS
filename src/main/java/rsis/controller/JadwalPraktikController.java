package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.Appointment;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Pasien;
import rsis.model.User;
import rsis.repository.DokterRepository;
import rsis.repository.PasienRepository;
import rsis.repository.UserRepository;
import rsis.service.AppointmentService;
import rsis.service.DokterService;
import rsis.service.JadwalPraktikService;
import rsis.service.NotifikasiService;
import rsis.service.PasienService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/jadwal")
public class JadwalPraktikController {

    @Autowired
    private PasienService pasienService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DokterService dokterService;

    @Autowired
    private JadwalPraktikService jadwalPraktikService;

    @Autowired
    private NotifikasiService notifikasiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasienRepository pasienRepository;

    @Autowired
    private DokterRepository dokterRepository;

    private void addNotifikasiToModel(String userId, Model model) {
        try {
            var notifikasis = notifikasiService.getNotifikasiByPenerimaId(userId);
            model.addAttribute("notifikasi", notifikasis);
        } catch (Exception e) {
            model.addAttribute("notifikasi", List.of());
        }
    }

    // Pasien jadwal endpoints
    @GetMapping("/dokter/{dokterId}")
    public String showDoctorSchedule(@PathVariable String dokterId,
            @AuthenticationPrincipal UserDetails principal,
            Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "jadwal");

        // Get notifications
        addNotifikasiToModel(user.getIdUser(), model);

        // Get doctor schedule
        List<JadwalPraktik> jadwals = pasienService.lihatJadwalDokter(dokterId);
        model.addAttribute("jadwals", jadwals);
        model.addAttribute("dokterId", dokterId);
        return "pasien/jadwal-dokter";
    }

    @GetMapping("/riwayat")
    public String jadwalRiwayat(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pasien pasien = pasienRepository.findByIdUser(user.getIdUser()).orElse(null);

        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "jadwal");

        // Get notifications
        String userId = pasien != null ? pasien.getIdUser() : user.getIdUser();
        addNotifikasiToModel(userId, model);

        if (pasien != null) {
            model.addAttribute("nomorRekamMedis", pasien.getNomorRekamMedis());
        }

        // Get all appointments for the pasien
        String pasienId = pasien != null ? pasien.getIdUser() : user.getIdUser();
        List<Appointment> appointments = appointmentService.getAppointmentsByPasienId(pasienId);

        // Auto-update status for past booking dates
        appointmentService.updateExpiredAppointments();

        // Populate transient fields for dokter in each appointment
        for (Appointment appointment : appointments) {
            if (appointment.getJadwal() != null && appointment.getJadwal().getDokter() != null) {
                dokterService.enrichWithUserData(appointment.getJadwal().getDokter());
            }
        }

        model.addAttribute("appointments", appointments);

        return "pasien/jadwal-riwayat";
    }

    @GetMapping("/api/dokter/{dokterId}")
    @ResponseBody
    public java.util.Map<String, Object> getJadwalByDokter(@PathVariable String dokterId) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            List<JadwalPraktik> jadwals = appointmentService.getJadwalByDokterId(dokterId);

            // Populate transient fields for dokter in each jadwal
            for (JadwalPraktik jadwal : jadwals) {
                if (jadwal.getDokter() != null) {
                    dokterService.enrichWithUserData(jadwal.getDokter());
                }
            }

            response.put("success", true);
            response.put("jadwals", jadwals);
            System.out.println("Jadwal data returned for dokterId " + dokterId + ": " + jadwals.size() + " records");
            String poliData = "N/A";
            if (!jadwals.isEmpty() && jadwals.get(0).getDokter() != null
                    && jadwals.get(0).getDokter().getPoli() != null) {
                poliData = jadwals.get(0).getDokter().getPoli().getNamaPoli();
            }
            System.out.println("First jadwal poli data: " + poliData);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            System.err.println("Error fetching jadwal data: " + e.getMessage());
        }
        return response;
    }

    // Dokter jadwal management endpoints
    @GetMapping("/praktik")
    public String jadwalPraktik(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Dokter dokter = dokterRepository.findByIdUser(user.getIdUser()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "jadwal-praktik");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);

            String dokterId = dokter.getIdUser();
            List<JadwalPraktik> jadwals = dokterService.getJadwalByDokterId(dokterId);
            model.addAttribute("jadwals", jadwals);
        }

        return "dokter/jadwal-praktik";
    }

    @PostMapping("/create")
    public String createJadwal(@ModelAttribute JadwalPraktik jadwal,
            @RequestParam(required = false) String principal,
            RedirectAttributes redirectAttributes) {
        try {
            // Dokter will be set in service layer based on logged-in user
            dokterService.createJadwal(jadwal);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil dibuat!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jadwal/praktik";
    }

    @PostMapping("/update")
    public String updateJadwal(@ModelAttribute JadwalPraktik jadwal,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.updateJadwal(jadwal);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jadwal/praktik";
    }

    @PostMapping("/delete/{id}")
    public String deleteJadwal(@PathVariable String id,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.deleteJadwal(id);
            redirectAttributes.addFlashAttribute("success", "Jadwal berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jadwal/praktik";
    }

    @GetMapping("/api")
    @ResponseBody
    public Map<String, Object> getJadwalDokter(@AuthenticationPrincipal UserDetails principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Dokter dokter = dokterRepository.findByIdUser(user.getIdUser()).orElse(null);
            if (dokter == null) {
                response.put("success", false);
                response.put("message", "Dokter not found");
                return response;
            }

            String dokterId = dokter.getIdUser();
            java.util.List<Map<String, Object>> jadwalWithDates = jadwalPraktikService
                    .getJadwalWithDatesForDokter(dokterId);

            response.put("success", true);
            response.put("jadwals", jadwalWithDates);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
