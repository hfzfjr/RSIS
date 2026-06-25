package rsis.config;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import rsis.model.User;
import rsis.repository.UserRepository;

@ControllerAdvice
public class UserControllerAdvice {

    private final UserRepository userRepository;

    public UserControllerAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void addUserToModel(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (principal != null) {
            User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                    .orElse(null);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("nama", user.getNama());
            }
        }
    }
}
