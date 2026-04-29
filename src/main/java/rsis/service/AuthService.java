package rsis.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.User;
import rsis.repository.AppUserRepository;

import java.time.Instant;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerPasien(String nama, String email, String rawPassword) {
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }

        User u = new User();
        u.setIdUser(nextUserId());
        u.setNama(nama);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setNomorHp(null);
        u.setRole("PASIEN");
        u.setCreatedAt(Instant.now());

        appUserRepository.save(u);
    }

    private String nextUserId() {
        int next = appUserRepository.findLatestUserId()
                .map(id -> {
                    String suffix = id.length() >= 3 ? id.substring(2) : "0";
                    try {
                        return Integer.parseInt(suffix);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .orElse(0) + 1;

        return String.format("u-%02d", next);
    }
}
