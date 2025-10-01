package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.Schedule;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.ArrayList;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {

    @Query("{'doctor.userId': ?0 }")
    ArrayList<Schedule> getSchedulesByDoctor(String doctorID);

    @Aggregation({"{ $match: { \"doctor.userId\": \"?0\", $expr: {  $gt: [\"$startHour\", { $dateTrunc: { date: \"$$NOW\",  unit: \"day\", binSize: 1 } } ] } } }"})
    ArrayList<Schedule> getUpcomingSchedulesByDoctor(String doctorID);
}
