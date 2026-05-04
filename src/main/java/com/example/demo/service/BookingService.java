package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.BookingRequest;
import com.example.demo.dto.BookingResponse;
import com.example.demo.entity.Booking;
import com.example.demo.entity.User;
import com.example.demo.enums.BookingStatus;
import com.example.demo.enums.Role;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponse createBooking(Long patientId, BookingRequest req) {

        // 1. Validate thời gian
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new AppException(ErrorCode.INVALID_TIME_RANGE);
        }
        if (req.getStartTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.PAST_TIME_NOT_ALLOWED);
        }

        // 2. Lock row doctor — chặn 2 request đồng thời cùng doctor
        // Request thứ 2 sẽ block ở đây cho đến khi transaction thứ 1 commit/rollback
        User doctor = userRepository.findByIdWithLock(req.getDoctorId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        if (doctor.getRole() != Role.DOCTOR) {
            throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
        }

        // 3. Check conflict SAU KHI đã lock — an toàn, không race condition
        boolean conflict = bookingRepository.existsConflict(
                req.getDoctorId(), req.getStartTime(), req.getEndTime());

        if (conflict) {
            throw new AppException(ErrorCode.TIME_SLOT_TAKEN);
        }

        // 4. Tạo booking
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Booking booking = new Booking();
        booking.setPatient(patient);
        booking.setDoctor(doctor);
        booking.setStartTime(req.getStartTime());
        booking.setEndTime(req.getEndTime());
        booking.setStatus(BookingStatus.BOOKED);
        booking.setNote(req.getNote());

        bookingRepository.save(booking);

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long patientId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getPatient().getId().equals(patientId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        if (booking.getStatus() != BookingStatus.BOOKED) {
            throw new AppException(ErrorCode.CANNOT_CANCEL);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(booking);
    }

    public List<BookingResponse> getMyBookings(Long patientId) {
        return bookingRepository.findByPatientIdOrderByStartTimeDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    public List<BookingResponse> getDoctorBookings(Long doctorId) {
        return bookingRepository.findByDoctorIdOrderByStartTimeDesc(doctorId)
                .stream().map(this::toResponse).toList();
    }

    private BookingResponse toResponse(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .doctorName(b.getDoctor().getFullName())
                .patientName(b.getPatient().getFullName())
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .status(b.getStatus().name())
                .note(b.getNote())
                .createdAt(b.getCreatedAt())
                .build();
    }

    public List<Integer> getBookedHours(Long doctorId, LocalDate date) {
        return bookingRepository
                .findBookedTimesByDoctorAndDate(doctorId, date)
                .stream()
                .map(LocalDateTime::getHour)
                .toList();
    }

    @Transactional
    public BookingResponse markDone(Long doctorId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getDoctor().getId().equals(doctorId))
            throw new AppException(ErrorCode.FORBIDDEN);

        if (booking.getStatus() != BookingStatus.BOOKED)
            throw new AppException(ErrorCode.CANNOT_CANCEL);

        booking.setStatus(BookingStatus.DONE);
        return toResponse(booking);
    }
}