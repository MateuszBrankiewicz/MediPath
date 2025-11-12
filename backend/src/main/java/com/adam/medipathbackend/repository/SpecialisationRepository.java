package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.Specialisation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecialisationRepository extends MongoRepository<Specialisation, String> {
    List<Specialisation> findByIsInstitutionType(boolean isInstitutionType);
}