package com.b1.mysawit.kebun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KebunUpdateRequest {

    // kodeKebun tidak bisa diubah sesuai spesifikasi
    private String namaKebun;

    private String koordinat;
}
