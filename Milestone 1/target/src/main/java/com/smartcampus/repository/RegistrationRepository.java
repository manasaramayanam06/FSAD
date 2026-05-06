package com.smartcampus.repository;

import com.smartcampus.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByEventId(Long eventId);
    List<Registration> findByStudentId(Long studentId);
    long countByEventIdAndStatus(Long eventId, String status);
}
