package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.repository.UserRepository;
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
}
