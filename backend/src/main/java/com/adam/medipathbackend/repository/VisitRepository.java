package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.models.Visit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.ArrayList;
import java.util.Optional;

public interface VisitRepository extends MongoRepository<Visit, String> {

    @Query("{'patient.patientId': ?0, 'time.startTime': { $gt: new Date() }}")
    ArrayList<Visit> getUpcomingVisits(String patientID);
}
