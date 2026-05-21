package com.b1.mysawit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "kebun")
public class Kebun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kode_kebun", nullable = false, unique = true)
    private String kodeKebun;

    @Column(name = "nama_kebun", nullable = false)
    private String namaKebun;

    @Column(name = "luas_hektare", nullable = false, precision = 10, scale = 2)
    private BigDecimal luasHektare;

    @Column(columnDefinition = "TEXT")
    private String koordinat;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "kebun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MandorAssignment> mandorAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "kebun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DriverAssignment> driverAssignments = new ArrayList<>();
}