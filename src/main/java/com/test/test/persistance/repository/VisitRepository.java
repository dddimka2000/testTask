package com.test.test.persistance.repository;

import com.test.test.persistance.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    @Query("SELECT v FROM Visit v WHERE v.doctor.id = :doctorId AND ((v.startDateTime BETWEEN :start AND :end) OR (v.endDateTime BETWEEN :start AND :end))")
    List<Visit> findConflictingVisits(Long doctorId, LocalDateTime start, LocalDateTime end);
    @Query("SELECT v.doctor.id, COUNT(DISTINCT v.patient) FROM Visit v WHERE v.doctor.id IN :doctorIds GROUP BY v.doctor.id")
    List<Object[]> countPatientsByDoctorIds(@Param("doctorIds") Set<Long> doctorIds);

}
