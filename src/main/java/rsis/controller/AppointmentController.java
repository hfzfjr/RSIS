package rsis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rsis.dto.BookingRequestDTO;
import rsis.model.Appointment;
import rsis.service.AppointmentService;
import rsis.service.NotifikasiService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotifikasiService notifikasiService;

    @GetMapping("/booking")
    public String showBookingForm(Model model) {
        model.addAttribute("bookingRequest", new BookingRequestDTO());
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
    public String showMyAppointments(Principal principal, Model model) {
        String pasienId = principal.getName();
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
    public String showAppointmentDetail(@PathVariable String id, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(id).orElse(null);
        model.addAttribute("appointment", appointment);
        return "pasien/jadwal-riwayat";
    }
}
