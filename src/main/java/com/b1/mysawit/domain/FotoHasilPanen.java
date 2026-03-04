package com.b1.mysawit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class FotoHasilPanen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hasil_panen_id")
    private HasilPanen hasilPanen;

    private String url;
    private OffsetDateTime uploadedAt;

    // getters & setters
}