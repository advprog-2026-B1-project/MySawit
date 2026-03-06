package com.b1.mysawit.kebun.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KebunUpdateRequest {

    // kodeKebun tidak bisa diubah sesuai spesifikasi
    private String namaKebun;

    @DecimalMin(value = "0.0", inclusive = false, message = "Luas hektare harus lebih besar dari 0")
    private BigDecimal luasHektare;

    private String koordinat;
}
