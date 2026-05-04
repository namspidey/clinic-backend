package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.BookingRequest;
import com.example.demo.dto.BookingResponse;
import com.example.demo.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @RequestAttribute Long patientId,
            @RequestBody @Valid BookingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(patientId, req));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(
            @RequestAttribute Long patientId,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(patientId, id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> myBookings(
            @RequestAttribute Long patientId) {
        return ResponseEntity.ok(bookingService.getMyBookings(patientId));
    }

    @GetMapping("/doctor")
    public ResponseEntity<List<BookingResponse>> doctorBookings(
            @RequestAttribute Long doctorId) {
        return ResponseEntity.ok(bookingService.getDoctorBookings(doctorId));
    }

    @GetMapping("/doctor/{doctorId}/slots")
    public ResponseEntity<List<Integer>> getBookedSlots(
            @PathVariable Long doctorId,
            @RequestParam String date) {
        return ResponseEntity.ok(
                bookingService.getBookedHours(doctorId, LocalDate.parse(date)));
    }

    @PatchMapping("/{id}/done")
    public ResponseEntity<BookingResponse> markDone(
            @RequestAttribute Long userId, // dùng userId thay vì doctorId
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.markDone(userId, id));
    }
}
