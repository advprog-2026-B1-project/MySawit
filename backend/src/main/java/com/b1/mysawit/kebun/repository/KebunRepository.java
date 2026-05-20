package com.b1.mysawit.kebun.repository;

import com.b1.mysawit.domain.Kebun;
import com.b1.mysawit.kebun.dto.KebunKoordinatProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KebunRepository extends JpaRepository<Kebun, Long> {

    boolean existsByKodeKebun(String kodeKebun);

    boolean existsByKodeKebunAndIdNot(String kodeKebun, Long id);

    List<Kebun> findByNamaKebunContainingIgnoreCase(String namaKebun);

    List<Kebun> findByKodeKebunContainingIgnoreCase(String kodeKebun);

    List<Kebun> findByNamaKebunContainingIgnoreCaseAndKodeKebunContainingIgnoreCase(
            String namaKebun, String kodeKebun);

    /**
     * Mengambil hanya id dan koordinat dari SEMUA kebun.
     * Digunakan saat CREATE untuk validasi overlap.
     * Hanya select 2 kolom — efisien, tidak trigger lazy-load relasi.
     */
    @Query("SELECT k.id as id, k.koordinat as koordinat FROM Kebun k")
    List<KebunKoordinatProjection> findAllKoordinat();

    /**
     * Mengambil hanya id dan koordinat dari semua kebun KECUALI kebun dengan id tertentu.
     * Digunakan saat UPDATE untuk memvalidasi overlap, dengan mengecualikan diri sendiri.
     */
    @Query("SELECT k.id as id, k.koordinat as koordinat FROM Kebun k WHERE k.id <> :excludeId")
    List<KebunKoordinatProjection> findAllKoordinatExcluding(@Param("excludeId") Long excludeId);
}
