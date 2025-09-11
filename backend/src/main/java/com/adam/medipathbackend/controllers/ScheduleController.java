package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.Schedule;
import com.adam.medipathbackend.models.Visit;
import com.adam.medipathbackend.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    ScheduleRepository scheduleRepository;


    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> add(@RequestBody Schedule schedule) {
        ArrayList<String> missingFields = new ArrayList<>();

        if(schedule.getEndHour() == null) {
            missingFields.add("endHour");
        }
        if(schedule.getDoctor() == null || !schedule.getDoctor().isValid()) {
            missingFields.add("doctor");
        }
        if(schedule.getStartHour() == null) {
            missingFields.add("startHour");
        }
        if(schedule.getInstitution() == null || !schedule.getInstitution().isValid()) {
            missingFields.add("institution");
        }
        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }
        Optional<Schedule> isDoctorBookedThen = scheduleRepository.checkScheduleDuplicate(schedule.getStartHour(), schedule.getDoctor().getUserId());
        if(isDoctorBookedThen.isPresent()) {
            return new ResponseEntity<>(Map.of("message", "this doctor is booked at this hour"), HttpStatus.CONFLICT);
        }
        scheduleRepository.save(schedule);
        return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.CREATED);

    }

}
