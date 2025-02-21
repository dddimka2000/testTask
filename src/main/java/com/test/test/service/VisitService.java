package com.test.test.service;

import com.test.test.dto.PageResponse;
import com.test.test.dto.PatientResponse;
import com.test.test.dto.VisitRequest;
import com.test.test.mapper.PatientMapper;
import com.test.test.persistance.entity.Doctor;
import com.test.test.persistance.entity.Patient;
import com.test.test.persistance.entity.Visit;
import com.test.test.persistance.repository.DoctorRepository;
import com.test.test.persistance.repository.PatientRepository;
import com.test.test.persistance.repository.VisitRepository;
import com.test.test.persistance.specification.PatientSpecification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class VisitService {
    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PatientMapper patientMapper;

    public void createVisit(VisitRequest request) {
        log.info("Creating visit for patient: {}, doctor: {}, start: {}, end: {}",
                request.getPatientId(), request.getDoctorId(), request.getStart(), request.getEnd());
        Doctor doctor = doctorRepository.findById(request.getDoctorId()).orElseThrow(() -> {
            log.error("Doctor with ID {} not found", request.getDoctorId());
            return new IllegalStateException("Doctor not found");
        });
        Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow();
        ZoneId doctorTimeZone = ZoneId.of(doctor.getTimezone());
        ZonedDateTime startInDoctorTz = request.getStart().atZone(doctorTimeZone);
        ZonedDateTime endInDoctorTz = request.getEnd().atZone(doctorTimeZone);
        LocalDateTime startUtc = startInDoctorTz.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endUtc = endInDoctorTz.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        List<Visit> conflicts = visitRepository.findConflictingVisits(request.getDoctorId(), startUtc, endUtc);
        if (!conflicts.isEmpty()) {
            log.warn("Conflict detected for doctor {} between {} and {}", request.getDoctorId(), startUtc, endUtc);
            throw new IllegalStateException("The time slot is already taken");
        }
        Visit visit = new Visit(null,startUtc, endUtc, patient, doctor);
        visitRepository.save(visit);
        log.info("Visit created, id= {}", visit.getId());
    }

    public PageResponse<PatientResponse> getPatients(Pageable pageable, String search, List<Long> doctorIds) {
        log.info("Starting to retrieve patients with parameters: page: {}, size: {}, search: {}, doctorIds: {}",
                pageable.getPageNumber(), pageable.getPageSize(), search, doctorIds);
        Specification<Patient> spec = PatientSpecification.withFilters(search, doctorIds);
        Page<Patient> patientsPage = patientRepository.findAll(spec, pageable);
        Page<PatientResponse> patientResponses = patientsPage.map(patientMapper::toPatientResponse);
        getFromDbPatientCounts(patientResponses);
        PageResponse<PatientResponse> response = new PageResponse<>(patientResponses.getContent()
                , patientResponses.getTotalElements());
        log.info("Successfully retrieved patients");
        return response;
    }

    private final void getFromDbPatientCounts(Page<PatientResponse> patientResponses) {
        Set<Long> doctorIdsSet = patientResponses.stream()
                .flatMap(p -> p.getLastVisits().stream())
                .map(v -> v.getDoctor().getId())
                .collect(Collectors.toSet());
        List<Object[]> resultList = visitRepository.countPatientsByDoctorIds(doctorIdsSet);
        Map<Long, Long> doctorPatientCounts = resultList.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
        patientResponses.forEach(s -> s.getLastVisits().forEach(d -> {
            Long totalPatients = doctorPatientCounts.get(d.getDoctor().getId());
            if (totalPatients != null) {
                d.getDoctor().setTotalPatients(totalPatients);
            }
        }));
    }
}
