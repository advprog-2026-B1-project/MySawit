package com.b1.mysawit.auth.service;

import com.b1.mysawit.auth.dto.RegisterRequest;
import com.b1.mysawit.auth.dto.UserResponse;
import com.b1.mysawit.common.exception.DuplicateResourceException;
import com.b1.mysawit.domain.MandorDetail;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.MandorDetailRepository;
import com.b1.mysawit.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final MandorDetailRepository mandorDetailRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       MandorDetailRepository mandorDetailRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mandorDetailRepository = mandorDetailRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

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

        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public boolean login(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .orElse(false);
    }
}
