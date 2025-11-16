package com.travel.travelbooking.Service;

import com.travel.travelbooking.Dto.UserDTO;
import com.travel.travelbooking.Entity.User;
import com.travel.travelbooking.Entity.UserStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserService {

    // 1. Đăng ký người dùng mới
    User registerUser(String username, String password, String email, String fullname, String phoneNumber, String... roleNames);

    // 2. Lấy tất cả user (lọc theo role, loại DELETED)
    List<User> getAllUsers(String role);

    // 3. Lấy user theo ID
    Optional<User> getUserById(Long id);

    // 4. Lấy user theo username
    User findByUsername(String username);

    // 5. Xóa mềm user
    boolean softDeleteUser(Long id);

    // 6. Admin tạo user/staff
    User createUser(UserDTO dto);

    // 7. Admin cập nhật staff
    User updateUser(String username, UserDTO dto);

    // 8. User tự cập nhật hồ sơ
    User updateOwnProfile(String username, UserDTO dto);

    // 9. Admin thay đổi trạng thái
    User changeStatus(String username, String status);

    // 10. Upload avatar
    User uploadAvatar(String username, MultipartFile file) throws IOException;

    // 11. Xóa avatar
    User deleteAvatar(String username);
}