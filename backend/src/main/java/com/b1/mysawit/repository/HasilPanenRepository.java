package com.b1.mysawit.repository;

import com.b1.mysawit.domain.HasilPanen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HasilPanenRepository extends JpaRepository<HasilPanen, Long> {
    boolean existsByWorker_IdAndTanggalPanen(Long workerId, LocalDate tanggalPanen);
    List<HasilPanen> findAllByWorker_IdOrderByTanggalPanenDesc(Long workerId);
}