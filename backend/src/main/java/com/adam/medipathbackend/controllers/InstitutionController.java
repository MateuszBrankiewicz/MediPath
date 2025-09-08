package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/institution")
public class InstitutionController {

    @Autowired
    InstitutionRepository institutionRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addInstitution(@RequestBody Institution institution) {
        ArrayList<String> missingFields = new ArrayList<>();

        if(institution.getAddress() == null || !institution.getAddress().isValid()) {
            missingFields.add("address");
        }
        if(institution.getName() == null || institution.getName().isBlank()) {
            missingFields.add("name");
        }

        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }

        ArrayList<Institution> possibleDuplicates = institutionRepository.findInstitutionByName(institution.getName());
        for(Institution dupl: possibleDuplicates) {
            if(dupl.isSimilar(institution)) {
                return new ResponseEntity<>(Map.of("message", "This institution is a possible duplicate"), HttpStatus.CONFLICT);
            }
        }

        institutionRepository.save(institution);
        return new ResponseEntity<>(Map.of("message", "Success"), HttpStatus.CREATED);
    }

    @PostMapping("/{institutionid}/addemployees/")
    public ResponseEntity<Map<String, Object>> addEmployeeToInstitution(@PathVariable String institutionid, @RequestBody ArrayList<AddEmployeeForm> employeeIds) {

        if(employeeIds.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "list of users is empty"), HttpStatus.BAD_REQUEST);
        }

        Optional<Institution> optionalInstitution = institutionRepository.findById(institutionid);
        if(optionalInstitution.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "invalid institution id"), HttpStatus.BAD_REQUEST);
        }

        for(AddEmployeeForm employee: employeeIds) {
            if(!userRepository.existsById(employee.getUserID())) {
                return new ResponseEntity<>(Map.of("message", "one or more user IDs is invalid"), HttpStatus.BAD_REQUEST);
            }
        }

        Institution currentInstitution = optionalInstitution.get();
        for(AddEmployeeForm employee: employeeIds) {

            Optional<User> currentUserOpt = userRepository.findById(employee.getUserID());

            if(currentUserOpt.isPresent()) {

                User currentUser = currentUserOpt.get();
                StaffDigest digest = new StaffDigest(currentUser.getId(), currentUser.getName(), currentUser.getSurname(), employee.getSpecialisations(), employee.getRoleCode());

                currentInstitution.addEmployee(digest);
                currentUser.setRoleCode(currentUser.getRoleCode() | employee.getRoleCode());

                InstitutionDigest institutionDigest = new InstitutionDigest(currentInstitution.getId(), currentInstitution.getName());
                currentUser.addEmployer(institutionDigest);
                userRepository.save(currentUser);
            }
        }

        institutionRepository.save(currentInstitution);
        return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.OK);
    }



}
