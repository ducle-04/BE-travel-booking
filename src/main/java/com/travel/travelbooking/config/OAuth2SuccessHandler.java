package com.travel.travelbooking.config;

import com.travel.travelbooking.entity.Role;
import com.travel.travelbooking.entity.User;
import com.travel.travelbooking.entity.UserStatus;
import com.travel.travelbooking.repository.RoleRepository;
import com.travel.travelbooking.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    public OAuth2SuccessHandler(UserRepository userRepository, RoleRepository roleRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(jakarta.servlet.http.HttpServletRequest request,
                                        jakarta.servlet.http.HttpServletResponse response,
                                        Authentication authentication) throws java.io.IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String provider, email, name, socialId;
        if (request.getRequestURI().contains("/google")) {
            provider = "GOOGLE";
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
            socialId = oauth2User.getAttribute("sub");
        } else if (request.getRequestURI().contains("/facebook")) {
            provider = "FACEBOOK";
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
            socialId = oauth2User.getAttribute("id").toString();
        } else {
            provider = "GITHUB";
            email = Optional.ofNullable(oauth2User.getAttribute("email"))
                    .map(Object::toString)
                    .orElse(oauth2User.getAttribute("login") + "@github.com");
            name = Optional.ofNullable(oauth2User.getAttribute("name"))
                    .map(Object::toString)
                    .orElse(oauth2User.getAttribute("login"));
            socialId = oauth2User.getAttribute("id").toString();
        }

        User user = getOrCreateUser(email, name, socialId, provider);

        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        String token = jwtUtil.generateToken(user.getUsername(), roles);

        String redirectUrl = "http://localhost:5173/oauth2/callback?token=" + token
                + "&email=" + URLEncoder.encode(email != null ? email : "", StandardCharsets.UTF_8)
                + "&fullname=" + URLEncoder.encode(name != null ? name : "", StandardCharsets.UTF_8)
                + "&provider=" + provider;

        response.sendRedirect(redirectUrl);
    }

    @Transactional
    public User getOrCreateUser(String email, String name, String socialId, String provider) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(email.split("@")[0]);
                    user.setEmail(email);
                    user.setFullname(name);
                    user.setStatus(UserStatus.ACTIVE);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setProvider(provider);
                    user.setSocialId(socialId);
                    user.setPhoneNumber("");
                    user.setRoles(getOrCreateRoles("USER"));
                    return userRepository.save(user);
                });
    }

    @Transactional
    private Set<Role> getOrCreateRoles(String... roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(roleName)));
            roles.add(role);
        }
        return roles;
    }
}
