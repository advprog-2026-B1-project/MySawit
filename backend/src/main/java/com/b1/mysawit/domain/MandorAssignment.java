package com.b1.mysawit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mandor_assignment")
public class MandorAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mandor_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User mandor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kebun_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Kebun kebun;

    @Column(name = "assigned_at")
    private OffsetDateTime assignedAt;

    @Column(name = "unassigned_at")
    private OffsetDateTime unassignedAt;
}