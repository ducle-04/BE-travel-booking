package com.travel.travelbooking.Service;

import com.travel.travelbooking.Entity.Role;
import com.travel.travelbooking.Entity.User;
import com.travel.travelbooking.Entity.UserStatus;
import com.travel.travelbooking.Dto.UserDTO;
import com.travel.travelbooking.Repository.RoleRepository;
import com.travel.travelbooking.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Đăng ký user mới (dành cho User thường, role mặc định là USER)
    @Transactional
    public User registerUser(String username, String password, String email,
                             String fullname, String phoneNumber, String... roleNames) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFullname(fullname);
        user.setPhoneNumber(phoneNumber);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(getOrCreateRoles(roleNames.length > 0 ? roleNames : new String[]{"USER"}));

        return userRepository.save(user);
    }

    // 2. Lấy tất cả user (trừ DELETED, phân loại theo role nếu cần)
    public List<User> getAllUsers(String role) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getStatus() != UserStatus.DELETED)
                .filter(user -> role == null || user.getRoles().stream()
                        .anyMatch(r -> r.getName().equalsIgnoreCase(role)))
                .collect(Collectors.toList());
    }

    // 3. Lấy user theo ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getStatus() != UserStatus.DELETED);
    }

    // 4. Lấy user theo username
    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getStatus() != UserStatus.DELETED) {
            return user;
        }
        return null;
    }

    // 5. Xóa mềm user (User hoặc Staff)
    @Transactional
    public boolean softDeleteUser(Long id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty()) return false;
        User user = optional.get();
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        return true;
    }

    // 6. Admin tạo user hoặc Staff từ DTO
    @Transactional
    public User createUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setFullname(dto.getFullname());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(getOrCreateRoles(dto.getRoles().toArray(new String[0])));

        return userRepository.save(user);
    }

    // 7. Admin cập nhật Staff
    @Transactional
    public User updateUser(String username, UserDTO dto) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getStatus() == UserStatus.DELETED) return null;

        // Kiểm tra user có role STAFF
        if (!user.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("STAFF"))) {
            throw new RuntimeException("Chỉ có thể cập nhật thông tin của Staff");
        }

        if (!dto.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        user.setEmail(dto.getEmail());
        user.setFullname(dto.getFullname());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : user.getStatus());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        user.setRoles(getOrCreateRoles(dto.getRoles().toArray(new String[0])));
        return userRepository.save(user);
    }

    // 8. User hoặc Staff tự sửa hồ sơ
    @Transactional
    public User updateOwnProfile(String username, UserDTO dto) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getStatus() == UserStatus.DELETED) return null;

        if (!dto.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        user.setEmail(dto.getEmail());
        user.setFullname(dto.getFullname());
        user.setPhoneNumber(dto.getPhoneNumber());

        return userRepository.save(user);
    }

    // 9. Admin cập nhật trạng thái user hoặc Staff
    @Transactional
    public User changeStatus(String username, String status) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("User not found");
        }

        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value");
        }

        user.setStatus(newStatus);
        return userRepository.save(user);
    }

    @Transactional
    private Set<Role> getOrCreateRoles(String... roleNames) {
        return Arrays.stream(roleNames)
                .map(name -> roleRepository.findByName(name)
                        .orElseGet(() -> roleRepository.save(new Role(name)))) // DÙNG new Role("USER")
                .collect(Collectors.toSet());
    }
}