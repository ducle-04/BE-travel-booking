package com.travel.travelbooking.Dto;

import com.travel.travelbooking.Entity.UserStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDTO {

    @Positive(message = "ID phải là số dương")
    private Long id;

    @Size(min = 3, max = 50, message = "Tên người dùng phải từ 3 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Tên người dùng chỉ được chứa chữ cái, số, dấu gạch dưới hoặc gạch ngang")
    private String username;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    private String password;

    @NotBlank(message = "Tên đầy đủ không được để trống")
    @Size(min = 2, max = 100, message = "Tên đầy đủ phải từ 2 đến 100 ký tự")
    private String fullname;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,12}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotNull(message = "Trạng thái không được để trống")
    private UserStatus status = UserStatus.ACTIVE; // ACTIVE, INACTIVE, BANNED, DELETED

    private LocalDateTime createdAt;

    private String avatarUrl;

    private Set<String> roles;

    public UserDTO() {}

    public UserDTO(Long id, String username, String email, String password, String fullname,
                   String phoneNumber, UserStatus status, LocalDateTime createdAt, Set<String> roles, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.roles = roles;
        this.avatarUrl = avatarUrl;
    }
}