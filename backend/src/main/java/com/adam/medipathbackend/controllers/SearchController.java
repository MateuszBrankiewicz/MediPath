package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.Institution;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    InstitutionRepository institutionRepository;


    @GetMapping("/search/{type}/{query}")
    public ResponseEntity<Map<String, Object>> search(@PathVariable String query, @PathVariable String type) {
        ArrayList<Institution> institutions;
        ArrayList<User> doctors;
        if(type.equals("by-spec")) {
            institutions = institutionRepository.findInstitutionBySpec(query);
            doctors = userRepository.findDoctorsBySpec(query);
        } else if(type.equals("by-name")) {
            institutions = institutionRepository.findInstitutionByName(query);
            doctors = userRepository.findDoctorsByName(query);
        } else {
            return new ResponseEntity<>(Map.of("message", "unknown query type"), HttpStatus.BAD_REQUEST);
        }
        List<Map<String, Serializable>> institutions_clean = institutions.stream().map(institution -> Map.of("name", institution.getName(), "types", institution.getTypes(), "rating", institution.getRating())).toList();
        List<Map<String, Serializable>> doctors_clean = doctors.stream().map(doctor -> Map.of("name", doctor.getName(), "surname", doctor.getSurname(), "specialisations", doctor.getSpecialisations(), "rating", doctor.getRating())).toList();
        return new ResponseEntity<>(Map.of("institutions", institutions_clean, "doctors", doctors_clean), HttpStatus.OK);
    }

}
