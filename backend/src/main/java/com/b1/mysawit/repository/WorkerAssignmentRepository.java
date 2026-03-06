package com.b1.mysawit.repository;

import com.b1.mysawit.domain.WorkerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkerAssignmentRepository extends JpaRepository<WorkerAssignment, Long> {
    Optional<WorkerAssignment> findByWorkerIdAndUnassignedAtIsNull(Long workerId);
}