package com.b1.mysawit.kebun.dto;

/**
 * Spring Data JPA closed projection untuk mengambil hanya id dan koordinat dari tabel kebun.
 * Menghindari loading seluruh entity + relasi lazy — kritis untuk NFR response time < 500ms.
 */
public interface KebunKoordinatProjection {
    Long getId();
    String getKoordinat();
}
