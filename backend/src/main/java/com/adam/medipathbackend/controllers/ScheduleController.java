package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.config.Utils;
import com.adam.medipathbackend.forms.AddScheduleForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
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

    @Autowired
    InstitutionRepository institutionRepository;

    @PostMapping(value = {"/add", "/add/"})
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddScheduleForm schedule, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        ArrayList<String> missingFields = getMissingFields(schedule);
        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }

        if(!isLoggedAsEmployeeOfInstitution(loggedUserID, schedule.getInstitutionID())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(!isLoggedAsDoctorOfInstitution(schedule.getDoctorID(), schedule.getInstitutionID())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<User> optUser = userRepository.findById(schedule.getDoctorID());
        if(optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Optional<Institution> optInst = institutionRepository.findById(schedule.getInstitutionID());
        if(optInst.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(isScheduleOverlapping(schedule.getStartHour(), schedule.getEndHour(), schedule.getDoctorID(), null)) {
            return new ResponseEntity<>(Map.of("message", "this doctor is booked at this hour"), HttpStatus.CONFLICT);
        }
        User doctor = optUser.get();
        Schedule newSchedule = new Schedule(schedule.getStartHour(), schedule.getEndHour(), new DoctorDigest(schedule.getDoctorID(), doctor.getName(), doctor.getSurname(), doctor.getSpecialisations()), new InstitutionDigest(schedule.getInstitutionID(), optInst.get().getName()));
        scheduleRepository.save(newSchedule);
        return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.CREATED);

    }

    @PostMapping(value = {"/addmany", "/addmany/"})
    public ResponseEntity<Map<String, Object>> addMany(@RequestBody AddScheduleForm schedule, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        ArrayList<String> missingFields = getMissingFields(schedule);
        if(schedule.getInterval() == null) {
            missingFields.add("interval");
        }
        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }

        if(!isLoggedAsEmployeeOfInstitution(loggedUserID, schedule.getInstitutionID())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(!isLoggedAsDoctorOfInstitution(schedule.getDoctorID(), schedule.getInstitutionID())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<User> optUser = userRepository.findById(schedule.getDoctorID());
        if(optUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Optional<Institution> optInst = institutionRepository.findById(schedule.getInstitutionID());
        if(optInst.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        LocalDateTime start = schedule.getStartHour();
        ArrayList<Schedule> newSchedules = new ArrayList<>();
        User doctor = optUser.get();
        while(start.plusSeconds(schedule.getInterval().toSecondOfDay()).isBefore(schedule.getEndHour())) {
            Optional<Schedule> isDoctorBookedThen = scheduleRepository.checkScheduleDuplicate(schedule.getStartHour(), schedule.getDoctorID());
            if(isDoctorBookedThen.isPresent()) {
                return new ResponseEntity<>(Map.of("message", "this doctor is booked at in this time frame"), HttpStatus.CONFLICT);
            }
            newSchedules.add(new Schedule(start, start.plusSeconds(schedule.getInterval().toSecondOfDay()), new DoctorDigest(schedule.getDoctorID(), doctor.getName(), doctor.getSurname(), doctor.getSpecialisations()), new InstitutionDigest(schedule.getInstitutionID(), optInst.get().getName())));
            start = start.plusSeconds(schedule.getInterval().toSecondOfDay());
        }
        scheduleRepository.saveAll(newSchedules);
        return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.CREATED);

    }



    private static ArrayList<String> getMissingFields(AddScheduleForm schedule) {
        ArrayList<String> missingFields = new ArrayList<>();

        if(schedule.getEndHour() == null) {
            missingFields.add("endHour");
        }
        if(schedule.getDoctorID() == null || schedule.getDoctorID().isBlank()) {
            missingFields.add("doctor");
        }
        if(schedule.getStartHour() == null) {
            missingFields.add("startHour");
        }
        if(schedule.getInstitutionID() == null || schedule.getInstitutionID().isBlank()) {
            missingFields.add("institution");
        }
        return missingFields;
    }

    @GetMapping(value = {"/bydoctor/{id}", "/bydoctor/{id}/"})
    public ResponseEntity<Map<String, Object>> getByDoctor(@PathVariable String id) {
        if(!Utils.isValidMongoOID(id) || userRepository.findDoctorById(id).isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "invalid user id"), HttpStatus.BAD_REQUEST);
        }
        ArrayList<Schedule> schedules = scheduleRepository.getSchedulesByDoctor(id);
        return new ResponseEntity<>(Map.of("schedules", schedules), HttpStatus.OK);
    }

//    @PutMapping(value = {"/{scheduleid}", "/{scheduleid}/"})
//    public ResponseEntity<Map<String, Object>> updateSchedule(@PathVariable String scheduleid, @RequestBody AddScheduleForm newSchedule, HttpSession session) {
//
//    }

    private boolean isLoggedAsDoctorOfInstitution(String userID, String institutionID) {
        if(!Utils.isValidMongoOID(userID) || !Utils.isValidMongoOID(institutionID)) {
            return false;
        }
        return institutionRepository.findDoctorById(userID, institutionID).isPresent();
    }
    private boolean isLoggedAsEmployeeOfInstitution(String userID, String institutionID) {
        if(!Utils.isValidMongoOID(userID) || !Utils.isValidMongoOID(institutionID)) {
            return false;
        }
        return institutionRepository.findStaffById(userID, institutionID).isPresent();
    }

}
