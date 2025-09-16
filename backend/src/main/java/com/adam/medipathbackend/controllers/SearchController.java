package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    InstitutionRepository institutionRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @GetMapping(value = {"/search/{query}", "/search"})
    public ResponseEntity<Map<String, Object>> search(@PathVariable(required = false) String query, @RequestParam("type") String type, @RequestParam(value = "city", defaultValue = ".*") String city, @RequestParam(value = "specialisations", defaultValue = "") String specialisations) {

        if(query == null || query.isBlank()) {
            query = ".*";
        }
        if(type.equals("institution")) {
            ArrayList<Institution> institutions;

            if(specialisations.isBlank()) {
                institutions = institutionRepository.findInstitutionByCity(city + ".*", query);
            } else {
                institutions = institutionRepository.findInstitutionByCityAndSpec(city + ".*", query, specialisations);
            }
            if(institutions.isEmpty()) {
                return new ResponseEntity<>(Map.of("result", List.of()), HttpStatus.OK);
            }
            List<Map<String, Serializable>> institutions_clean = institutions.stream().map(institution -> Map.of("id", institution.getId(), "name", institution.getName(), "types", institution.getTypes(), "image", institution.getImage(), "address", institution.getAddress().toString(), "isPublic", institution.isPublic(), "rating", institution.getRating(), "numOfRatings", institution.getNumOfRatings())).toList();
            return new ResponseEntity<>(Map.of("result", institutions_clean), HttpStatus.OK);
        } else if(type.equals("doctor")) {
            ArrayList<StaffDigest> doctors;

            if(specialisations.isBlank()) {
                doctors = institutionRepository.findDoctorsByCity(city + ".*", query);
            } else {
                doctors = institutionRepository.findDoctorsByCityAndSpec(city + ".*", query, specialisations);
            }
            if(doctors.isEmpty()) {
                return new ResponseEntity<>(Map.of("result", List.of()), HttpStatus.OK);
            }
            System.out.println();
            List<Map<String, Object>> doctors_clean = doctors.stream().map(doctor -> {
                User doctorProfile = userRepository.findById(doctor.getUserId()).get();
                return Map.of("id", doctor.getUserId(), "name", doctor.getName(), "surname", doctor.getSurname(),
                        "specialisations", doctor.getSpecialisations(), "addresses", getAddressesForDoctor(doctor.getUserId()),
                        "schedules", getSchedulesTruncatedForDoctor(doctor.getUserId()), "image", doctor.getPfpimage(), "rating", doctorProfile.getRating(), "numOfRatings", doctorProfile.getNumOfRatings());
                    }

            ).toList();
            return new ResponseEntity<>(Map.of("result", doctors_clean), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("message", "unknown type"), HttpStatus.BAD_REQUEST);
        }

    }

    private ArrayList<Pair<String, String>> getAddressesForDoctor(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty()) {
            return new ArrayList<>();
        }
        User user = userOptional.get();
        ArrayList<Pair<String, String>> addresses = new ArrayList<>();
        for(InstitutionDigest digest: user.getEmployers()) {
            Optional<Institution> institutionOptional = institutionRepository.findById(digest.getInstitutionId());
            if(institutionOptional.isEmpty()) continue;
            addresses.add(Pair.of(institutionOptional.get().getName(), institutionOptional.get().getAddress().toString()));
        }
        return addresses;
    }

    private Object getSchedulesTruncatedForDoctor(String userid) {
        ArrayList<Schedule> schedules = scheduleRepository.getUpcomingSchedulesByDoctor(userid);
        return schedules.stream().map(schedule -> Map.of("id", schedule.getId(), "startTime", schedule.getStartHour().toString(), "isBooked", schedule.isBooked())).toList();
    }
}
