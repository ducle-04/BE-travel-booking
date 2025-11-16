package com.travel.travelbooking.Controller;

import com.travel.travelbooking.Dto.UserDTO;
import com.travel.travelbooking.Entity.Role;
import com.travel.travelbooking.Entity.User;
import com.travel.travelbooking.Service.UserService;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. Lấy thông tin hồ sơ người dùng hiện tại (User, Staff, Admin)
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build(); // Không được ủy quyền
        }

        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toDTO(user));
    }

    // 2. Cập nhật hồ sơ người dùng hiện tại (User, Staff, Admin)
    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDTO dto) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        String username = userDetails.getUsername();
        User updatedUser = userService.updateOwnProfile(username, dto);
        return ResponseEntity.ok(toDTO(updatedUser));
    }


    // 3. Lấy danh sách người dùng với bộ lọc vai trò
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@RequestParam(required = false) String role) {
        List<User> users = userService.getAllUsers(role);
        List<UserDTO> dtos = users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // 4. Admin hoặc Staff xem chi tiết user
    @GetMapping("/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDTO(user));
    }

    // 5. Admin tạo User hoặc Staff
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO dto) {
        User user = userService.createUser(dto);
        return ResponseEntity.ok(toDTO(user));
    }

    // 6. Admin cập nhật Staff
    @PutMapping("/staff/update/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateStaff(@PathVariable String username, @Valid @RequestBody UserDTO dto) {
        User updatedUser = userService.updateUser(username, dto);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDTO(updatedUser));
    }

    // 7. Admin xóa mềm User hoặc Staff
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.softDeleteUser(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    // 8. Admin cập nhật trạng thái User hoặc Staff
    @PutMapping("/status/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> changeStatus(@PathVariable String username, @RequestParam String status) {
        try {
            User updatedUser = userService.changeStatus(username, status);
            return ResponseEntity.ok(toDTO(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // Giá trị trạng thái không hợp lệ
        }
    }

    // 9. Upload avatar mới
    @PutMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("avatar") MultipartFile avatar) throws IOException {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        if (avatar == null || avatar.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        String username = userDetails.getUsername();
        User updatedUser = userService.uploadAvatar(username, avatar);
        return ResponseEntity.ok(toDTO(updatedUser));
    }

    // 10. Xóa avatar (đặt về null)
    @DeleteMapping("/avatar")
    public ResponseEntity<UserDTO> deleteAvatar(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        String username = userDetails.getUsername();
        User updatedUser = userService.deleteAvatar(username);
        return ResponseEntity.ok(toDTO(updatedUser));
    }

    // Helper chuyển từ Entity -> DTO
    private UserDTO toDTO(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null,
                user.getFullname(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.getCreatedAt(),
                roles,
                user.getAvatarUrl()
        );
    }
}