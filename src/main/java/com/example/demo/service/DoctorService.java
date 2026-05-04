package com.example.demo.service;

import com.example.demo.dto.DoctorResponse;
import com.example.demo.repository.DoctorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;

    public Page<DoctorResponse> getDoctors(String specialty, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("user.fullName").ascending());

        return doctorProfileRepository
                .findDoctors(specialty, pageable)
                .map(dp -> DoctorResponse.builder()
                        .id(dp.getUser().getId())
                        .fullName(dp.getUser().getFullName())
                        .phone(dp.getUser().getPhone())
                        .specialty(dp.getSpecialty())
                        .description(dp.getDescription())
                        .build());
    }
}