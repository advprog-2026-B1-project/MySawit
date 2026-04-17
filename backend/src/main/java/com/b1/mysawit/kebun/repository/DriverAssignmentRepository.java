package com.b1.mysawit.kebun.repository;

import com.b1.mysawit.domain.DriverAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, Long> {

    boolean existsByDriverIdAndUnassignedAtIsNull(Long driverId);

    Optional<DriverAssignment> findByDriverIdAndKebunIdAndUnassignedAtIsNull(Long driverId, Long kebunId);
}