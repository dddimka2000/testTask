package com.test.test.controller;

import com.test.test.dto.PageResponse;
import com.test.test.dto.PatientResponse;
import com.test.test.dto.VisitRequest;
import com.test.test.dto.VisitResponse;
import com.test.test.service.VisitService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/visits")
@AllArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @PostMapping("/create")
    public ResponseEntity<VisitResponse> createVisit(@RequestBody VisitRequest request) {
        visitService.createVisit(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getAll")
    public ResponseEntity<PageResponse<PatientResponse>> getPatients(
            @RequestParam int page, @RequestParam int size, @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> doctorIds) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(visitService.getPatients(pageable, search, doctorIds));
    }
}
