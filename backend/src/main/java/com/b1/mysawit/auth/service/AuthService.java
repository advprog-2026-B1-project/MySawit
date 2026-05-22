package com.b1.mysawit.auth.service;

import com.b1.mysawit.auth.dto.RegisterRequest;
import com.b1.mysawit.auth.dto.UserResponse;
import com.b1.mysawit.common.exception.BusinessRuleViolationException;
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

    @Transactional
    public UserResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        String email = request.getEmail().trim();
        User.Role role = parseRole(request.getRole());
        if (role == User.Role.Mandor && isBlank(request.getNomorSertifikasiMandor())) {
            throw new BusinessRuleViolationException("Nomor Sertifikasi Mandor wajib diisi untuk role Mandor.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        User user = new User();
        OffsetDateTime now = OffsetDateTime.now();

        user.setNama(request.getNama().trim());
        user.setEmail(email);
        user.setUsername(defaultUsername(request));
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);

        if (role == User.Role.Mandor) {
            MandorDetail mandorDetail = new MandorDetail();
            mandorDetail.setNomorSertifikasi(request.getNomorSertifikasiMandor().trim());
            mandorDetail.setMandor(savedUser);
            mandorDetailRepository.save(mandorDetail);
        }

        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public boolean login(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .orElse(false);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Data registrasi tidak boleh kosong");
        }
        if (isBlank(request.getNama())) {
            throw new IllegalArgumentException("Nama wajib diisi");
        }
        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("Email wajib diisi");
        }
        if (isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Password wajib diisi");
        }
        if (isBlank(request.getRole())) {
            throw new IllegalArgumentException("Role wajib diisi");
        }
    }

    private String defaultUsername(RegisterRequest request) {
        if (!isBlank(request.getUsername())) {
            return request.getUsername().trim();
        }
        return request.getEmail().trim();
    }

    private User.Role parseRole(String role) {
        for (User.Role candidate : User.Role.values()) {
            if (candidate.name().equalsIgnoreCase(role)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Role tidak valid: " + role);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
