package com.b1.mysawit.domain;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MandorDetail {

    @Id
    private Long userId;

    private String nomorSertifikasi;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User mandor;

    // getters & setters
}