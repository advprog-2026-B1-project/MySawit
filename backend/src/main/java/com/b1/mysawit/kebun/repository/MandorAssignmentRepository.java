package com.b1.mysawit.kebun.repository;

import com.b1.mysawit.domain.MandorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MandorAssignmentRepository extends JpaRepository<MandorAssignment, Long> {

    /**
     * Cek apakah kebun masih memiliki Mandor aktif (unassignedAt == null).
     * Digunakan untuk validasi sebelum menghapus kebun.
     */
    boolean existsByKebunIdAndUnassignedAtIsNull(Long kebunId);

    boolean existsByMandorIdAndUnassignedAtIsNull(Long mandorId);

    Optional<MandorAssignment> findByMandorIdAndKebunIdAndUnassignedAtIsNull(Long mandorId, Long kebunId);
}
