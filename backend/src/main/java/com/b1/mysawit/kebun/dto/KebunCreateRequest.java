package com.b1.mysawit.kebun.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KebunCreateRequest {

    @NotBlank(message = "Kode kebun tidak boleh kosong")
    private String kodeKebun;

    @NotBlank(message = "Nama kebun tidak boleh kosong")
    private String namaKebun;

    @NotNull(message = "Luas hektare tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = false, message = "Luas hektare harus lebih besar dari 0")
    private BigDecimal luasHektare;

    @NotBlank(message = "Koordinat tidak boleh kosong")
    private String koordinat;
}
