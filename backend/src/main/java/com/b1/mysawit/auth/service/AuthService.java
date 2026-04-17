package com.b1.mysawit.auth.service;

import com.b1.mysawit.domain.MandorDetail;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.auth.dto.RegisterRequest;
import com.b1.mysawit.repository.MandorDetailRepository;
import com.b1.mysawit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MandorDetailRepository mandorDetailRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setNama(request.nama());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(User.Role.valueOf(request.role()));
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == User.Role.Mandor) {
            MandorDetail detail = new MandorDetail();
            detail.setMandor(savedUser);
            detail.setNomorSertifikasi(request.nomorSertifikasi());
            mandorDetailRepository.save(detail);
        }

        return savedUser;
    }

    public boolean login(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(password, user.getPasswordHash())) // UPDATE BAGIAN INI
                .orElse(false);
    }
}