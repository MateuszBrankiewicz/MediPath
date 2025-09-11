package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.Schedule;
import com.adam.medipathbackend.models.Visit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {

    @Query("{'startHour': ?0, 'doctor.userId': ?1}")
    Optional<Schedule> checkScheduleDuplicate(LocalDateTime startHour, String userId);
}
