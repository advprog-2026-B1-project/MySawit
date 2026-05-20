package com.b1.mysawit.kebun.service;

import com.b1.mysawit.domain.Kebun;
import com.b1.mysawit.kebun.dto.KebunCreateRequest;
import com.b1.mysawit.kebun.dto.KebunResponse;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Komponen mapper untuk konversi antara entitas Kebun dan DTO.
 * Dipisahkan dari KebunServiceImpl agar setiap kelas hanya memiliki
 * satu tanggung jawab (Single Responsibility Principle).
 */
@Component
public class KebunMapper {

    public Kebun toEntity(KebunCreateRequest request) {
        return Kebun.builder()
                .kodeKebun(request.getKodeKebun().trim())
                .namaKebun(request.getNamaKebun().trim())
                .luasHektare(request.getLuasHektare())
                .koordinat(request.getKoordinat())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public KebunResponse toResponse(Kebun kebun) {
        return KebunResponse.fromEntity(kebun);
    }
}
