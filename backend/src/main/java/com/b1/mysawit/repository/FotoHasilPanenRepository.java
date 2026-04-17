package com.b1.mysawit.repository;

import com.b1.mysawit.domain.FotoHasilPanen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoHasilPanenRepository extends JpaRepository<FotoHasilPanen, Long> {
    List<FotoHasilPanen> findAllByHasilPanen_Id(Long hasilPanenId);
}