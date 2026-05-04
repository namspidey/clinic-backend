package com.example.demo.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USERNAME_EXISTED(400,    "Username đã tồn tại"),
    INVALID_CREDENTIALS(401, "Sai tài khoản hoặc mật khẩu"),
    FORBIDDEN(403,           "Không có quyền thực hiện"),
    USER_NOT_FOUND(404,      "Không tìm thấy user"),
    DOCTOR_NOT_FOUND(404,    "Không tìm thấy bác sĩ"),
    BOOKING_NOT_FOUND(404,   "Không tìm thấy lịch đặt"),
    TIME_SLOT_TAKEN(409,     "Khung giờ này đã có người đặt"),
    INVALID_TIME_RANGE(400,  "Thời gian không hợp lệ"),
    PAST_TIME_NOT_ALLOWED(400,"Không thể đặt lịch trong quá khứ"),
    CANNOT_CANCEL(400,       "Chỉ có thể hủy lịch đang ở trạng thái BOOKED");

    private final int    httpStatus;
    private final String message;
}
