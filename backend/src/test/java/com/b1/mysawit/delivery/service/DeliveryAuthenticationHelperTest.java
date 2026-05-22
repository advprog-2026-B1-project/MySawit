package com.b1.mysawit.delivery.service;

import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryAuthenticationHelperTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DeliveryAuthenticationHelper authenticationHelper;

    private User dummyUser;

    @BeforeEach
    void setUp() {
        dummyUser = new User();
        dummyUser.setId(10L);
        dummyUser.setEmail("test@mysawit.com");
        dummyUser.setRole(User.Role.Mandor);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_shouldReturnUser_whenPrincipalIsOAuth2User() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn("test@mysawit.com");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(userRepository.findByEmail("test@mysawit.com")).thenReturn(Optional.of(dummyUser));

        User result = authenticationHelper.getCurrentUser();

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("test@mysawit.com", result.getEmail());
    }

    @Test
    void getCurrentUser_shouldReturnUser_whenPrincipalIsUserDetails() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@mysawit.com");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail("test@mysawit.com")).thenReturn(Optional.of(dummyUser));

        User result = authenticationHelper.getCurrentUser();

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("test@mysawit.com", result.getEmail());
    }

    @Test
    void getCurrentUser_shouldReturnUser_whenPrincipalIsString() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@mysawit.com");
        when(userRepository.findByEmail("test@mysawit.com")).thenReturn(Optional.of(dummyUser));

        User result = authenticationHelper.getCurrentUser();

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("test@mysawit.com", result.getEmail());
    }

    @Test
    void getCurrentUser_shouldThrowAccessDenied_whenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> {
            authenticationHelper.getCurrentUser();
        });

        assertEquals("Authentication required", ex.getMessage());
    }

    @Test
    void getCurrentUser_shouldThrowAccessDenied_whenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> {
            authenticationHelper.getCurrentUser();
        });

        assertEquals("Authentication required", ex.getMessage());
    }

    @Test
    void getCurrentUser_shouldThrowAccessDenied_whenEmailCannotBeExtracted() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(new Object()); // Unknown principal

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> {
            authenticationHelper.getCurrentUser();
        });

        assertEquals("Could not extract email from authentication context", ex.getMessage());
    }

    @Test
    void getCurrentUser_shouldThrowAccessDenied_whenUserNotFoundInDatabase() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@mysawit.com");
        when(userRepository.findByEmail("test@mysawit.com")).thenReturn(Optional.empty());

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> {
            authenticationHelper.getCurrentUser();
        });

        assertEquals("Authenticated user not found in database", ex.getMessage());
    }
}
