package rsis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import rsis.service.UserService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null)
                    return false;
                String s = encodedPassword.trim();
                boolean looksLikeBcrypt = s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$");
                if (looksLikeBcrypt) {
                    return bcrypt.matches(rawPassword, s);
                }
                // Backward-compat: allow legacy plaintext passwords stored in DB.
                return rawPassword != null && s.contentEquals(rawPassword);
            }
        };
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider(UserService userDetailsService,
            PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(encoder);
        provider.setHideUserNotFoundExceptions(false); // Membuka segel agar eror notfound terbaca
        return provider;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationSuccessHandler successHandler)
            throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/auth", "/login", "/register", "/css/**", "/js/**", "/images/**",
                                "/landing")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/dokter/**").hasRole("DOKTER")
                        .requestMatchers("/pasien/**").hasRole("PASIEN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/auth?tab=login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureHandler((request, response, exception) -> {
                            String errorType = "wrongpassword";

                            // Cek apakah erornya karena email tidak ditemukan
                            if (exception instanceof org.springframework.security.core.userdetails.UsernameNotFoundException) {
                                errorType = "notfound";
                            }

                            // Redirect sambil membawa parameter error yang spesifik (hanya dipanggil 1
                            // kali)
                            response.sendRedirect("/auth?tab=login&error=" + errorType);
                        })
                        .permitAll())
                .rememberMe(remember -> remember
                        .key("kunciRahasiaRSIS")
                        .tokenValiditySeconds(604800)) // Ingat user selama 7 hari
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth?tab=login&logout"));

        return http.build();
    }

    @Bean
    AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            request.getSession(true).setAttribute("FLASH_SUCCESS", "Login berhasil");
            response.sendRedirect(resolveTargetUrl(authentication));
        };
    }

    private String resolveTargetUrl(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "/admin/dashboard";
        }
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOKTER"))) {
            return "/dokter/dashboard";
        }
        return "/pasien/dashboard";
    }
}