package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.RegistrationForm;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<HashMap<String, Object>> registerUser(@RequestBody RegistrationForm registrationForm) {

        ArrayList<String> missingFields = getMissingFields(registrationForm);
        if(!missingFields.isEmpty()) {
            HashMap<String, Object> invalid = new HashMap<>();
            invalid.put("message", "Invalid fields in request body");
            invalid.put("fields", missingFields);
            return new ResponseEntity<>(invalid, HttpStatus.BAD_REQUEST);
        }
        if(userRepository.findByEmail(registrationForm.getEmail()).isPresent() || userRepository.findByGovID(registrationForm.getGovID()).isPresent()) {
            HashMap<String, Object> invalid = new HashMap<>();
            invalid.put("message", "This email or person is already registered");
            return new ResponseEntity<>(invalid, HttpStatus.CONFLICT);
        }
        Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
        String passwordHash = argon2PasswordEncoder.encode(registrationForm.getPassword());
        userRepository.save(new User(
                registrationForm.getEmail(),
                registrationForm.getName(),
                registrationForm.getSurname(),
                registrationForm.getGovID(),
                LocalDate.parse(registrationForm.getBirthDate(), DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                registrationForm.getProvince(),
                registrationForm.getCity(),
                registrationForm.getPostalCode(),
                registrationForm.getNumber(),
                registrationForm.getStreet(),
                registrationForm.getPhoneNumber(),
                passwordHash
        ));
        HashMap<String, Object> message = new HashMap<>();
        message.put("message", "Success");
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    private static ArrayList<String> getMissingFields(RegistrationForm registrationForm) {
        ArrayList<String> missingFields = new ArrayList<>();
        if(registrationForm.getName() == null || registrationForm.getName().isBlank()) {
            missingFields.add("Name");
        }
        if(registrationForm.getSurname() == null || registrationForm.getSurname().isBlank()) {
            missingFields.add("Surname");
        }
        if(registrationForm.getEmail() == null || registrationForm.getEmail().isBlank()) {
            missingFields.add("Email");
        }
        if(registrationForm.getCity() == null || registrationForm.getCity().isBlank()) {
            missingFields.add("City");
        }
        if(registrationForm.getProvince() == null || registrationForm.getProvince().isBlank()) {
            missingFields.add("Province");
        }
        if(registrationForm.getStreet() == null || registrationForm.getStreet().isBlank()) {
            missingFields.add("Street");
        }
        if(registrationForm.getNumber() == null || registrationForm.getNumber().isBlank()) {
            missingFields.add("Number");
        }
        if(registrationForm.getPostalCode() == null || registrationForm.getPostalCode().isBlank()) {
            missingFields.add("PostalCode");
        }
        if(registrationForm.getBirthDate() == null || registrationForm.getBirthDate().isBlank()) {
            missingFields.add("BirthDate");
        }
        if(registrationForm.getGovID() == null || registrationForm.getGovID().isBlank()) {
            missingFields.add("GovID");
        }
        if(registrationForm.getPhoneNumber() == null || registrationForm.getPhoneNumber().isBlank()) {
            missingFields.add("Phone");
        }
        if(registrationForm.getPassword() == null || registrationForm.getPassword().isBlank()) {
            missingFields.add("Password");
        }
        return missingFields;
    }
}
