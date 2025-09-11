package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.config.Utils;
import com.adam.medipathbackend.models.Schedule;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.models.Visit;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    UserRepository userRepository;

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

    @GetMapping("/bydoctor/{id}")
    public ResponseEntity<Map<String, Object>> getByDoctor(@PathVariable String id) {
        if(!Utils.isValidMongoOID(id) || userRepository.findDoctorById(id).isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "invalid user id"), HttpStatus.BAD_REQUEST);
        }
        ArrayList<Schedule> schedules = scheduleRepository.getSchedulesByDoctor(id);
        return new ResponseEntity<>(Map.of("schedules", schedules), HttpStatus.OK);
    }

}
