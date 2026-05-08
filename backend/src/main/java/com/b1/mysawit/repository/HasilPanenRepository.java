package com.b1.mysawit.repository;

import com.b1.mysawit.domain.HasilPanen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HasilPanenRepository extends JpaRepository<HasilPanen, Long> {
    boolean existsByWorker_IdAndTanggalPanen(Long workerId, LocalDate tanggalPanen);
    List<HasilPanen> findAllByWorker_IdOrderByTanggalPanenDesc(Long workerId);

    @Query("SELECT h FROM HasilPanen h WHERE h.worker.id = :workerId " +
            "AND (CAST(:startDate AS date) IS NULL OR h.tanggalPanen >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR h.tanggalPanen <= :endDate) " +
            "AND (:status IS NULL OR h.status = :status) " +
            "ORDER BY h.tanggalPanen DESC")
    List<HasilPanen> findByWorkerIdWithFilters(
            @Param("workerId") Long workerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") HasilPanen.Status status);

    @Query("SELECT h FROM HasilPanen h " +
            "JOIN WorkerAssignment wa ON h.worker.id = wa.worker.id " +
            "WHERE wa.mandor.id = :mandorId AND wa.unassignedAt IS NULL " +
            "AND (CAST(:startDate AS date) IS NULL OR h.tanggalPanen >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR h.tanggalPanen <= :endDate) " +
            "AND (:status IS NULL OR h.status = :status) " +
            "AND (:workerName IS NULL OR LOWER(h.worker.nama) LIKE LOWER(CONCAT('%', :workerName, '%'))) " +
            "ORDER BY h.tanggalPanen DESC")
    List<HasilPanen> findForMandorWithFilters(
            @Param("mandorId") Long mandorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") HasilPanen.Status status,
            @Param("workerName") String workerName);
}