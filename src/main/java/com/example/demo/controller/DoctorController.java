package com.example.demo.controller;

import com.example.demo.dto.DoctorResponse;
import com.example.demo.dto.PageResponse;
import com.example.demo.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<PageResponse<DoctorResponse>> getDoctors(
            @RequestParam(required = false) String specialty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<DoctorResponse> result = doctorService.getDoctors(specialty, page, size);
        return ResponseEntity.ok(new PageResponse<>(result));
    }
}