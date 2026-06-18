package rsis.controller;

import jakarta.servlet.http.HttpServletResponse; 
import org.springframework.security.core.context.SecurityContext; 
import org.springframework.security.web.context.HttpSessionSecurityContextRepository; 
import org.springframework.security.web.context.SecurityContextRepository; 
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // <-- IMPORT BARU
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import rsis.service.AuthService;

@Controller
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @org.springframework.beans.factory.annotation.Autowired
    private rsis.service.AdminRSService adminRSService;

    @GetMapping("/temp-debug")
    @org.springframework.web.bind.annotation.ResponseBody
    public String tempDebug() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("=== BEFORE UPDATE ===\n");
            var before = adminRSService.getAllDokter().stream()
                    .filter(d -> d.getIdUser().equals("usr-0059")).findFirst().orElse(null);
            if (before != null) {
                sb.append("Nama: ").append(before.getNama()).append(", STR: ").append(before.getNomorStr())
                  .append(", HP: ").append(before.getNomorHp()).append("\n");
            } else {
                sb.append("usr-0059 not found\n");
            }

            var specs = adminRSService.getAllSpesialisasi();
            var polis = adminRSService.getAllPoli();
            String specId = specs.isEmpty() ? null : specs.get(0).getIdSpesialisasi();
            String poliId = polis.isEmpty() ? null : polis.get(0).getIdPoli();

            sb.append("=== RUNNING UPDATE ===\n");
            sb.append("SpecId: ").append(specId).append(", PoliId: ").append(poliId).append("\n");
            
            adminRSService.updateDokter("usr-0059", "muhammad edited", "89098765423", "STR-20260615-9999", specId, poliId);

            sb.append("=== AFTER UPDATE ===\n");
            // Clear entity manager cache to force reload from DB if needed
            var after = adminRSService.getAllDokter().stream()
                    .filter(d -> d.getIdUser().equals("usr-0059")).findFirst().orElse(null);
            if (after != null) {
                sb.append("Nama: ").append(after.getNama()).append(", STR: ").append(after.getNomorStr())
                  .append(", HP: ").append(after.getNomorHp()).append("\n");
            }

            return sb.toString();
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return "Error: " + sw.toString();
        }
    }

    public AuthController(AuthService authService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/")
    public String root() {
        return "landing";
    }

@GetMapping("/auth")
    public String authPage(@RequestParam(value = "tab", required = false) String tab,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {
        
        // LOGIKA BARU: Cek jenis eror dari URL yang dilempar oleh SecurityConfig
        if (error != null) {
            if (error.equals("notfound")) {
                model.addAttribute("errorMessage", "Email tidak terdaftar, tolong registrasi dahulu.");
            } else if (error.equals("wrongpassword") || error.equals("true")) {
                // error=true ditambahkan sebagai fallback darurat bawaan lama
                model.addAttribute("errorMessage", "Email atau kata sandi salah.");
            }
        }

        model.addAttribute("hasLogout", logout != null);
        model.addAttribute("hasRegistered", registered != null);
        
        // Cek agar data form (nama, email) yang diketik tidak terhapus saat direfresh
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterForm());
        }
        
        return "auth/auth";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {
        model.addAttribute("hasError", error != null);
        model.addAttribute("hasLogout", logout != null);
        model.addAttribute("hasRegistered", registered != null);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterForm());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute("form") RegisterForm form,
            BindingResult binding,
            RedirectAttributes redirectAttributes, // PERUBAHAN 2: Pakai RedirectAttributes, bukan Model
            HttpServletRequest request,
            HttpServletResponse response) {
        
        // PERUBAHAN 3: Ganti model.addAttribute jadi redirectAttributes.addFlashAttribute
        // Serta return "auth/auth" diubah jadi return "redirect:/auth?tab=register"
        
        if (isBlank(form.namaLengkap()) || isBlank(form.email()) || isBlank(form.password())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nama lengkap, email, dan password wajib diisi");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/auth?tab=register";
        }

        if (!form.password().equals(form.confirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password dan konfirmasi password tidak cocok");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/auth?tab=register";
        }

        if (form.password().length() < 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password minimal 8 karakter");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/auth?tab=register";
        }

        if (!form.email().toLowerCase().endsWith("@gmail.com")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email harus menggunakan domain @gmail.com");
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/auth?tab=register";
        }

        try {
            // 1. Simpan data pasien ke database
            authService.registerPasien(form.namaLengkap(), form.email(), form.password());
            
            // 2. Lakukan proses login di belakang layar
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(form.email(), form.password()));
            
            // 3. Buat SecurityContext (Standar baru Spring Security 6)
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            
            // 4. Simpan status login menggunakan repository
            securityContextRepository.saveContext(context, request, response);

            // 5. Berikan pesan sukses
            HttpSession session = request.getSession(true);
            session.setAttribute("FLASH_SUCCESS", "Registrasi berhasil. Selamat datang!");
            
            // 6. Langsung masuk ke dashboard
            return "redirect:/pasien/dashboard";
            
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/auth?tab=register";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Terjadi kesalahan sistem: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/auth?tab=register";
        }
    } 

    public record RegisterForm(
            @NotBlank(message = "Nama lengkap wajib diisi") String namaLengkap,
            @NotBlank(message = "Email wajib diisi") @Email(message = "Email tidak valid") String email,
            @NotBlank(message = "Password wajib diisi") String password,
            @NotBlank(message = "Konfirmasi password wajib diisi") String confirmPassword) {
        public RegisterForm() {
            this("", "", "", "");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}