package com.b1.mysawit.kebun.service;

import com.b1.mysawit.kebun.dto.KebunCreateRequest;
import com.b1.mysawit.kebun.dto.KebunUpdateRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Komponen validasi bisnis untuk entitas Kebun.
 * Dipisahkan dari KebunServiceImpl agar setiap kelas hanya memiliki
 * satu tanggung jawab (Single Responsibility Principle).
 */
@Component
public class KebunValidator {

    public void validateCreateRequest(KebunCreateRequest request) {
        if (!hasValue(request.getKodeKebun())) {
            throw new IllegalArgumentException("Kode kebun tidak boleh kosong");
        }
        if (!hasValue(request.getNamaKebun())) {
            throw new IllegalArgumentException("Nama kebun tidak boleh kosong");
        }
        if (request.getLuasHektare() == null) {
            throw new IllegalArgumentException("Luas hektare tidak boleh kosong");
        }
        validateLuasHektare(request.getLuasHektare());
        if (!hasValue(request.getKoordinat())) {
            throw new IllegalArgumentException("Koordinat tidak boleh kosong");
        }
    }

    public void validateUpdateRequest(KebunUpdateRequest request) {
        if (request.getLuasHektare() != null) {
            validateLuasHektare(request.getLuasHektare());
        }
    }

    public void validateLuasHektare(BigDecimal luas) {
        if (luas.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Luas hektare harus lebih besar dari 0");
        }
    }

    public boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}
