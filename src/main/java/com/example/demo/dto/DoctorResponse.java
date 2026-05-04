package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String specialty;
    private String description;
}