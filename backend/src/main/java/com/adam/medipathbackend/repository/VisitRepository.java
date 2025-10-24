package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.models.Visit;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public interface VisitRepository extends MongoRepository<Visit, String> {

    @Query("{'patient.userId': ?0, 'time.startTime': { $gt: new Date() }, 'status': 'Upcoming'}")
    ArrayList<Visit> getUpcomingVisits(String patientID);

    @Query("{'patient.userId': ?0}")
    ArrayList<Visit> getAllVisitsForPatient(String patientID);

    @Aggregation({"{ $unwind: { path: \"$codes\" } }", " { $match: { \"patient.userId\": \"?0\" } }", "{ $project:  { \"codes.codeType\": 1, \"codes.code\": 1, \"codes.isActive\": 1,_id: 0, date: { $dateToString: { format: \"%Y-%m-%dT%H:%M:%S.%LZ\", date: \"$time.endTime\" }}, doctor: {$concat: [\"$doctor.doctorName\", \" \", \"$doctor.doctorSurname\"]} } }"})
    ArrayList<Map<String, Object>> getCodesForPatient(String patientID);

    @Query("{'patient.userId': ?0, 'doctor.userId': ?1}")
    ArrayList<Visit> getAllVisitsForPatientWithDoctor(String patientID, String doctorId);

    @Query("{doctor.userId': ?0}")
    ArrayList<Visit> getAllVisitsForDoctor(String doctorId);

    @Query("{'patient.userId' : ?0, 'institution.institutionId' : ?1}")
    ArrayList<Visit> getAllVisitsForPatientInInstitution(String patientID, String institutionid);

    @Query("{'institution.institutionId' : ?0, 'status': 'Upcoming' }}")
    ArrayList<Visit> getUpcomingVisitsInInstitution(String institutionId);

    @Query("{'doctor.userId': ?0, 'time.startTime': {$gt: ?1, $lt: ?2}}")
    ArrayList<Visit> getDoctorVisitsOnDay(String userId, LocalDateTime date1, LocalDateTime date2);

    @Query("{'institution.institutionId': ?0, 'time.startTime': {$gt: ?1, $lt: ?2}}")
    ArrayList<Visit> getInstitutionVisitsOnDay(String userId, LocalDateTime date1, LocalDateTime date2);

    @Query("{'institution.institutionId' : ?0}}")
    ArrayList<Visit> getAllVisitsInInstitution(String institutionId);
}
