package com.b1.mysawit.auth.service;

import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // ambil data penting dari Google
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // jika user belum ada di database MySawit, otomatis didaftarkan
        userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setNama(name);
            newUser.setUsername(email.split("@")[0]); // generate username dari email
            
            // set default role ke buruh, admin bisa ganti
            newUser.setRole(User.Role.Buruh); 
            newUser.setCreatedAt(OffsetDateTime.now());
            
            return userRepository.save(newUser);
        });

        return oAuth2User;
    }
}