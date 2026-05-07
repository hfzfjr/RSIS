package rsis.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rsis.model.Pasien;
import rsis.repository.PasienRepository;

import java.time.Instant;

@Service
public class AuthService {
    private final PasienRepository pasienRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(PasienRepository pasienRepository, PasswordEncoder passwordEncoder) {
        this.pasienRepository = pasienRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerPasien(String nama, String email, String rawPassword) {
        if (pasienRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }

        Pasien u = new Pasien();
        u.setIdUser(nextUserId());
        u.setNama(nama);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setNomorHp(null);
        u.setRole("PASIEN");
        u.setCreatedAt(Instant.now());

        pasienRepository.save(u);
    }

    private String nextUserId() {
        int next = pasienRepository.findLatestUserId()
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
