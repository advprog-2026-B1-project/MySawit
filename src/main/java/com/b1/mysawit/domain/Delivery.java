package com.b1.mysawit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne
    @JoinColumn(name = "mandor_id")
    private User mandor;

    @ManyToOne
    @JoinColumn(name = "hasil_panen_id")
    private HasilPanen hasilPanen;

    @Enumerated(EnumType.STRING)
    private Status status;

    private BigDecimal rejectedKg;
    private String rejectionReason;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public enum Status {
        Memuat, Mengirim, Tiba, Rejected
    }
}