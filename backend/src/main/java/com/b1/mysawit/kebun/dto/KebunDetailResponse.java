package com.b1.mysawit.kebun.dto;

import com.b1.mysawit.auth.facade.UserSummary;
import com.b1.mysawit.domain.Kebun;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KebunDetailResponse {

    private Long id;
    private String kodeKebun;
    private String namaKebun;
    private BigDecimal luasHektare;
    private String koordinat;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private UserSummary mandor;
    private List<UserSummary> supirList;

    public static KebunDetailResponse fromKebun(Kebun kebun) {
        return KebunDetailResponse.builder()
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
