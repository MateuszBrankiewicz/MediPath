package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.InstitutionRepository;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/institution")
public class InstitutionController {

    @Autowired
    InstitutionRepository institutionRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping(value = {"/add", "/add/"})
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

    @PostMapping(value = {"/{institutionid}/addemployees/", "/{institutionid}/addemployees"})
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
                StaffDigest digest = new StaffDigest(currentUser.getId(), currentUser.getName(), currentUser.getSurname(), employee.getSpecialisations(), employee.getRoleCode(), currentUser.getPfpimage());

                currentInstitution.addEmployee(digest);
                currentUser.setRoleCode(currentUser.getRoleCode() | employee.getRoleCode());

                InstitutionDigest institutionDigest = new InstitutionDigest(currentInstitution.getId(), currentInstitution.getName()
                );
                currentUser.addEmployer(institutionDigest);
                userRepository.save(currentUser);
            }
        }

        institutionRepository.save(currentInstitution);
        return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.OK);
    }


    @GetMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<Map<String, Object>> getInstitution(@PathVariable String id, @RequestParam(value = "fields", required = false) String[] fields) {
        Optional<Institution> institutionOptional = institutionRepository.findById(id);
        if(institutionOptional.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "invalid institution id"), HttpStatus.BAD_REQUEST);
        }
        Institution institution = institutionOptional.get();
        Map<String, Object> outputFields = new HashMap<>();
        System.out.println(fields == null);
        List<String> fieldsList;
        if(fields == null) {
            fieldsList = List.of("id", "name", "types", "isPublic", "address", "employees", "rating", "image");
        } else {
            fieldsList = List.of(fields);
        }

        if(fieldsList.contains("id")) {
            outputFields.put("id", institution.getId());
        }
        if(fieldsList.contains("name")) {
            outputFields.put("name", institution.getName());
        }
        if(fieldsList.contains("address")) {
            outputFields.put("address", institution.getAddress());
        }
        if(fieldsList.contains("isPublic")) {
            outputFields.put("isPublic", institution.isPublic());
        }
        if(fieldsList.contains("types")) {
            outputFields.put("types", institution.getTypes());
        }
        if(fieldsList.contains("employees")) {
            int[] validDoctorCodes = {2, 3, 6, 7, 14, 15};
            outputFields.put("employees", institution.getEmployees().stream().filter(employee -> IntStream.of(validDoctorCodes).anyMatch(x -> x == employee.getRoleCode())));
        }
        if(fieldsList.contains("rating")) {
            outputFields.put("rating", institution.getRating());
        }
        if(fieldsList.contains("image")) {
            outputFields.put("image", institution.getImage());
        }
        return new ResponseEntity<>(Map.of("institution", outputFields), HttpStatus.OK);
    }


}
