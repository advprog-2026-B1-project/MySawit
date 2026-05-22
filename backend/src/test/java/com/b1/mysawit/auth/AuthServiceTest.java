package com.b1.mysawit.auth;

import com.b1.mysawit.auth.dto.RegisterRequest;
import com.b1.mysawit.auth.dto.UserResponse;
import com.b1.mysawit.auth.service.AuthService;
import com.b1.mysawit.common.exception.DuplicateResourceException;
import com.b1.mysawit.domain.MandorDetail;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.MandorDetailRepository;
import com.b1.mysawit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private MandorDetailRepository mandorDetailRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    @Test
    void testRegister_Success_Buruh() {
        RegisterRequest req = new RegisterRequest(
                "buruh123", "buruh@mail.com", "Budi", "pass123", "Buruh", null
        );

        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = (User) i.getArguments()[0];
            u.setId(1L);
            return u;
        });

        UserResponse result = authService.register(req);

        assertThat(result.getRole()).isEqualTo("Buruh");
        assertThat(result.getEmail()).isEqualTo("buruh@mail.com");
        verify(mandorDetailRepository, never()).save(any());
    }

    @Test
    void testRegister_Success_Mandor() {
        RegisterRequest req = new RegisterRequest(
                "mandorhebat", "mandor@mail.com", "Mandor Kita", "pass123", "Mandor", "CERT-123"
        );

        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = (User) i.getArguments()[0];
            u.setId(1L);
            return u;
        });

        authService.register(req);

        verify(mandorDetailRepository, times(1)).save(any(MandorDetail.class));
    }

    @Test
    void testRegister_Failed_EmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest(
                "tester", "test@mail.com", "Test", "pass", "Buruh", null
        );

        when(userRepository.existsByEmail("test@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_Failed_WrongPasswordOrNotFound() {
        when(userRepository.findByEmail("notfound@mail.com")).thenReturn(Optional.empty());

        boolean isLogin = authService.login("notfound@mail.com", "pass");

        assertThat(isLogin).isFalse();
    }
}
