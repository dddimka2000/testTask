package com.test.test.persistance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Doctor extends Person {
    private String timezone;
    @OneToMany(mappedBy = "doctor")
    private List<Visit> visits;
    @Transient
    private long totalPatients;

}