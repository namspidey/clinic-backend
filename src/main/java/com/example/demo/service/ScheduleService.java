package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.ScheduleResponse;
import com.example.demo.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public List<ScheduleResponse> getDoctorSchedules(Long doctorId) {
        return scheduleRepository
                .findByDoctorIdAndWorkDateGreaterThanEqual(doctorId, LocalDate.now())
                .stream()
                .map(s -> ScheduleResponse.builder()
                        .id(s.getId())
                        .workDate(s.getWorkDate())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .build())
                .toList();
    }
}