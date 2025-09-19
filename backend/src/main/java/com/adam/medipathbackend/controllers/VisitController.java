package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.repository.VisitRepository;
import jakarta.servlet.http.HttpSession;
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

    @Autowired
    InstitutionRepository institutionRepository;

    @PostMapping(value = {"/add", "/add/"})
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddVisitForm visit) {
        ArrayList<String> missingFields = new ArrayList<>();

        if(visit.getPatientID() == null || visit.getPatientID().isBlank()) {
            missingFields.add("patientID");
        }
        if(visit.getScheduleID() == null || visit.getScheduleID().isBlank()) {
            missingFields.add("scheduleID");
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

    @GetMapping(value = {"/getupcoming/{userid}", "/getupcoming/{userid}/"})
    public ResponseEntity<Map<String, Object>> add(@PathVariable String userid) {
        if(!userRepository.existsById(userid)) {
            return new ResponseEntity<>(Map.of("message", "invalid user id"), HttpStatus.BAD_REQUEST);
        }
        ArrayList<Visit> upcomingVisits =  visitRepository.getUpcomingVisits(userid);
        return new ResponseEntity<>(Map.of("visits", upcomingVisits), HttpStatus.OK);
    }
    @GetMapping(value = {"/getactivecodes/{userid}", "/getactivecodes/{userid}/"})
    public ResponseEntity<Map<String, Object>> getCodes(@PathVariable String userid) {
        if(!userRepository.existsById(userid)) {
            return new ResponseEntity<>(Map.of("message", "invalid user id"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(Map.of("codes", visitRepository.getActiveCodesForPatient(userid)), HttpStatus.OK);
    }
    @DeleteMapping(value = {"/{visitid}", "/{visitid}/"})
    public ResponseEntity<Map<String, Object>> cancelVisit(@PathVariable String visitid, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Visit> optVisit = visitRepository.findById(visitid);
        if(optVisit.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Visit visitToCancel = optVisit.get();
        if(!(visitToCancel.getPatient().getUserId().equals(loggedUserID) || isLoggedAsEmployeeOfInstitution(loggedUserID, visitToCancel.getInstitution().getInstitutionId()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(visitToCancel.getStatus().equals("Completed")) {
            return new ResponseEntity<>(Map.of("message", "this visit is completed"), HttpStatus.BAD_REQUEST);
        }
        if(visitToCancel.getStatus().equals("Cancelled")) {
            return new ResponseEntity<>(Map.of("message", "this visit is already cancelled"), HttpStatus.BAD_REQUEST);
        }
        visitToCancel.setStatus("Cancelled");
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(visitToCancel.getTime().getScheduleId());
        if(scheduleOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Schedule oldSchedule = scheduleOptional.get();
        oldSchedule.setBooked(false);
        scheduleRepository.save(oldSchedule);
        visitRepository.save(visitToCancel);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = {"/{visitid}/reschedule/", "/{visitid}/reschedule"})
    public ResponseEntity<Map<String, Object>> rescheduleVisit(@PathVariable String visitid, @RequestParam(value = "newschedule", defaultValue = "") String newScheduleId, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if(newScheduleId.isBlank()) {
            return new ResponseEntity<>(Map.of("message", "newschedule parameter missing"), HttpStatus.BAD_REQUEST);
        }
        Optional<Visit> optVisit = visitRepository.findById(visitid);
        if(optVisit.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Visit visitToReschedule = optVisit.get();
        if(!(visitToReschedule.getPatient().getUserId().equals(loggedUserID) || isLoggedAsEmployeeOfInstitution(loggedUserID, visitToReschedule.getInstitution().getInstitutionId()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(visitToReschedule.getStatus().equals("Completed")) {
            return new ResponseEntity<>(Map.of("message", "this visit is completed"), HttpStatus.BAD_REQUEST);
        }
        if(visitToReschedule.getStatus().equals("Cancelled")) {
            return new ResponseEntity<>(Map.of("message", "this visit is already cancelled"), HttpStatus.BAD_REQUEST);
        }
        Optional<Schedule> newScheduleOptional = scheduleRepository.findById(newScheduleId);
        if(newScheduleOptional.isEmpty() || newScheduleOptional.get().isBooked()) {
            return new ResponseEntity<>(Map.of("message", "invalid new schedule id or schedule is booked"), HttpStatus.BAD_REQUEST);
        }
        Schedule newSchedule = newScheduleOptional.get();
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(visitToReschedule.getTime().getScheduleId());
        if(scheduleOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Schedule oldSchedule = scheduleOptional.get();
        oldSchedule.setBooked(false);
        newSchedule.setBooked(true);
        visitToReschedule.setTime(new VisitTime(newSchedule.getId(), newSchedule.getStartHour(), newSchedule.getEndHour()));
        scheduleRepository.save(oldSchedule);
        scheduleRepository.save(newSchedule);
        visitRepository.save(visitToReschedule);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = {"/{visitid}", "/{visitid}/"})
    public ResponseEntity<Map<String, Object>> getVisitDetails(@PathVariable String visitid, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Visit> optVisit = visitRepository.findById(visitid);
        if(optVisit.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Visit visit = optVisit.get();
        if(!(visit.getPatient().getUserId().equals(loggedUserID) || isLoggedInAsStaffOrDoctorInInstitution(loggedUserID, visit.getInstitution().getInstitutionId()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(Map.of("visit", visit), HttpStatus.OK);
    }

    private boolean isLoggedAsEmployeeOfInstitution(String userID, String institutionID) {
        return institutionRepository.findStaffById(userID, institutionID).isPresent();
    }
    private boolean isLoggedInAsStaffOrDoctorInInstitution(String userid, String institutionID) {
        return institutionRepository.findStaffORDoctorById(userid, institutionID).isPresent();
    }

}
