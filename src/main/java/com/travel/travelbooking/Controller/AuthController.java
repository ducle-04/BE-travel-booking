package com.travel.travelbooking.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.travel.travelbooking.Config.JwtUtil;
import com.travel.travelbooking.Dto.LoginDTO;
import com.travel.travelbooking.Dto.RegisterDTO;
import com.travel.travelbooking.Entity.User;
import com.travel.travelbooking.Entity.UserStatus;
import com.travel.travelbooking.Service.UserService;

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
        return ResponseEntity.ok("Đăng ký người dùng thành công");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getUsername());

            // Kiểm tra mật khẩu
            if (!passwordEncoder.matches(loginDTO.getPassword(), userDetails.getPassword())) {
                return ResponseEntity.status(401).body("Thông tin đăng nhập không hợp lệ");
            }

            // Kiểm tra trạng thái tài khoản
            User user = userService.findByUsername(loginDTO.getUsername());
            if (user == null || user.getStatus() != UserStatus.ACTIVE) {
                return ResponseEntity.status(403).body("Tài khoản không hoạt động");
            }

            // Tạo token và lấy danh sách roles
            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toSet());
            String token = jwtUtil.generateToken(loginDTO.getUsername(), roles);

            // Tạo phản hồi chứa token, username và roles
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", loginDTO.getUsername());
            response.put("roles", roles);

            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(401).body("Thông tin đăng nhập không hợp lệ");
        }
    }
}