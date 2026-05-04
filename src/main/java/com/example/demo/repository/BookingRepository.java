package com.example.demo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Check conflict: tìm booking BOOKED của doctor bị overlap với [start, end]
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.doctor.id = :doctorId
          AND b.status = 'BOOKED'
          AND b.startTime < :endTime
          AND b.endTime > :startTime
        """)
    boolean existsConflict(
        @Param("doctorId")  Long doctorId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime")   LocalDateTime endTime
    );

    // Lịch của patient
    List<Booking> findByPatientIdOrderByStartTimeDesc(Long patientId);

    // Lịch của doctor
    List<Booking> findByDoctorIdOrderByStartTimeDesc(Long doctorId);

    // BookingRepository.java
@Query("""
    SELECT b.startTime FROM Booking b
    WHERE b.doctor.id = :doctorId
      AND DATE(b.startTime) = :date
      AND b.status = 'BOOKED'
    """)
List<LocalDateTime> findBookedTimesByDoctorAndDate(
    @Param("doctorId") Long doctorId,
    @Param("date") LocalDate date
);
}
