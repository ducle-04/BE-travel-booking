package com.travel.travelbooking.Dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegisterDTO {
    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(min = 3, max = 50, message = "Tên người dùng phải từ 3 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Tên người dùng chỉ được chứa chữ cái, số, dấu gạch dưới hoặc gạch ngang")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @NotBlank(message = "Tên đầy đủ không được để trống")
    @Size(min = 2, max = 100, message = "Tên đầy đủ phải từ 2 đến 100 ký tự")
    private String fullname;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,12}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotNull(message = "Thời gian tạo không được để trống")
    private LocalDateTime createdAt;

    public RegisterDTO() {}

    public RegisterDTO(String username, String password, String email, String fullname, String phoneNumber, LocalDateTime createdAt) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullname = fullname;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }
}
