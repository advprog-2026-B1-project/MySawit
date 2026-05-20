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
public class ReassignSupirRequest {

    @NotNull(message = "supirId tidak boleh kosong")
    private Long supirId;

    @NotNull(message = "oldKebunId tidak boleh kosong")
    private Long oldKebunId;

    @NotNull(message = "newKebunId tidak boleh kosong")
    private Long newKebunId;
}