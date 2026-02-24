package com.b1.mysawit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MandorAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mandor_id")
    private User mandor;

    @ManyToOne
    @JoinColumn(name = "kebun_id")
    private Kebun kebun;

    private OffsetDateTime assignedAt;
    private OffsetDateTime unassignedAt;

    // getters & setters
}