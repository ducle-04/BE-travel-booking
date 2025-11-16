package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.UserDTO;
import com.travel.travelbooking.Entity.Role;
import com.travel.travelbooking.Entity.User;
import com.travel.travelbooking.Entity.UserStatus;
import com.travel.travelbooking.Repository.RoleRepository;
import com.travel.travelbooking.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    @Transactional
    public User registerUser(String username, String password, String email, String fullname, String phoneNumber, String... roleNames) {
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

    @Override
    public List<User> getAllUsers(String role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() != UserStatus.DELETED)
                .filter(user -> role == null || user.getRoles().stream()
                        .anyMatch(r -> r.getName().equalsIgnoreCase(role)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getStatus() != UserStatus.DELETED);
    }

    @Override
    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return (user != null && user.getStatus() != UserStatus.DELETED) ? user : null;
    }

    @Override
    @Transactional
    public boolean softDeleteUser(Long id) {
        return userRepository.findById(id).map(user -> {
            user.setStatus(UserStatus.DELETED);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    @Override
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

    @Override
    @Transactional
    public User updateUser(String username, UserDTO dto) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getStatus() == UserStatus.DELETED) return null;

        if (!user.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase("STAFF"))) {
            throw new RuntimeException("Chỉ có thể cập nhật thông tin của Staff");
        }

        user.setFullname(dto.getFullname());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : user.getStatus());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setRoles(getOrCreateRoles(dto.getRoles().toArray(new String[0])));

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateOwnProfile(String username, UserDTO dto) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getStatus() == UserStatus.DELETED) return null;

        user.setFullname(dto.getFullname());
        user.setPhoneNumber(dto.getPhoneNumber());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User changeStatus(String username, String status) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("User not found");
        }
        UserStatus newStatus = UserStatus.valueOf(status.toUpperCase());
        user.setStatus(newStatus);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User uploadAvatar(String username, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("User không tồn tại");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }
        String avatarUrl = cloudinaryService.uploadImage(file);
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User deleteAvatar(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null || user.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("User không tồn tại");
        }
        user.setAvatarUrl(null);
        return userRepository.save(user);
    }

    // === PRIVATE HELPER ===
    private Set<Role> getOrCreateRoles(String... roleNames) {
        return Arrays.stream(roleNames)
                .map(name -> roleRepository.findByName(name)
                        .orElseGet(() -> roleRepository.save(new Role(name))))
                .collect(Collectors.toSet());
    }
}