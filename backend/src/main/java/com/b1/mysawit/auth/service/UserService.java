package com.b1.mysawit.auth.service;

import com.b1.mysawit.auth.dto.RegisterRequest;
import com.b1.mysawit.auth.dto.UserResponse;
import com.b1.mysawit.common.exception.BusinessRuleViolationException;
import com.b1.mysawit.common.exception.ResourceNotFoundException;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.domain.WorkerAssignment;
import com.b1.mysawit.repository.UserRepository;
import com.b1.mysawit.repository.WorkerAssignmentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final WorkerAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       WorkerAssignmentRepository assignmentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(String nama, String email, String role) {
        return userRepository.findAll().stream()
                .filter(user -> containsIgnoreCase(user.getNama(), nama))
                .filter(user -> containsIgnoreCase(user.getEmail(), email))
                .filter(user -> role == null || role.isBlank()
                        || (user.getRole() != null && user.getRole().name().equalsIgnoreCase(role)))
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(u -> role == null || role.isBlank()
                        || (u.getRole() != null && u.getRole().name().equalsIgnoreCase(role)))
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long targetUserId, Long currentAdminId) {
        if (targetUserId == null) {
            throw new IllegalArgumentException("ID pengguna wajib diisi");
        }
        if (targetUserId.equals(currentAdminId)) {
            throw new IllegalArgumentException("Admin Utama tidak dapat menghapus akunnya sendiri");
        }

        userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", targetUserId));
        userRepository.deleteById(targetUserId);
    }

    @Transactional
    public void assignWorkerToMandor(Long workerId, Long mandorId) {
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", workerId));
        User mandor = userRepository.findById(mandorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", mandorId));

        if (worker.getRole() != User.Role.Buruh) {
            throw new BusinessRuleViolationException("User dengan id '" + workerId + "' bukan Buruh");
        }
        if (mandor.getRole() != User.Role.Mandor) {
            throw new BusinessRuleViolationException("User dengan id '" + mandorId + "' bukan Mandor");
        }

        assignmentRepository.findByWorkerIdAndUnassignedAtIsNull(workerId)
                .ifPresent(existing -> {
                    existing.setUnassignedAt(OffsetDateTime.now());
                    assignmentRepository.save(existing);
                });

        WorkerAssignment assignment = new WorkerAssignment();
        assignment.setWorker(worker);
        assignment.setMandor(mandor);
        assignment.setAssignedAt(OffsetDateTime.now());
        assignmentRepository.save(assignment);
    }

    public UserResponse updateUser(Long id, RegisterRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setNama(request.getNama());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(parseRole(request.getRole()));
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        user.setUpdatedAt(OffsetDateTime.now());

        return UserResponse.from(userRepository.save(user));
    }

    private User.Role parseRole(String role) {
        for (User.Role candidate : User.Role.values()) {
            if (candidate.name().equalsIgnoreCase(role)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Role tidak valid: " + role);
    }

    private boolean containsIgnoreCase(String actual, String expectedPart) {
        if (expectedPart == null || expectedPart.isBlank()) {
            return true;
        }
        return actual != null && actual.toLowerCase().contains(expectedPart.toLowerCase());
    }
}
