package com.b1.mysawit.auth.service;

import com.b1.mysawit.domain.User;
import com.b1.mysawit.domain.WorkerAssignment;
import com.b1.mysawit.repository.UserRepository;
import com.b1.mysawit.repository.WorkerAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WorkerAssignmentRepository assignmentRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id, Long currentAdminId) {
        if (id.equals(currentAdminId)) {
            throw new IllegalArgumentException("Admin utama tidak dapat menghapus dirinya sendiri.");
        }
        userRepository.deleteById(id);
    }

    public void assignWorkerToMandor(Long workerId, Long mandorId) {
        User worker = userRepository.findById(workerId).orElseThrow();
        User mandor = userRepository.findById(mandorId).orElseThrow();

        // reassignment logic
        assignmentRepository.findByWorkerIdAndUnassignedAtIsNull(workerId).ifPresent(existingAssignment -> {
            existingAssignment.setUnassignedAt(OffsetDateTime.now());
            assignmentRepository.save(existingAssignment);
        });

        WorkerAssignment newAssignment = new WorkerAssignment();
        newAssignment.setWorker(worker);
        newAssignment.setMandor(mandor);
        newAssignment.setAssignedAt(OffsetDateTime.now());
        assignmentRepository.save(newAssignment);
    }
}