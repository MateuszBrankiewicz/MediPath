package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.config.Utils;
import com.adam.medipathbackend.forms.DoctorUpdateForm;
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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

    @Autowired
    VisitRepository visitRepository;

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
            fieldsList = List.of("id", "name", "surname", "licence_number", "specialisations", "employers", "rating", "numofratings", "image");
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

        if(fieldsList.contains("rating")) {
            outputFields.put("rating", foundDoctor.getRating());
        }

        if(fieldsList.contains("employers")) {
            outputFields.put("employers", foundDoctor.getEmployers());
        }

        if(fieldsList.contains("numofratings")) {
            outputFields.put("numofratings", foundDoctor.getNumOfRatings());
        }
        if(fieldsList.contains("image")) {
            outputFields.put("image", foundDoctor.getPfpimage());
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

    @PutMapping(value = {"/{doctorid}/", "/{doctorid}"})
    public ResponseEntity<Map<String, Object>> updateDoctor(@PathVariable String doctorid, @RequestBody DoctorUpdateForm doctorUpdateForm, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> adminOpt = userRepository.findById(loggedUserID);
        if(adminOpt.isEmpty() || adminOpt.get().getRoleCode() < 8) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(!Utils.isValidMongoOID(doctorid)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Optional<User> doctorOpt = userRepository.findDoctorById(doctorid);
        if(doctorOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(doctorUpdateForm.getLicenceNumber() == null) {
            return new ResponseEntity<>(Map.of("message", "missing licence number"), HttpStatus.BAD_REQUEST);
        }
        if(doctorUpdateForm.getSpecialisations() == null) {
            return new ResponseEntity<>(Map.of("message", "missing specialisations"), HttpStatus.BAD_REQUEST);
        }
        User doctor = doctorOpt.get();
        doctor.setLicenceNumber(doctorUpdateForm.getLicenceNumber());
        doctor.setSpecialisations(doctorUpdateForm.getSpecialisations());
        userRepository.save(doctor);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping(value = {"/me/visitsbydate/{date}", "/me/visitsbydate/{date}/"})
    public ResponseEntity<Map<String, Object>> getMyVisitsByDate(@PathVariable String date, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if(!Utils.isValidMongoOID(loggedUserID)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Optional<User> doctorOpt = userRepository.findDoctorById(loggedUserID);
        if(doctorOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(date.equals("today")) {
            date = LocalDate.now().toString();
        }
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(Map.of("message","invalid date"), HttpStatus.BAD_REQUEST);
        }
        ArrayList<Visit> visits = visitRepository.getDoctorVisitsOnDay(loggedUserID, startDate.atStartOfDay(), startDate.plusDays(1).atStartOfDay());
        return new ResponseEntity<>(Map.of("visits", visits), HttpStatus.OK);


    }

}
