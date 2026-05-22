package com.b1.mysawit.auth;

import com.b1.mysawit.auth.config.AuthDataInitializer;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthDataInitializerTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @Test
    void run_DoesNothingWhenDefaultAdminAlreadyExists() {
        AuthDataInitializer initializer = new AuthDataInitializer(
                userRepository, passwordEncoder, "admin@mysawit.com", "secret");
        when(userRepository.existsByEmail("admin@mysawit.com")).thenReturn(true);

        initializer.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_CreatesDefaultAdminWhenMissing() {
        AuthDataInitializer initializer = new AuthDataInitializer(
                userRepository, passwordEncoder, "admin@mysawit.com", "secret");
        when(userRepository.existsByEmail("admin@mysawit.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");

        initializer.run(null);

        verify(userRepository).save(any(User.class));
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(admin -> {
            assertThat(admin.getEmail()).isEqualTo("admin@mysawit.com");
            assertThat(admin.getPasswordHash()).isEqualTo("hashed-secret");
            assertThat(admin.getRole()).isEqualTo(User.Role.Admin);
            assertThat(admin.getCreatedAt()).isNotNull();
            return true;
        }));
    }
}
