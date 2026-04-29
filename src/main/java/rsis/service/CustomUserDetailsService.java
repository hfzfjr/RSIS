package rsis.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rsis.model.User;
import rsis.repository.AppUserRepository;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPassword(),
                authoritiesFromRole(u.getRole())
        );
    }

    private List<GrantedAuthority> authoritiesFromRole(String role) {
        String normalized = role == null ? "" : role.trim().toLowerCase();
        return switch (normalized) {
            case "admin", "adminrs", "admin_rs" -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            case "dokter" -> List.of(new SimpleGrantedAuthority("ROLE_DOKTER"));
            default -> List.of(new SimpleGrantedAuthority("ROLE_PASIEN"));
        };
    }
}
