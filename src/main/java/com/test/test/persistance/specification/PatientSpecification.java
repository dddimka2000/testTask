package com.test.test.persistance.specification;

import com.test.test.persistance.entity.Patient;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PatientSpecification {
    public static Specification<Patient> withFilters(String search, List<Long> doctorIds) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                String searchLower = "%" + search.toLowerCase() + "%";
                Predicate firstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")), searchLower);
                predicates.add(firstNamePredicate);
            }
            if (doctorIds != null && !doctorIds.isEmpty()) {
                predicates.add(root.join("visits").get("doctor").get("id").in(doctorIds));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
