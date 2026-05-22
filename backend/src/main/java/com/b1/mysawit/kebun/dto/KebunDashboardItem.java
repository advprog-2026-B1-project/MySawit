package com.b1.mysawit.kebun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KebunDashboardItem {

    private Long id;
    private String kodeKebun;
    private String namaKebun;
    private BigDecimal luasHektare;
    private Long countMandorAktif;
    private Long countSupirAktif;
}
