package com.test.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitRequest {
    private LocalDateTime start;
    private LocalDateTime end;
    private Long patientId;
    private Long doctorId;
}