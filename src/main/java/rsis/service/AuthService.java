package rsis.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.AppUser;
import rsis.model.Pasien;
import rsis.repository.UserRepository;
import rsis.repository.PasienRepository;

import java.time.Instant;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasienRepository pasienRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasienRepository pasienRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.pasienRepository = pasienRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerPasien(String nama, String email, String rawPassword) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }

        // Validate email domain (must be @gmail.com)
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            throw new IllegalArgumentException("Email harus menggunakan domain @gmail.com");
        }

        // Validate password length (minimum 8 characters)
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password minimal 8 karakter");
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
        userRepository.save(appUser);

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
