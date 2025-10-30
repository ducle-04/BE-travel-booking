package com.travel.travelbooking.Config;

import com.travel.travelbooking.Entity.Role;
import com.travel.travelbooking.Entity.User;
import com.travel.travelbooking.Entity.UserStatus;
import com.travel.travelbooking.Repository.RoleRepository;
import com.travel.travelbooking.Repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    public SecurityConfig(UserRepository userRepository, RoleRepository roleRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
    }

    // Lấy thông tin người dùng từ DB
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username);
            if (user == null)
                throw new UsernameNotFoundException("User not found");
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword() != null ? user.getPassword() : "")
                    .roles(user.getRoles().stream().map(Role::getName).toArray(String[]::new))
                    .build();
        };
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Xử lý thành công OAuth2 (Google, Facebook, GitHub)
    @Bean
    public AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            String provider;
            String email;
            String name;
            String socialId;

            // 🔹 Xác định nhà cung cấp (Provider)
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
                Object emailObj = oauth2User.getAttribute("email");
                Object nameObj = oauth2User.getAttribute("name");
                Object loginObj = oauth2User.getAttribute("login");
                Object idObj = oauth2User.getAttribute("id");

                // GitHub có thể không trả email hoặc name
                email = emailObj != null ? emailObj.toString()
                        : (loginObj != null ? loginObj.toString() + "@github.com" : "unknown@github.com");
                name = nameObj != null ? nameObj.toString()
                        : (loginObj != null ? loginObj.toString() : "GitHub User");
                socialId = idObj != null ? idObj.toString() : UUID.randomUUID().toString();
            }

            // 🔹 Tạo hoặc lấy user từ DB
            User user = getOrCreateUser(email, name, socialId, provider);

            // 🔹 Tạo JWT Token
            Set<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            String token = jwtUtil.generateToken(user.getUsername(), roles);

            // 🔹 Redirect về Frontend
            String redirectUrl = "http://localhost:5173/oauth2/callback?token=" + token
                    + "&email=" + URLEncoder.encode(email != null ? email : "", StandardCharsets.UTF_8)
                    + "&fullname=" + URLEncoder.encode(name != null ? name : "", StandardCharsets.UTF_8)
                    + "&provider=" + provider;

            response.sendRedirect(redirectUrl);
        };
    }

    //  Tạo hoặc lấy User
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

    //  Tạo Role nếu chưa có
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

    //  Cấu hình bảo mật tổng thể
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/oauth2/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/user/profile").authenticated()
                        .requestMatchers("/api/user/{username}").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/user/list").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/user/create").hasRole("ADMIN")
                        .requestMatchers("/api/user/staff/update/{username}").hasRole("ADMIN")
                        .requestMatchers("/api/user/delete/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/user/status/{username}").hasRole("ADMIN")
                        .requestMatchers("/api/destinations", "/api/destinations/{id}",
                                "/api/destinations/search", "/api/destinations/region").permitAll()
                        .requestMatchers("/api/destinations/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/tours", "/api/tours/{id}", "/api/tours/search",
                                "/api/tours/destination/{destinationId}").permitAll()
                        .requestMatchers("/api/tours/**").hasAnyRole("ADMIN", "STAFF")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler())
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((req, res, authEx) -> {
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\": \"Chưa đăng nhập hoặc token không hợp lệ\"}");
                        })
                        .accessDeniedHandler((req, res, accessEx) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\": \"Không có quyền truy cập\"}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cho phép CORS kết nối từ frontend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
