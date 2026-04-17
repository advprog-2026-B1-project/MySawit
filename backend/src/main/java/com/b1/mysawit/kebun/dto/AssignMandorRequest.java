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
public class AssignMandorRequest {

    @NotNull(message = "mandorId tidak boleh kosong")
    private Long mandorId;

    @NotNull(message = "kebunId tidak boleh kosong")
    private Long kebunId;
}