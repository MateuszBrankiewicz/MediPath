package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.DoctorDigest;
import com.adam.medipathbackend.models.MedicalHistory;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.models.Visit;
import com.adam.medipathbackend.repository.MedicalHistoryRepository;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.repository.VisitRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/medicalhistory")
public class MedicalHistoryController {

    @Autowired
    UserRepository userRepository;


    @Autowired
    MedicalHistoryRepository medicalHistoryRepository;

    @Autowired
    VisitRepository visitRepository;


    @PostMapping(value = {"/add", "/add/"})
    public ResponseEntity<Map<String, Object>> addMedicalHistory(@RequestBody MedicalHistory medicalHistory, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ArrayList<String> missingFields = new ArrayList<>();
        if(medicalHistory.getUserId() == null || medicalHistory.getUserId().isBlank()) {
            missingFields.add("userId");
        }
        if(medicalHistory.getDate() == null) {
            missingFields.add("date");
        }
        if(medicalHistory.getNote() == null || medicalHistory.getNote().isBlank()) {
            missingFields.add("note");
        }
        if(medicalHistory.getTitle() == null || medicalHistory.getTitle().isBlank()) {
            missingFields.add("title");
        }
        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }
        Optional<User> userOpt = userRepository.findById(medicalHistory.getUserId());
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (!medicalHistory.getUserId().equals(loggedUserID)) {
            if (medicalHistory.getDoctor() == null || medicalHistory.getDoctor().getUserId() == null || medicalHistory.getDoctor().getUserId().isBlank()) {
                return new ResponseEntity<>(Map.of("message", "doctor digest must be included when adding for a patient"), HttpStatus.BAD_REQUEST);
            } else if (!medicalHistory.getDoctor().getUserId().equals(loggedUserID)
                      || visitRepository.getAllVisitsForPatientWithDoctor(medicalHistory.getUserId(), medicalHistory.getDoctor().getUserId()).isEmpty()) {

                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } else {
                Optional<User> doctorOpt = userRepository.findById(medicalHistory.getDoctor().getUserId());
                if(doctorOpt.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
                User doctor = doctorOpt.get();
                medicalHistory.setDoctor(new DoctorDigest(doctor.getId(), doctor.getName(), doctor.getSurname(), doctor.getSpecialisations()));
            }

        } else {
            medicalHistory.setDoctor(null);
        }
        User patient = userOpt.get();
        patient.addLatestMedicalHistory(medicalHistory);
        medicalHistoryRepository.save(medicalHistory);
        userRepository.save(patient);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = {"/{med_his_id}", "/{med_his_id}/"})
    public ResponseEntity<Map<String, Object>> modifyMedHisEntry(@PathVariable String med_his_id, @RequestBody MedicalHistory medicalHistory, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<MedicalHistory> oldMedHisOpt = medicalHistoryRepository.findById(med_his_id);
        if(oldMedHisOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        MedicalHistory oldMedicalHistory = oldMedHisOpt.get();
        if(!oldMedicalHistory.getUserId().equals(loggedUserID)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(oldMedicalHistory.getDoctor() != null) {
            return new ResponseEntity<>(Map.of("message", "entries added by doctors cannot be changed"), HttpStatus.FORBIDDEN);
        }

        oldMedicalHistory.setDate(medicalHistory.getDate());
        oldMedicalHistory.setNote(medicalHistory.getNote());
        oldMedicalHistory.setTitle(medicalHistory.getTitle());
        User user = userOpt.get();
        LinkedList<MedicalHistory> histories = user.getLatestMedicalHistory();
        boolean foundInLatest = false;
        for(int i = 0; i < histories.size(); i++) {
            if(histories.get(i).getId().equals(med_his_id)) {
                histories.set(i, oldMedicalHistory);
                foundInLatest = true;
            }
        }

        medicalHistoryRepository.save(oldMedicalHistory);
        if(foundInLatest) userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @DeleteMapping(value = {"/{med_his_id}", "/{med_his_id}/"})
    public ResponseEntity<Map<String, Object>> deleteMedHisEntry(@PathVariable String med_his_id, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<MedicalHistory> oldMedHisOpt = medicalHistoryRepository.findById(med_his_id);
        if(oldMedHisOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        MedicalHistory oldMedicalHistory = oldMedHisOpt.get();
        if(!oldMedicalHistory.getUserId().equals(loggedUserID)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(oldMedicalHistory.getDoctor() != null) {
            return new ResponseEntity<>(Map.of("message", "entries added by doctors cannot be deleted"), HttpStatus.FORBIDDEN);
        }

        User user = userOpt.get();
        LinkedList<MedicalHistory> histories = user.getLatestMedicalHistory();
        boolean foundInLatest = histories.removeIf(history -> history.getId().equals(med_his_id));

        medicalHistoryRepository.deleteById(med_his_id);
        if(foundInLatest) userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);

    }

}
