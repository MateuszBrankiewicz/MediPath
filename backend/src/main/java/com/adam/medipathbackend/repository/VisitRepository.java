package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.models.Visit;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public interface VisitRepository extends MongoRepository<Visit, String> {

    @Query("{'patient.patientId': ?0, 'time.startTime': { $gt: new Date() }}")
    ArrayList<Visit> getUpcomingVisits(String patientID);

    @Query("{'patient.patientId': ?0}")
    ArrayList<Visit> getAllVisitsForPatient(String patientID);

    @Aggregation({"{ $unwind: { path: \"$codes\" } }", " { $match: { \"patient.userId\": \"?0\" } }", "{ $project:  { \"codes.codeType\": 1, \"codes.code\": 1, _id: 0, date: { $dateToString: { format: \"%Y-%m-%dT%H:%M:%S.%LZ\", date: \"$time.endTime\" } } } }"})
    ArrayList<Map<String, Object>> getCodesForPatient(String patientID);
}
