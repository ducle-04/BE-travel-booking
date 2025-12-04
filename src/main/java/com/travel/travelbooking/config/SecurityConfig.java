package com.travel.travelbooking.config;

import com.travel.travelbooking.repository.RoleRepository;
import com.travel.travelbooking.repository.UserRepository;
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
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(UserRepository userRepository,
                          RoleRepository roleRepository,
                          JwtUtil jwtUtil,
                          OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    // Load thông tin user từ DB khi login bằng username/password
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            var user = userRepository.findByUsername(username);
            if (user == null)
                throw new UsernameNotFoundException("User not found");
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword() != null ? user.getPassword() : "")
                    .roles(user.getRoles().stream().map(r -> r.getName()).toArray(String[]::new))
                    .build();
        };
    }

    // JWT Filter xử lý xác thực token
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService());
    }

    // Mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cấu hình Spring Security chính
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Các endpoint public
                        .requestMatchers("/api/auth/**", "/api/oauth2/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/destinations", "/api/destinations/{id}",
                                "/api/destinations/search", "/api/destinations/region").permitAll()
                        .requestMatchers("/api/tours", "/api/tours/**").permitAll()
                        .requestMatchers("/api/hotels", "/api/hotels/{id}", "/api/hotels/search").permitAll()
                        .requestMatchers("/api/blogs", "/api/blogs/{id}", "/api/blogs/{id}/related", "/api/blogs/{id}/comments").permitAll()
                        .requestMatchers("/api/tour-categories/active").permitAll()
                        .requestMatchers("/api/tour-categories").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/bookings").permitAll()


                        // Các endpoint yêu cầu quyền
                        .requestMatchers("/api/user/profile").authenticated()
                        .requestMatchers("/api/user/avatar").authenticated()
                        .requestMatchers("/api/user/{username}", "/api/user/list").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/user/create", "/api/user/staff/update/{username}",
                                "/api/user/delete/{id}", "/api/user/status/{username}").hasRole("ADMIN")
                        .requestMatchers("/api/destinations/**", "/api/tours/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/hotels/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/blogs", "/api/blogs/{id}/comments").authenticated()
                        .requestMatchers("/api/blogs/{id}/approve", "/api/blogs/{id}/reject").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/admin/dashboard/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/tour-categories",
                                "/api/tour-categories/**").hasAnyRole("ADMIN", "STAFF")


                        // Còn lại: yêu cầu xác thực
                        .anyRequest().authenticated()
                )
                // Cấu hình đăng nhập OAuth2
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                )
                // Xử lý lỗi truy cập
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\": \"Chưa đăng nhập hoặc token không hợp lệ\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\": \"Không có quyền truy cập\"}");
                        })
                )
                // Thêm JWT filter
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cho phép frontend (Vite React) truy cập API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
