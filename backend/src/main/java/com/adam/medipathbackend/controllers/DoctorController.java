package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.config.Utils;
import com.adam.medipathbackend.forms.DoctorUpdateForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.services.DoctorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    DoctorService doctorService;

    @GetMapping(value= {"/{id}", "/{id}/"})
    public ResponseEntity<Map<String, Object>> getDoctor(@PathVariable String id, @RequestParam(value = "fields", required = false) String[] fields) {

        try {
            Map<String, Object> result = doctorService.getDoctor(id, fields);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value= {"/{id}/institutions", "/{id}/institutions/"})
    public ResponseEntity<Map<String, Object>> getDoctorInstitutions(@PathVariable String id) {

        try {
            Map<String, Object> result = doctorService.getDoctorInstitutions(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }
    @GetMapping(value = {"/{doctorid}/schedules/", "/{doctorid}/schedules"})
    public ResponseEntity<Map<String, Object>> getDoctors(@PathVariable String doctorid, @RequestParam(required = false) String institution) {

        try {
            Map<String, Object> result = doctorService.getDoctorsSchedules(doctorid, institution);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping(value = {"/me/schedules", "/me/schedules/"})
    public ResponseEntity<Map<String, Object>> getMySchedules(HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> result = doctorService.getMySchedules(loggedUserID);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        }

    }

    @PutMapping(value = {"/{doctorid}/", "/{doctorid}"})
    public ResponseEntity<Map<String, Object>> updateDoctor(@PathVariable String doctorid, @RequestBody DoctorUpdateForm doctorUpdateForm, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            doctorService.updateDoctor(doctorid, doctorUpdateForm, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "Doctor updated successfully"), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        }

    }

    @GetMapping(value = {"/me/visits/{date}", "/me/visits/{date}/", "/me/visits", "/me/visits/"})
    public ResponseEntity<Map<String, Object>> getMyVisits(@PathVariable(required = false) String date, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> result = doctorService.getMyVisitsByDate(date, loggedUserID);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        }

    }

    @GetMapping(value = {"/me/patients", "/me/patients/"})
    public ResponseEntity<Map<String, Object>> getMyPatients(@PathVariable(required = false) String date, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> result = doctorService.getMyPatients(loggedUserID);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    @GetMapping(value = {"/me/patients/{patientId}/visits", "/me/patients/{patientId}/visits/"})
    public ResponseEntity<Map<String, Object>> getPatientVisits(@PathVariable String patientId, HttpSession session) {

    String loggedUserID = (String) session.getAttribute("id");
    if (loggedUserID == null) {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    try {
        Map<String, Object> result = doctorService.getPatientVisits(loggedUserID, patientId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
        return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
    } catch (IllegalAccessException e) {
        return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
    }

}
}
