package com.adam.medipathbackend.controllers;


import com.adam.medipathbackend.models.MedicalHistory;
import com.adam.medipathbackend.services.MedicalHistoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/medicalhistory")
public class MedicalHistoryController {

    @Autowired
    MedicalHistoryService medicalHistoryService;


    @PostMapping(value = {"/add", "/add/"})
    public ResponseEntity<Map<String, Object>> addMedicalHistory(@RequestBody MedicalHistory medicalHistory, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {

            medicalHistoryService.addMedicalHistory(medicalHistory, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "Medical history added successfully"), HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        }

    }

    @PutMapping(value = {"/{med_his_id}", "/{med_his_id}/"})
    public ResponseEntity<Map<String, Object>> modifyMedHisEntry(@PathVariable String med_his_id, @RequestBody MedicalHistory medicalHistory, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {

            medicalHistoryService.modifyMedHisEntry(med_his_id, medicalHistory, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "Medical history modified successfully"), HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        }

    }

    @DeleteMapping(value = {"/{med_his_id}", "/{med_his_id}/"})
    public ResponseEntity<Map<String, Object>> deleteMedHisEntry(@PathVariable String med_his_id, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {

            medicalHistoryService.deleteMedHisEntry(med_his_id, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "Medical history deleted successfully"), HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        }

    }

}
