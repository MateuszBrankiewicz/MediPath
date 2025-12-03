package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.Schedule;
import com.adam.medipathbackend.models.Visit;
import org.springframework.data.mongodb.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {

    @Query("{'doctor.userId': ?0 }")
    ArrayList<Schedule> getSchedulesByDoctor(String doctorID);

    @Aggregation({"{ $match: { \"doctor.userId\": \"?0\", $expr: {  $gt: [\"$startHour\", { $dateTrunc: { date: \"$$NOW\",  unit: \"day\", binSize: 1 } } ] } } }"})
    ArrayList<Schedule> getUpcomingSchedulesByDoctor(String doctorID);

    @Aggregation({"{ $match: { \"doctor.userId\": \"?0\", \"institution.institutionId\": \"?1\", $expr: {  $gt: [\"$startHour\", { $dateTrunc: { date: \"$$NOW\",  unit: \"day\", binSize: 1 } } ] } } }"})
    ArrayList<Schedule> getUpcomingSchedulesByDoctorInInstitution(String doctorID, String institutionId);

    @Query("{'doctor.userId': ?0, startHour: {$gte: ?1, $lte: ?2}}")
    ArrayList<Schedule> getSchedulesBetween(String doctorId, LocalDateTime date1, LocalDateTime date2);

    @Query("{'institution.institutionId': ?0, 'startHour': {$gt: ?1, $lt: ?2}}")
    ArrayList<Schedule> getInstitutionSchedulesOnDay(String institutionId, LocalDateTime date1, LocalDateTime date2);

    @Query("{'institution.institutionId': ?0}}")
    ArrayList<Schedule> getInstitutionSchedules(String institutionId);

    @Query("{'doctor.userId': ?0}")
    @Update("{'$set': { booked: true }}")
    void deleteAllFutureSchedulesForDoctor(String doctorid);

    @DeleteQuery("{ startHour: { $lt: ?0 }}")
    void deleteOldSchedules(LocalDateTime date);

}
