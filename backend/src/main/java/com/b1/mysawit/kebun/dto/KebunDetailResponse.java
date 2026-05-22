package com.b1.mysawit.kebun.dto;

import com.b1.mysawit.auth.facade.UserSummary;
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
}
