package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.MedicalHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.ArrayList;

public interface MedicalHistoryRepository extends MongoRepository<MedicalHistory, String> {

    @Query("{'userId': ?0}")
    ArrayList<MedicalHistory> getEntriesForPatient(String patientId);
}
