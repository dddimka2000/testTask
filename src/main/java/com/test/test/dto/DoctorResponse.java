package com.test.test.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponse {
    @JsonIgnore
    private long id;
    private String firstName;
    private String lastName;
    private long totalPatients;
}
