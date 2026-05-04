package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class BookingResponse {
    private Long id;
    private String doctorName;
    private String patientName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String note;
    private LocalDateTime createdAt;
}