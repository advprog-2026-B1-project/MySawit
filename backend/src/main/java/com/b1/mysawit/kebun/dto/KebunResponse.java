package com.b1.mysawit.kebun.dto;

import com.b1.mysawit.domain.Kebun;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KebunResponse {

    private Long id;
    private String kodeKebun;
    private String namaKebun;
    private BigDecimal luasHektare;
    private String koordinat;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static KebunResponse fromEntity(Kebun kebun) {
        return KebunResponse.builder()
                .id(kebun.getId())
                .kodeKebun(kebun.getKodeKebun())
                .namaKebun(kebun.getNamaKebun())
                .luasHektare(kebun.getLuasHektare())
                .koordinat(kebun.getKoordinat())
                .createdAt(kebun.getCreatedAt())
                .updatedAt(kebun.getUpdatedAt())
                .build();
    }
}
