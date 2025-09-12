package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    @Autowired
    VisitRepository visitRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddVisitForm visit) {
        ArrayList<String> missingFields = new ArrayList<>();

        if(visit.getPatientID() == null || visit.getPatientID().isBlank()) {
            missingFields.add("patient");
        }
        if(visit.getScheduleID() == null || visit.getScheduleID().isBlank()) {
            missingFields.add("time");
        }
        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }
        Optional<User> optionalUser = userRepository.findById(visit.getPatientID());
        if(optionalUser.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "invalid patient id"), HttpStatus.BAD_REQUEST);
        }
        Optional<Schedule> schedule = scheduleRepository.findById(visit.getScheduleID());
        if(schedule.isEmpty() || schedule.get().isBooked()) {
            return new ResponseEntity<>(Map.of("message", "visit time is invalid or booked"), HttpStatus.BAD_REQUEST);
        }
        Schedule foundSchedule = schedule.get();
        User foundUser = optionalUser.get();
        PatientDigest foundUserDigest = new PatientDigest(foundUser.getId(), foundUser.getName(), foundUser.getSurname(), foundUser.getGovId());
        VisitTime time = new VisitTime(foundSchedule.getId(), foundSchedule.getStartHour(), foundSchedule.getEndHour());
        Visit newVisit = new Visit(foundUserDigest, foundSchedule.getDoctor(), time,foundSchedule.getInstitution());
        foundSchedule.setBooked(true);
        ArrayList<Code> codes = new ArrayList<>();
        codes.add(new Code(Code.CodeType.PRESCRIPTION, "1234", true));
        codes.add(new Code(Code.CodeType.REFERRAL, "12345", true));
        newVisit.setCodes(codes);
        visitRepository.save(newVisit);
        scheduleRepository.save(foundSchedule);
        return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.CREATED);
    }

    @GetMapping("/getupcoming/{userid}")
    public ResponseEntity<Map<String, Object>> add(@PathVariable String userid) {
        if(!userRepository.existsById(userid)) {
            return new ResponseEntity<>(Map.of("message", "invalid user id"), HttpStatus.BAD_REQUEST);
        }
        ArrayList<Visit> upcomingVisits =  visitRepository.getUpcomingVisits(userid);
        return new ResponseEntity<>(Map.of("visits", upcomingVisits), HttpStatus.OK);
    }
    @GetMapping("/getactivecodes/{userid}")
    public ResponseEntity<Map<String, Object>> getCodes(@PathVariable String userid) {
        if(!userRepository.existsById(userid)) {
            return new ResponseEntity<>(Map.of("message", "invalid user id"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(Map.of("codes", visitRepository.getActiveCodesForPatient(userid)), HttpStatus.OK);
    }

}
