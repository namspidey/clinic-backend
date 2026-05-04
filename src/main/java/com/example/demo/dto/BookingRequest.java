package com.example.demo.dto;

import java.time.LocalDateTime;

import org.antlr.v4.runtime.misc.NotNull;

import lombok.Data;

@Data
public class BookingRequest {
    @NotNull private Long doctorId;
    @NotNull private LocalDateTime startTime;
    @NotNull private LocalDateTime endTime;
    private String note;
}
