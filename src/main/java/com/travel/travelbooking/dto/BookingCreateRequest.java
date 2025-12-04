package com.travel.travelbooking.dto;

import com.travel.travelbooking.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingCreateRequest {

    @NotNull(message = "Thiếu tourId")
    private Long tourId;

    @NotNull(message = "Vui lòng chọn ngày khởi hành")
    private LocalDate startDate;

    private String transportName; // tên phương tiện (có thể null → mặc định 0đ)

    @NotNull(message = "Vui lòng nhập số lượng người")
    @Min(1)
    private Integer numberOfPeople;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;


    // Chỉ bắt buộc khi chưa đăng nhập
    private String contactName;
    private String contactEmail;
    private String contactPhone;

    private String note;
}