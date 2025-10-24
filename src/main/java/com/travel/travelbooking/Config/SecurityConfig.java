package com.travel.travelbooking.Config;

import com.travel.travelbooking.Entity.Role;
import com.travel.travelbooking.Repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public SecurityConfig(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.travel.travelbooking.Entity.User user = userRepository.findByUsername(username);
            if (user == null) throw new UsernameNotFoundException("User not found");
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Cho phép tất cả truy cập vào các API auth
                        .requestMatchers("/api/user/profile").authenticated() // Yêu cầu xác thực cho API hồ sơ
                        .requestMatchers("/api/user/{username}").hasAnyRole("ADMIN", "STAFF") // Chỉ ADMIN hoặc STAFF xem chi tiết user
                        .requestMatchers("/api/user/list").hasAnyRole("ADMIN", "STAFF") // Chỉ ADMIN hoặc STAFF truy cập API danh sách người dùng
                        .requestMatchers("/api/user/create").hasRole("ADMIN") // Chỉ ADMIN tạo user
                        .requestMatchers("/api/user/staff/update/{username}").hasRole("ADMIN") // Chỉ ADMIN cập nhật staff
                        .requestMatchers("/api/user/delete/{id}").hasRole("ADMIN") // Chỉ ADMIN xóa mềm user
                        .requestMatchers("/api/user/status/{username}").hasRole("ADMIN") // Chỉ ADMIN cập nhật trạng thái
                        .requestMatchers("/api/destinations", "/api/destinations/{id}", "/api/destinations/search", "/api/destinations/region").permitAll()
                        .requestMatchers("/api/destinations/**").hasAnyRole("ADMIN", "STAFF") // Yêu cầu ADMIN hoặc STAFF cho POST, PUT, DELETE
                        .requestMatchers("/api/tours", "/api/tours/{id}", "/api/tours/search", "/api/tours/destination/{destinationId}").permitAll()
                        .requestMatchers("/api/tours/**").hasAnyRole("ADMIN", "STAFF")
                        // Tất cả các yêu cầu khác cần xác thực
                        .anyRequest().authenticated() // Tất cả các yêu cầu khác cần xác thực
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Chưa đăng nhập hoặc token không hợp lệ\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Không có quyền truy cập\"}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}