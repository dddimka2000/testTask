package com.test.test.service;

import com.test.test.dto.*;
import com.test.test.mapper.PatientMapper;
import com.test.test.persistance.entity.Doctor;
import com.test.test.persistance.entity.Patient;
import com.test.test.persistance.entity.Visit;
import com.test.test.persistance.repository.DoctorRepository;
import com.test.test.persistance.repository.PatientRepository;
import com.test.test.persistance.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class VisitServiceTest {
    @Mock
    private VisitRepository visitRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private VisitService visitService;

    private VisitRequest validRequest;
    private Pageable pageable;
    private String search;
    private List<Long> doctorIds;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validRequest = new VisitRequest();
        validRequest.setPatientId(1L);
        validRequest.setDoctorId(1L);
        validRequest.setStart(LocalDateTime.of(2025, 2, 20, 10, 0));
        validRequest.setEnd(LocalDateTime.of(2025, 2, 20, 11, 0));

        pageable = PageRequest.of(0, 10);
        search = "John";
        doctorIds = Arrays.asList(1L, 2L);
    }

    @Test
    void testCreateVisit_DoctorNotFound() {
        when(doctorRepository.findById(validRequest.getDoctorId())).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> visitService.createVisit(validRequest));
    }
    @Test
    void testGetPatients_Success2() {
        DoctorResponse doctor1 = new DoctorResponse();
        doctor1.setId(1L);
        DoctorResponse doctor2 = new DoctorResponse();
        doctor2.setId(2L);

        VisitResponse visit1 = new VisitResponse();
        visit1.setDoctor(doctor1);
        VisitResponse visit2 = new VisitResponse();
        visit2.setDoctor(doctor2);

        PatientResponse response1 = new PatientResponse();
        response1.setLastVisits(Arrays.asList(visit1));

        PatientResponse response2 = new PatientResponse();
        response2.setLastVisits(Arrays.asList(visit2));

        List<PatientResponse> patientResponses = Arrays.asList(response1, response2);
        Page<PatientResponse> patientPage = new PageImpl<>(patientResponses);

        Page<Patient> patientsPage = new PageImpl<>(Arrays.asList(new Patient(), new Patient()));
        when(patientRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(patientsPage);
        when(patientMapper.toPatientResponse(any(Patient.class))).thenReturn(response1, response2);

        List<Object[]> countResults = Arrays.asList(new Object[]{1L, 5L}, new Object[]{2L, 3L});
        when(visitRepository.countPatientsByDoctorIds(anySet())).thenReturn(countResults);

        PageResponse<PatientResponse> result = visitService.getPatients(pageable, search, doctorIds);

        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals(5L, doctor1.getTotalPatients());
        assertEquals(3L, doctor2.getTotalPatients());

        verify(visitRepository, times(1)).countPatientsByDoctorIds(anySet());

        verify(patientMapper, times(2)).toPatientResponse(any(Patient.class));
    }


    @Test
    void testCreateVisit_PatientNotFound() {
        Doctor doctor = new Doctor();
        doctor.setId(validRequest.getDoctorId());
        when(doctorRepository.findById(validRequest.getDoctorId())).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(validRequest.getPatientId())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> visitService.createVisit(validRequest));
    }

    @Test
    void testCreateVisit_TimeSlotConflict() {
        Doctor doctor = new Doctor();
        doctor.setId(validRequest.getDoctorId());
        doctor.setTimezone("UTC");
        when(doctorRepository.findById(validRequest.getDoctorId())).thenReturn(Optional.of(doctor));

        Patient patient = new Patient();
        patient.setId(validRequest.getPatientId());
        when(patientRepository.findById(validRequest.getPatientId())).thenReturn(Optional.of(patient));

        List<Visit> conflicts = new ArrayList<>();
        conflicts.add(new Visit());
        when(visitRepository.findConflictingVisits(validRequest.getDoctorId(),
                validRequest.getStart(), validRequest.getEnd())).thenReturn(conflicts);

        assertThrows(IllegalStateException.class, () -> visitService.createVisit(validRequest));
    }

    @Test
    void testCreateVisit_Success() {
        Doctor doctor = new Doctor();
        doctor.setId(validRequest.getDoctorId());
        doctor.setTimezone("UTC");
        when(doctorRepository.findById(validRequest.getDoctorId())).thenReturn(Optional.of(doctor));

        Patient patient = new Patient();
        patient.setId(validRequest.getPatientId());
        when(patientRepository.findById(validRequest.getPatientId())).thenReturn(Optional.of(patient));

        List<Visit> conflicts = new ArrayList<>();
        when(visitRepository.findConflictingVisits(validRequest.getDoctorId(),
                validRequest.getStart(), validRequest.getEnd())).thenReturn(conflicts);

        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> {
            Visit savedVisit = invocation.getArgument(0);
            savedVisit.setId(1L);
            return savedVisit;
        });

        visitService.createVisit(validRequest);

        verify(visitRepository, times(1)).save(any(Visit.class));
    }

    @Test
    void testGetPatients_Success() {
        Patient patient1 = new Patient();
        patient1.setId(1L);
        Patient patient2 = new Patient();
        patient2.setId(2L);

        List<Patient> patientList = Arrays.asList(patient1, patient2);
        Page<Patient> patientsPage = new PageImpl<>(patientList);

        when(patientRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(patientsPage);

        PatientResponse response1 = new PatientResponse();
        response1.setLastVisits(Collections.emptyList());
        PatientResponse response2 = new PatientResponse();
        response2.setLastVisits(Collections.emptyList());

        when(patientMapper.toPatientResponse(patient1)).thenReturn(response1);
        when(patientMapper.toPatientResponse(patient2)).thenReturn(response2);

        List<Object[]> countResults = Arrays.asList(new Object[]{1L, 5L}, new Object[]{2L, 3L});
        when(visitRepository.countPatientsByDoctorIds(anySet())).thenReturn(countResults);

        PageResponse<PatientResponse> result = visitService.getPatients(pageable, search, doctorIds);

        assertNotNull(result);
        assertEquals(2, result.getData().size()); // Проверка, что два пациента вернулись
        verify(patientRepository, times(1)).findAll(any(Specification.class), eq(pageable)); // Проверка вызова репозитория
        verify(visitRepository, times(1)).countPatientsByDoctorIds(anySet()); // Проверка вызова countPatientsByDoctorIds
        verify(patientMapper, times(2)).toPatientResponse(any(Patient.class)); // Проверка маппинга пациентов
    }


    @Test
    void testGetPatients_NoPatientsFound() {
        Page<Patient> emptyPage = Page.empty();
        when(patientRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        PageResponse<PatientResponse> result = visitService.getPatients(pageable, search, doctorIds);

        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
    }

}