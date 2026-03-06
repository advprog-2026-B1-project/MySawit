package com.b1.mysawit.auth.dto;

public record RegisterRequest(
    String username,
    String email,
    String nama,
    String password,
    String role,
    String nomorSertifikasi
) {}
