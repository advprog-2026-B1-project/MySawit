package com.b1.mysawit.auth;

import com.b1.mysawit.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    @Test
    void recordStyleAccessorsReturnFieldValues() {
        RegisterRequest request = new RegisterRequest(
                "buruh1", "buruh@mysawit.com", "Budi", "pass", "Buruh", "CERT-1");

        assertThat(request.username()).isEqualTo("buruh1");
        assertThat(request.email()).isEqualTo("buruh@mysawit.com");
        assertThat(request.nama()).isEqualTo("Budi");
        assertThat(request.password()).isEqualTo("pass");
        assertThat(request.role()).isEqualTo("Buruh");
        assertThat(request.nomorSertifikasiMandor()).isEqualTo("CERT-1");
    }

    @Test
    void certificationGetterFallsBackToLegacyField() {
        RegisterRequest request = new RegisterRequest();
        request.setNomorSertifikasi("CERT-LEGACY");

        assertThat(request.getNomorSertifikasiMandor()).isEqualTo("CERT-LEGACY");
        assertThat(request.nomorSertifikasiMandor()).isEqualTo("CERT-LEGACY");
    }
}
