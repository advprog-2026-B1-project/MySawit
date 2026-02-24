package com.b1.mysawit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DriverAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne
    @JoinColumn(name = "kebun_id")
    private Kebun kebun;

    private OffsetDateTime assignedAt;
    private OffsetDateTime unassignedAt;

    // getters & setters
}