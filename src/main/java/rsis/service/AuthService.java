package rsis.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.Pasien;
import rsis.repository.UserRepository;
import rsis.repository.PasienRepository;

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

        // With Joined Table Inheritance, saving Pasien will automatically create
        // entries in both users and pasien tables
        Pasien pasien = new Pasien();
        pasien.setIdUser(userId);
        pasien.setNama(nama);
        pasien.setEmail(email);
        pasien.setPassword(passwordEncoder.encode(rawPassword));
        pasien.setRole("PASIEN");
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
}
