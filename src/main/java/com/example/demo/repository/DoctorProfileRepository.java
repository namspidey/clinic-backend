package com.example.demo.repository;

import com.example.demo.entity.DoctorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    @Query("""
        SELECT dp FROM DoctorProfile dp
        JOIN FETCH dp.user u
        WHERE u.status = 1
          AND (:specialty IS NULL OR LOWER(dp.specialty) LIKE LOWER(CONCAT('%', :specialty, '%')))
        """)
    Page<DoctorProfile> findDoctors(@Param("specialty") String specialty, Pageable pageable);
}