package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    InstitutionRepository institutionRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @GetMapping(value= {"/{id}", "/{id}/"})
    public ResponseEntity<Map<String, Object>> getDoctor(@PathVariable String id, @RequestParam(value = "fields", required = false) String[] fields) {
        Optional<User> user = userRepository.findDoctorById(id);
        if(user.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "invalid user id"), HttpStatus.BAD_REQUEST);
        }
        User foundDoctor = user.get();
        Map<String, Object> outputFields = new HashMap<>();
        System.out.println(fields == null);
        List<String> fieldsList;
        if(fields == null) {
            fieldsList = List.of("id", "name", "surname", "licence_number", "specialisations", "employers");
        } else {
            fieldsList = List.of(fields);
        }

        if(fieldsList.contains("id")) {
            outputFields.put("id", foundDoctor.getId());
        }
        if(fieldsList.contains("name")) {
            outputFields.put("name", foundDoctor.getName());
        }
        if(fieldsList.contains("surname")) {
            outputFields.put("surname", foundDoctor.getSurname());
        }
        if(fieldsList.contains("licence_number")) {
            outputFields.put("licence_number", foundDoctor.getLicenceNumber());
        }
        if(fieldsList.contains("specialisations")) {
            outputFields.put("specialisations", foundDoctor.getSpecialisations());
        }
        if(fieldsList.contains("employers")) {
            outputFields.put("employers", foundDoctor.getEmployers());
        }
        return new ResponseEntity<>(Map.of("doctor", outputFields), HttpStatus.OK);
    }

    @GetMapping(value= {"/{id}/institutions", "/{id}/institutions/"})
    public ResponseEntity<Map<String, Object>> getDoctorInstitutions(@PathVariable String id) {
        Optional<User> user = userRepository.findDoctorById(id);
        if(user.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "invalid user id"), HttpStatus.BAD_REQUEST);
        }
        User foundDoctor = user.get();
        ArrayList<InstitutionDigest> employers = foundDoctor.getEmployers();
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        boolean updated = false;
        for(int i = 0; i < employers.size(); i++) {
            InstitutionDigest digest = employers.get(i);
            Optional<Institution> institutionOpt = institutionRepository.findById(digest.getInstitutionId());
            if(institutionOpt.isPresent()) {
                Institution institution = institutionOpt.get();
                if (!institution.getName().equals(digest.getInstitutionName())) {
                    employers.set(i, new InstitutionDigest(digest.getInstitutionId(), institution.getName()));
                    updated = true;
                }
                results.add(Map.of("institutionId", institution.getId(), "institutionName", institution.getName(), "image", institution.getImage(), "address", institution.getAddress()));
            }

        }
        if(updated) {
            foundDoctor.setEmployers(employers);
            userRepository.save(foundDoctor);
        }
        return new ResponseEntity<>(Map.of("institutions", results), HttpStatus.OK);
    }
    @GetMapping(value = {"/{doctorid}/schedules/", "/{doctorid}/schedules"})
    public ResponseEntity<Map<String, Object>> getDoctors(@PathVariable String doctorid, @RequestParam(required = false) String institution) {
            Optional<User> doctorOpt = userRepository.findDoctorById(doctorid);
            if(doctorOpt.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            if(institution != null) {
                User doctor = doctorOpt.get();
                if(doctor.getEmployers().stream().noneMatch(employer -> employer.getInstitutionId().equals(institution))) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                return new ResponseEntity<>(Map.of("schedules", scheduleRepository.getUpcomingSchedulesByDoctorInInstitution(doctorid, institution)), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Map.of("schedules", scheduleRepository.getUpcomingSchedulesByDoctor(doctorid)), HttpStatus.OK);

            }

    }

    @GetMapping(value = {"/me/schedules", "/me/schedules/"})
    public ResponseEntity<Map<String, Object>> getMySchedules(HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if(userRepository.findDoctorById(loggedUserID).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        ArrayList<Schedule> schedules = scheduleRepository.getSchedulesByDoctor(loggedUserID);
        return new ResponseEntity<>(Map.of("schedules", schedules), HttpStatus.OK);
    }
}
