package com.b1.mysawit.auth;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.b1.mysawit.auth.service.CustomOAuth2UserService;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void testProcessOAuth2User_NewUser() {
        when(oAuth2User.getAttribute("email")).thenReturn("pekerjabaru@gmail.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Pekerja Baru");
        when(userRepository.findByEmail("pekerjabaru@gmail.com")).thenReturn(Optional.empty());

        OAuth2User result = customOAuth2UserService.processOAuth2User(oAuth2User);

        assertEquals(oAuth2User, result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testProcessOAuth2User_ExistingUser() {
        when(oAuth2User.getAttribute("email")).thenReturn("pekerjaLama@gmail.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Pekerja Lama");
        
        when(userRepository.findByEmail("pekerjaLama@gmail.com")).thenReturn(Optional.of(new User()));

        OAuth2User result = customOAuth2UserService.processOAuth2User(oAuth2User);

        assertEquals(oAuth2User, result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testProcessOAuth2User_MissingEmail() {
        when(oAuth2User.getAttribute("email")).thenReturn(null);

        assertThatThrownBy(() -> customOAuth2UserService.processOAuth2User(oAuth2User))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }
}
