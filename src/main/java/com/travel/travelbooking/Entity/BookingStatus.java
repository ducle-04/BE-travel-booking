package com.travel.travelbooking.Entity;

public enum BookingStatus {
    PENDING,         // Chờ xác nhận
    CONFIRMED,       // Đã xác nhận
    CANCEL_REQUEST,  // Khách yêu cầu hủy
    CANCELLED,       // Đã hủy chính thức
    REJECTED,        // Bị từ chối khi tạo
    COMPLETED,       // Đã hoàn thành (sau tour)
    DELETED          // Xóa mềm (không hiển thị)
}
