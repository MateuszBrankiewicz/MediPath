package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.Institution;
import com.adam.medipathbackend.models.StaffDigest;
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


    @GetMapping(value = {"/search/{query}", "/search/{query}/{city}"})
    public ResponseEntity<Map<String, Object>> search(@PathVariable String query, @PathVariable(required = false) String city) {
        ArrayList<Institution> institutions;
        ArrayList<StaffDigest> doctors;

        city = city == null ? ".*" : city;
        institutions = institutionRepository.findInstitutionByCity(city, query);
        doctors = institutionRepository.findDoctorsByCity(city, query);

        List<Map<String, Serializable>> institutions_clean = institutions.stream().map(institution -> Map.of("name", institution.getName(), "types", institution.getTypes())).toList();
        List<Map<String, Serializable>> doctors_clean = doctors.stream().map(doctor -> Map.of("name", doctor.getName(), "surname", doctor.getSurname(), "specialisations", doctor.getSpecialisations())).toList();
        return new ResponseEntity<>(Map.of("institutions", institutions_clean, "doctors", doctors), HttpStatus.OK);
    }

}
