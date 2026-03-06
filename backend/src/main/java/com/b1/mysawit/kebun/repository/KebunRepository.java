package com.b1.mysawit.kebun.repository;

import com.b1.mysawit.domain.Kebun;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
