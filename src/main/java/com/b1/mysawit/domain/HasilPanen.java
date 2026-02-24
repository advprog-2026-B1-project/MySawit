package com.b1.mysawit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "hasil_panen",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hasil_panen_worker_date",
                        columnNames = {"worker_id", "tanggal_panen"}
                )
        }
)
public class HasilPanen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker;

    @ManyToOne
    @JoinColumn(name = "kebun_id")
    private Kebun kebun;

    @Column(name = "tanggal_panen", nullable = false)
    private LocalDate tanggalPanen;

    private BigDecimal kilogram;
    private String berita;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String rejectionReason;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public enum Status {
        Pending, Approved, Rejected
    }

    // getters & setters
}