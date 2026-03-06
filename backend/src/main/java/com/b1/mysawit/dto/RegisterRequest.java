package com.b1.mysawit.dto;

public record RegisterRequest(
    String username,
    String email,
    String nama,
    String password,
    String role,
    String nomorSertifikasiString
) {}
