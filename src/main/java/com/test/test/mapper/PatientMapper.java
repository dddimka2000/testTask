package com.test.test.mapper;

import com.test.test.dto.PatientResponse;
import com.test.test.dto.VisitResponse;
import com.test.test.persistance.entity.Patient;
import com.test.test.persistance.entity.Visit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "lastVisits", source = "visits")
    List<VisitResponse> toVisitResponses(List<Visit> visits);

    @Mapping(target = "lastVisits", source = "visits")
    PatientResponse toPatientResponse(Patient patient);

    @Mapping(target = "start", source = "startDateTime")
    @Mapping(target = "end", source = "endDateTime")
    @Mapping(target = "doctor.id", source = "doctor.id")
    @Mapping(target = "doctor.firstName", source = "doctor.firstName")
    @Mapping(target = "doctor.lastName", source = "doctor.lastName")
    VisitResponse toVisitResponse(Visit visit);
}
