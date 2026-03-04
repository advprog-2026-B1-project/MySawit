package com.b1.mysawit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") // PostgreSQL table name
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String nama;
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    private BigDecimal saldo = BigDecimal.ZERO;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public enum Role {
        Buruh, Mandor, Supir, Admin
    }

    // getters & setters
}