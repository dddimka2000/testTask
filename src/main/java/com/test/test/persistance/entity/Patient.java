package com.test.test.persistance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Entity
@Data
public class Patient extends Person {
    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    private List<Visit> visits;
}