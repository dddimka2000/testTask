package com.test.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientResponse {
    private String firstName;
    private String lastName;
    private List<VisitResponse> lastVisits;
}
