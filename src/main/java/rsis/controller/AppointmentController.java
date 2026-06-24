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
import rsis.model.Dokter;
import rsis.model.JadwalPraktik;
import rsis.model.Pasien;
import rsis.model.User;
import rsis.repository.PasienRepository;
import rsis.repository.UserRepository;
import rsis.service.AppointmentService;
import rsis.service.DokterService;
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

    @Autowired
    private PasienRepository pasienRepository;

    @Autowired
    private DokterService dokterService;

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
            @RequestParam(required = false) String reschedule,
            Model model) {
        // Add navbar attributes
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pasien pasien = pasienRepository.findByIdUser(user.getIdUser()).orElse(null);
        String userId = pasien != null ? pasien.getIdUser() : user.getIdUser();

        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "booking");
        model.addAttribute("nomorHp", user.getNomorHp());
        model.addAttribute("tanggalLahir", pasien != null ? pasien.getTanggalLahir() : null);
        model.addAttribute("alamat", pasien != null ? pasien.getAlamat() : null);

        // Get notifications
        addNotifikasiToModel(userId, model);

        // Handle reschedule - fetch existing appointment
        if (reschedule != null && !reschedule.isEmpty()) {
            try {
                Optional<Appointment> appointmentOpt = appointmentService.getAppointmentById(reschedule);
                if (appointmentOpt.isPresent()) {
                    Appointment appointment = appointmentOpt.get();
                    // Verify the appointment belongs to the logged-in pasien
                    if (appointment.getUser().getIdUser().equals(userId)) {
                        model.addAttribute("rescheduleAppointment", appointment);
                        model.addAttribute("isReschedule", true);
                    }
                }
            } catch (RuntimeException e) {
                model.addAttribute("error", "Appointment tidak ditemukan untuk reschedule");
            }
        }

        // If jadwalId is provided, fetch doctor and schedule data
        if (jadwalId != null && !jadwalId.isEmpty()) {
            try {
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
            } catch (RuntimeException e) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("bookingRequest", new BookingRequestDTO());
            }
        } else if (dokterId != null && !dokterId.isEmpty()) {
            // If dokterId is provided, fetch doctor data and available schedules
            try {
                Optional<Dokter> dokterOpt = appointmentService.getDokterById(dokterId);
                if (dokterOpt.isPresent()) {
                    Dokter dokter = dokterOpt.get();
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
            } catch (RuntimeException e) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("bookingRequest", new BookingRequestDTO());
            }
        } else {
            model.addAttribute("bookingRequest", new BookingRequestDTO());
        }

        return "pasien/booking";
    }

    @PostMapping("/booking")
    public String bookAppointment(@ModelAttribute BookingRequestDTO bookingRequest,
            @RequestParam(required = false) String reschedule,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            // Get pasien data
            User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Pasien pasien = pasienRepository.findByIdUser(user.getIdUser())
                    .orElseThrow(() -> new RuntimeException("Pasien not found"));

            // Set pasienId from logged-in user
            bookingRequest.setPasienId(pasien.getIdUser());

            // Handle reschedule - cancel old appointment first
            if (reschedule != null && !reschedule.isEmpty()) {
                Optional<Appointment> oldAppointmentOpt = appointmentService.getAppointmentById(reschedule);
                if (oldAppointmentOpt.isPresent()) {
                    Appointment oldAppointment = oldAppointmentOpt.get();
                    // Verify the appointment belongs to the logged-in pasien
                    if (oldAppointment.getUser().getIdUser().equals(pasien.getIdUser())) {
                        appointmentService.cancelAppointment(reschedule);
                    }
                }
            }

            Appointment appointment = appointmentService.bookAppointment(bookingRequest);

            // Send notification to pasien
            String message = reschedule != null && !reschedule.isEmpty()
                    ? "Appointment berhasil dijadwalkan ulang dengan ID: " + appointment.getIdAppointment()
                    : "Appointment berhasil dibuat dengan ID: " + appointment.getIdAppointment();
            notifikasiService.kirimNotifikasi(pasien.getIdUser(), message, "APPOINTMENT_BARU");

            redirectAttributes.addFlashAttribute("success",
                    reschedule != null && !reschedule.isEmpty() ? "Appointment berhasil dijadwalkan ulang!"
                            : "Appointment berhasil dibuat!");
            return "redirect:/jadwal/riwayat";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Include parameters in redirect to preserve context
            String redirectUrl = "redirect:/appointment/booking";
            if (bookingRequest.getJadwalId() != null && !bookingRequest.getJadwalId().isEmpty()) {
                redirectUrl += "?jadwalId=" + bookingRequest.getJadwalId();
            }
            if (reschedule != null && !reschedule.isEmpty()) {
                redirectUrl += (redirectUrl.contains("?") ? "&" : "?") + "reschedule=" + reschedule;
            }
            return redirectUrl;
        }
    }

    @GetMapping("/my-appointments")
    public String showMyAppointments(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Add navbar attributes
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
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

    @PostMapping("/cancel/{id}/api")
    @ResponseBody
    public java.util.Map<String, Object> cancelAppointmentApi(@PathVariable String id,
            Principal principal) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            appointmentService.cancelAppointment(id);
            response.put("success", true);
            response.put("message", "Appointment berhasil dibatalkan!");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
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
    @ResponseBody
    public java.util.Map<String, Object> getAppointmentDetail(@PathVariable String id,
            @AuthenticationPrincipal UserDetails principal) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Pasien pasien = pasienRepository.findByIdUser(user.getIdUser())
                    .orElseThrow(() -> new RuntimeException("Pasien not found"));

            Appointment appointment = appointmentService.getAppointmentById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            // Verify the appointment belongs to the logged-in pasien
            if (!appointment.getUser().getIdUser().equals(pasien.getIdUser())) {
                throw new RuntimeException("Unauthorized access to appointment");
            }

            // Populate transient fields for dokter
            if (appointment.getJadwal() != null && appointment.getJadwal().getDokter() != null) {
                dokterService.enrichWithUserData(appointment.getJadwal().getDokter());
            }

            response.put("success", true);
            response.put("appointment", appointment);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // Doctor appointment management endpoints
    @GetMapping("/dokter")
    public String showDoctorAppointments(@AuthenticationPrincipal UserDetails principal, Model model) {
        // Get user data for navbar
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("nama", user.getNama());
        model.addAttribute("role", user.getRole());
        model.addAttribute("activeMenu", "appointment");

        // Get notifications
        addNotifikasiToModel(user.getIdUser(), model);

        String dokterId = user.getIdUser();
        List<Appointment> appointments = dokterService.getPendingAppointments(dokterId);
        model.addAttribute("appointments", appointments);

        return "dokter/appointment";
    }

    @PostMapping("/konfirmasi/{id}")
    public String konfirmasiAppointment(@PathVariable String id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            dokterService.konfirmasiAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Appointment berhasil dikonfirmasi!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/appointment/dokter";
    }

    @PostMapping("/tolak/{id}")
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
        return "redirect:/appointment/dokter";
    }

    @PostMapping("/update/{id}")
    @ResponseBody
    public java.util.Map<String, Object> updateAppointment(@PathVariable String id,
            @RequestBody java.util.Map<String, String> request,
            @AuthenticationPrincipal UserDetails principal) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Pasien pasien = pasienRepository.findByIdUser(user.getIdUser())
                    .orElseThrow(() -> new RuntimeException("Pasien not found"));

            Appointment appointment = appointmentService.getAppointmentById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            // Verify the appointment belongs to the logged-in pasien
            if (!appointment.getUser().getIdUser().equals(pasien.getIdUser())) {
                throw new RuntimeException("Unauthorized access to appointment");
            }

            String newJadwalId = request.get("jadwalId");
            String newCatatan = request.get("catatan");

            if (newJadwalId != null && !newJadwalId.isEmpty()) {
                Optional<JadwalPraktik> newJadwalOpt = appointmentService.getJadwalById(newJadwalId);
                if (newJadwalOpt.isPresent()) {
                    appointment.setJadwal(newJadwalOpt.get());
                }
            }

            if (newCatatan != null) {
                appointment.setCatatan(newCatatan);
            }

            appointmentService.updateAppointment(appointment);

            response.put("success", true);
            response.put("message", "Appointment berhasil diperbarui");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
