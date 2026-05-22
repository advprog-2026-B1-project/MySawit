package com.b1.mysawit.auth;

import com.b1.mysawit.auth.service.CustomUserDetailsService;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_Success() {
        User user = new User();
        user.setEmail("admin@test.com");
        user.setPasswordHash("hashed_pw");
        user.setRole(User.Role.Admin);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("admin@test.com");

        assertThat(result.getUsername()).isEqualTo("admin@test.com");
        assertThat(result.getPassword()).isEqualTo("hashed_pw");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("Admin");
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("notfound@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("notfound@test.com");
    }
}
