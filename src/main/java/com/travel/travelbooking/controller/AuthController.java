package com.travel.travelbooking.controller;

import com.travel.travelbooking.config.JwtUtil;
import com.travel.travelbooking.dto.LoginDTO;
import com.travel.travelbooking.dto.RegisterDTO;
import com.travel.travelbooking.entity.User;
import com.travel.travelbooking.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, JwtUtil jwtUtil, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        User user = userService.registerUser(
                registerDTO.getUsername(),
                registerDTO.getPassword(),
                registerDTO.getEmail(),
                registerDTO.getFullname(),
                registerDTO.getPhoneNumber(),
                "USER"
        );

        // TỰ ĐỘNG GÁN BOOKING GUEST → USER MỚI TẠO
        userService.attachBookingsToNewUser(user);

        return ResponseEntity.ok("Đăng ký thành công, booking trước đó đã được đồng bộ (nếu có).");
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getUsername());

            if (!passwordEncoder.matches(loginDTO.getPassword(), userDetails.getPassword())) {
                return ResponseEntity.status(401).body("Mật khẩu không đúng");
            }

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toSet());

            String token = jwtUtil.generateToken(userDetails.getUsername(), roles);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", userDetails.getUsername());
            response.put("roles", roles);

            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(401).body("Thông tin đăng nhập không hợp lệ");
        }
    }
}