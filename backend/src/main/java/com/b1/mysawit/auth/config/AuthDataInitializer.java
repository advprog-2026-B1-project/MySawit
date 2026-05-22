package com.b1.mysawit.auth.config;

import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class AuthDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AuthDataInitializer(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${mysawit.auth.admin-email:admin@mysawit.com}") String adminEmail,
                               @Value("${mysawit.auth.admin-password:admin123}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail(adminEmail);
        admin.setNama("Admin Utama");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(User.Role.Admin);
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        userRepository.save(admin);
    }
}
