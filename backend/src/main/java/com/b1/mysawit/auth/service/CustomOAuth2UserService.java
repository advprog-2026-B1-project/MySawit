package com.b1.mysawit.auth.service;

import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        User user = findOrCreateOAuthUser(oAuth2User);

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole().name())),
                oAuth2User.getAttributes(),
                "email"
        );
    }

    public OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        findOrCreateOAuthUser(oAuth2User);
        return oAuth2User;
    }

    private User findOrCreateOAuthUser(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email dari Google tidak tersedia");
        }

        return userRepository.findByEmail(email).orElseGet(() -> {
            OffsetDateTime now = OffsetDateTime.now();
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setNama(name);
            newUser.setUsername(email.split("@")[0]);
            newUser.setPasswordHash("");
            newUser.setRole(User.Role.Buruh);
            newUser.setCreatedAt(now);
            newUser.setUpdatedAt(now);
            return userRepository.save(newUser);
        });
    }
}
