package com.b1.mysawit.kebun.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReassignMandorRequest {

    @NotNull(message = "mandorId tidak boleh kosong")
    private Long mandorId;

    @NotNull(message = "oldKebunId tidak boleh kosong")
    private Long oldKebunId;

    @NotNull(message = "newKebunId tidak boleh kosong")
    private Long newKebunId;
}