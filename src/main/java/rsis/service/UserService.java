package rsis.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rsis.model.User;
import rsis.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        User user = userOpt.get();
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole()));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    public User saveUser(User user) {
        if (user == null) {
            throw new RuntimeException("User cannot be null");
        }
        return userRepository.save(user);
    }

    public Optional<User> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public void changePassword(String userId, String oldPassword, String newPassword, String confirmPassword) {
        if (userId == null) {
            throw new RuntimeException("User ID cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Kata sandi lama tidak sesuai");
        }

        // Validate new password length
        if (newPassword.length() < 8) {
            throw new RuntimeException("Kata sandi baru minimal 8 karakter");
        }

        // Validate password confirmation
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Konfirmasi kata sandi baru harus sama dengan kata sandi baru");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
