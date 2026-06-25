package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.model.Appointment;
import rsis.model.User;
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.service.DokterService;
import rsis.service.UserService;
import rsis.service.JadwalPraktikService;
import rsis.service.NotifikasiService;
import rsis.service.AppointmentService;

import java.util.List;

@Controller
@RequestMapping("/dokter")
public class DokterController {

    @Autowired
    private DokterService dokterService;

    @Autowired
    private JadwalPraktikService jadwalPraktikService;

    @Autowired
    private NotifikasiService notifikasiService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentService appointmentService;

    private void addNotifikasiToModel(String userId, Model model) {
        try {
            var notifikasis = notifikasiService.getNotifikasiByPenerimaId(userId);
            model.addAttribute("notifikasi", notifikasis);
        } catch (Exception e) {
            model.addAttribute("notifikasi", List.of());
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Dokter dokter = dokterService.getDokterByIdUser(user.getIdUser()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "dashboard");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);
        }

        String dokterId = dokter != null ? dokter.getIdUser() : "";
        model.addAttribute("dokterId", dokterId);
        return "dokter/dashboard";
    }

    @GetMapping("/daftar-pasien")
    public String daftarPasien(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Dokter dokter = dokterService.getDokterByIdUser(user.getIdUser()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "jadwal");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);

            String dokterId = dokter.getIdUser();
            // Ubah pemanggilan fungsinya agar mengambil SEMUA data, bukan cuma yang pending
            List<Appointment> appointments = dokterService.getDaftarPasien(dokterId);
            model.addAttribute("appointments", appointments);
        }

        return "dokter/daftar-pasien";
    }

    @GetMapping("/appointment")
    public String appointment(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Dokter dokter = dokterService.getDokterByIdUser(user.getIdUser()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "appointment");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);

            String dokterId = dokter.getIdUser();
            List<Appointment> appointments = appointmentService.getAppointmentsByDokterId(dokterId);
            model.addAttribute("appointments", appointments);
        }

        return "dokter/appointment";
    }

    @GetMapping("/profil")
    public String profil(Model model) {
        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Dokter dokter = dokterService.getDokterByIdUser(user.getIdUser()).orElse(null);
        if (dokter != null) {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "profil");
            model.addAttribute("idUser", dokter.getIdUser());
            model.addAttribute("nomorHp", user.getNomorHp() != null ? user.getNomorHp() : "");
            model.addAttribute("alamat", ""); // Dokter doesn't have alamat field
            model.addAttribute("spesialisasi",
                    dokter.getSpesialisasi() != null ? dokter.getSpesialisasi().getNama() : "");
            model.addAttribute("poli", dokter.getPoli() != null ? dokter.getPoli().getNamaPoli() : "");
            model.addAttribute("nomorStr", dokter.getNomorStr() != null ? dokter.getNomorStr() : "");

            // Get notifications
            addNotifikasiToModel(dokter.getIdUser(), model);

            // Get statistics for dokter
            String dokterId = dokter.getIdUser();
            List<JadwalPraktik> jadwals = jadwalPraktikService.getJadwalByDokterId(dokterId);
            model.addAttribute("totalJadwal", jadwals.size());

            List<Appointment> appointments = dokterService.getDaftarPasien(dokterId);
            model.addAttribute("totalPasien", appointments.size());
        } else {
            model.addAttribute("nama", user.getNama());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("role", user.getRole());
            model.addAttribute("activeMenu", "profil");
            model.addAttribute("idUser", user.getIdUser());
            model.addAttribute("nomorHp", "");
            model.addAttribute("alamat", "");
            model.addAttribute("spesialisasi", "");
            model.addAttribute("poli", "");
            model.addAttribute("nomorStr", "");
            model.addAttribute("totalJadwal", 0);
            model.addAttribute("totalPasien", 0);
        }

        return "dokter/profil";
    }

    @PostMapping("/profil")
    public String updateProfile(@RequestParam String namaLengkap,
            @RequestParam String nomorTelepon,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            User user = (User) model.getAttribute("currentUser");
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            // Update nama and nomorHp in user
            user.setNama(namaLengkap);
            user.setNomorHp(nomorTelepon);
            userService.saveUser(user);

            redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/profil";
    }

    @PostMapping("/profil/password")
    public String changePassword(@RequestParam String passwordLama,
            @RequestParam String passwordBaru,
            @RequestParam String konfirmasiPassword,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            User user = (User) model.getAttribute("currentUser");
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            userService.changePassword(user.getIdUser(), passwordLama, passwordBaru, konfirmasiPassword);
            redirectAttributes.addFlashAttribute("success", "Kata sandi berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dokter/profil";
    }

}
