package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.dto.BookingRequestDTO;
import rsis.model.Appointment;
import rsis.model.AppUser;
import rsis.model.JadwalPraktik;
import rsis.repository.UserRepository;
import rsis.service.AppointmentService;
import rsis.service.NotifikasiService;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotifikasiService notifikasiService;

    @Autowired
    private UserRepository userRepository;

    private void addNotifikasiToModel(String userId, Model model) {
        try {
            var notifikasis = notifikasiService.getNotifikasiByPenerimaId(userId);
            model.addAttribute("notifikasi", notifikasis);
        } catch (Exception e) {
            model.addAttribute("notifikasi", List.of());
        }
    }

    @GetMapping("/booking")
    public String showBookingForm(@AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String jadwalId,
            @RequestParam(required = false) String dokterId,
            Model model) {
        // Add navbar attributes
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("nama", appUser.getNama());
        model.addAttribute("role", appUser.getRole());
        model.addAttribute("activeMenu", "booking");

        // Get notifications
        addNotifikasiToModel(appUser.getIdUser(), model);

        // If jadwalId is provided, fetch doctor and schedule data
        if (jadwalId != null && !jadwalId.isEmpty()) {
            Optional<JadwalPraktik> jadwalOpt = appointmentService.getJadwalById(jadwalId);
            if (jadwalOpt.isPresent()) {
                JadwalPraktik jadwal = jadwalOpt.get();
                model.addAttribute("jadwal", jadwal);
                model.addAttribute("dokter", jadwal.getDokter());
                model.addAttribute("spesialisasi", jadwal.getDokter().getSpesialisasi());
                model.addAttribute("poli", jadwal.getDokter().getPoli());

                // Pre-fill bookingRequest with jadwalId
                BookingRequestDTO bookingRequest = new BookingRequestDTO();
                bookingRequest.setJadwalId(jadwalId);
                model.addAttribute("bookingRequest", bookingRequest);
            } else {
                model.addAttribute("error", "Jadwal tidak ditemukan");
                model.addAttribute("bookingRequest", new BookingRequestDTO());
            }
        } else if (dokterId != null && !dokterId.isEmpty()) {
            // If dokterId is provided, fetch doctor data and available schedules
            Optional<rsis.model.Dokter> dokterOpt = appointmentService.getDokterById(dokterId);
            if (dokterOpt.isPresent()) {
                rsis.model.Dokter dokter = dokterOpt.get();
                model.addAttribute("dokter", dokter);
                model.addAttribute("spesialisasi", dokter.getSpesialisasi());
                model.addAttribute("poli", dokter.getPoli());

                // Fetch available schedules for this doctor
                List<JadwalPraktik> availableJadwal = appointmentService.getJadwalByDokterId(dokterId);
                model.addAttribute("availableDates", availableJadwal);

                model.addAttribute("bookingRequest", new BookingRequestDTO());
            } else {
                model.addAttribute("error", "Dokter tidak ditemukan");
                model.addAttribute("bookingRequest", new BookingRequestDTO());
            }
        } else {
            model.addAttribute("bookingRequest", new BookingRequestDTO());
        }

        return "pasien/booking";
    }

    @PostMapping("/booking")
    public String bookAppointment(@ModelAttribute BookingRequestDTO bookingRequest,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            // Set pasienId from logged-in user
            String pasienId = principal.getName(); // Assuming username is pasienId
            bookingRequest.setPasienId(pasienId);

            Appointment appointment = appointmentService.bookAppointment(bookingRequest);

            // Send notification to pasien
            notifikasiService.kirimNotifikasi(pasienId,
                    "Appointment berhasil dibuat dengan ID: " + appointment.getIdAppointment(),
                    "BOOKING");

            redirectAttributes.addFlashAttribute("success", "Appointment berhasil dibuat!");
            return "redirect:/appointment/my-appointments";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/appointment/booking";
        }
    }

    @GetMapping("/my-appointments")
    public String showMyAppointments(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Add navbar attributes
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("nama", appUser.getNama());
        model.addAttribute("role", appUser.getRole());
        model.addAttribute("activeMenu", "jadwal");

        // Get notifications
        String pasienId = principal.getUsername();
        addNotifikasiToModel(pasienId, model);

        List<Appointment> appointments = appointmentService.getAppointmentsByPasienId(pasienId);
        model.addAttribute("appointments", appointments);
        return "pasien/jadwal-riwayat";
    }

    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable String id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Appointment berhasil dibatalkan!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/appointment/my-appointments";
    }

    @PostMapping("/reschedule/{id}")
    public String rescheduleAppointment(@PathVariable String id,
            @RequestParam String newJadwalId,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            appointmentService.rescheduleAppointment(id, newJadwalId);
            redirectAttributes.addFlashAttribute("success", "Appointment berhasil dijadwalkan ulang!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/appointment/my-appointments";
    }

    @GetMapping("/detail/{id}")
    public String showAppointmentDetail(@PathVariable String id,
            @AuthenticationPrincipal UserDetails principal,
            Model model) {
        // Add navbar attributes
        AppUser appUser = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("nama", appUser.getNama());
        model.addAttribute("role", appUser.getRole());
        model.addAttribute("activeMenu", "jadwal");

        // Get notifications
        addNotifikasiToModel(appUser.getIdUser(), model);

        Appointment appointment = appointmentService.getAppointmentById(id).orElse(null);
        model.addAttribute("appointment", appointment);
        return "pasien/jadwal-riwayat";
    }
}
