package rsis.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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

    public AuthController(AuthService authService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
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
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute("form") RegisterForm form,
                                 BindingResult binding,
                                 Model model,
                                 HttpServletRequest request) {
        // Bean Validation on @ModelAttribute record isn't automatically triggered everywhere;
        // keep a tiny guard for required fields to avoid bad inserts.
        if (isBlank(form.nama()) || isBlank(form.email()) || isBlank(form.password())) {
            model.addAttribute("errorMessage", "Nama, email, dan password wajib diisi");
            return "auth/register";
        }
        try {
            authService.registerPasien(form.nama(), form.email(), form.password());
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(form.email(), form.password())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
            session.setAttribute("FLASH_SUCCESS", "Registrasi berhasil. Selamat datang!");
            return "redirect:/pasien/dashboard";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }
    }

    public record RegisterForm(
            @NotBlank(message = "Nama wajib diisi") String nama,
            @NotBlank(message = "Email wajib diisi") @Email(message = "Email tidak valid") String email,
            @NotBlank(message = "Password wajib diisi") String password
    ) {
        public RegisterForm() {
            this("", "", "");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
