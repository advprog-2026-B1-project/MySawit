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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNomorSertifikasi() {
        return nomorSertifikasi;
    }

    public void setNomorSertifikasi(String nomorSertifikasi) {
        this.nomorSertifikasi = nomorSertifikasi;
    }

    public User getMandor() {
        return mandor;
    }

    public void setMandor(User mandor) {
        this.mandor = mandor;
    }
}