package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.ScheduleRepository;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping(value = {"/search/{query}", "/search/{query}/{city}"})
    public ResponseEntity<Map<String, Object>> search(@PathVariable String query, @PathVariable(required = false) String city) {
        ArrayList<Institution> institutions;
        ArrayList<StaffDigest> doctors;

        city = city == null ? ".*" : city;
        institutions = institutionRepository.findInstitutionByCity(city, query);
        doctors = institutionRepository.findDoctorsByCity(city, query);

        List<Map<String, Serializable>> institutions_clean = institutions.stream().map(institution -> Map.of("id", institution.getId(), "name", institution.getName(), "types", institution.getTypes())).toList();
        List<Map<String, Object>> doctors_clean = doctors.stream().map(doctor ->
                Map.of("id", doctor.getUserId(), "name", doctor.getName(), "surname", doctor.getSurname(),
                        "specialisations", doctor.getSpecialisations(), "addresses", getAddressesForDoctor(doctor.getUserId()),
                        "schedules", getSchedulesTruncatedForDoctor(doctor.getUserId()))
        ).toList();
        return new ResponseEntity<>(Map.of("institutions", institutions_clean, "doctors", doctors_clean), HttpStatus.OK);
    }

    private ArrayList<String> getAddressesForDoctor(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty()) {
            return new ArrayList<>();
        }
        User user = userOptional.get();
        ArrayList<String> addresses = new ArrayList<>();
        for(InstitutionDigest digest: user.getEmployers()) {
            Optional<Institution> institutionOptional = institutionRepository.findById(digest.getInstitutionId());
            if(institutionOptional.isEmpty()) continue;
            addresses.add(institutionOptional.get().getAddress().toString());
        }
        return addresses;
    }

    private Object getSchedulesTruncatedForDoctor(String userid) {
        ArrayList<Schedule> schedules = scheduleRepository.getUpcomingSchedulesByDoctor(userid);
        return schedules.stream().map(schedule -> Map.of("id", schedule.getId(), "startTime", schedule.getStartHour().toString(), "isBooked", schedule.isBooked())).toList();
    }
}
