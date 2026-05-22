package com.b1.mysawit.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String nama;
    private String password;
    private String role;
    private String nomorSertifikasiMandor;
    private String nomorSertifikasi;

    public RegisterRequest(String username,
                           String email,
                           String nama,
                           String password,
                           String role,
                           String nomorSertifikasiMandor) {
        this.username = username;
        this.email = email;
        this.nama = nama;
        this.password = password;
        this.role = role;
        this.nomorSertifikasiMandor = nomorSertifikasiMandor;
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    public String nama() {
        return nama;
    }

    public String password() {
        return password;
    }

    public String role() {
        return role;
    }

    public String nomorSertifikasiMandor() {
        return getNomorSertifikasiMandor();
    }

    public String getNomorSertifikasiMandor() {
        if (nomorSertifikasiMandor != null && !nomorSertifikasiMandor.isBlank()) {
            return nomorSertifikasiMandor;
        }
        return nomorSertifikasi;
    }
}
