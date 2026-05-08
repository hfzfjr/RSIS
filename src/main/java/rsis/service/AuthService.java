package rsis.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.AppUser;
import rsis.model.Pasien;
import rsis.repository.AppUserRepository;
import rsis.repository.PasienRepository;
import rsis.repository.UserRepository;

import java.time.Instant;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasienRepository pasienRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, PasienRepository pasienRepository,
            UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.pasienRepository = pasienRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerPasien(String nama, String email, String rawPassword) {
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }

        String userId = nextUserId();
        String pasienId = nextPasienId();

        // Save to users table (AppUser)
        AppUser appUser = new AppUser();
        appUser.setIdUser(userId);
        appUser.setNama(nama);
        appUser.setEmail(email);
        appUser.setPassword(passwordEncoder.encode(rawPassword));
        appUser.setNomorHp(null);
        appUser.setRole("PASIEN");
        appUser.setCreatedAt(Instant.now());
        appUserRepository.save(appUser);

        // Save to pasien table with FK to users
        Pasien pasien = new Pasien();
        pasien.setIdPasien(pasienId);
        pasien.setIdUser(userId);
        pasien.setNama(nama);
        pasien.setEmail(email);
        pasien.setPassword(appUser.getPassword());
        pasien.setNomorHp(null);
        pasien.setRole("PASIEN");
        // created_at only in users table, not in pasien table
        pasienRepository.save(pasien);
    }

    private String nextUserId() {
        int next = userRepository.findLatestUserId()
                .map(id -> {
                    String suffix = id.length() >= 5 ? id.substring(4) : "0";
                    try {
                        return Integer.parseInt(suffix);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .orElse(0) + 1;

        return String.format("usr-%03d", next);
    }

    private String nextPasienId() {
        int next = pasienRepository.findLatestPasienId()
                .map(id -> {
                    // Skip "psn-" (4 characters) to get the numeric part
                    String suffix = id.length() > 4 ? id.substring(4) : "0";
                    try {
                        return Integer.parseInt(suffix);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .orElse(0) + 1;

        return String.format("psn-%03d", next);
    }
}
