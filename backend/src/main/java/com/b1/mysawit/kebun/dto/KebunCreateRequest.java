package com.b1.mysawit.kebun.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KebunCreateRequest {

    @NotBlank(message = "Kode kebun tidak boleh kosong")
    private String kodeKebun;

    @NotBlank(message = "Nama kebun tidak boleh kosong")
    private String namaKebun;

    @NotBlank(message = "Koordinat tidak boleh kosong")
    private String koordinat;
}
