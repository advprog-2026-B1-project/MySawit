package com.b1.mysawit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Kebun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kodeKebun;
    private String namaKebun;
    private BigDecimal luasHektare;

    @Column(columnDefinition = "JSON")
    private String koordinat;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // getters & setters
}