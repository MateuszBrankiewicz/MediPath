package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.LoginForm;
import com.adam.medipathbackend.models.RegistrationForm;
import com.adam.medipathbackend.models.ResetForm;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.RestController;

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
            invalid.put("message", "missing fields in request body");
            invalid.put("fields", missingFields);
            return new ResponseEntity<>(invalid, HttpStatus.BAD_REQUEST);
        }
        if(userRepository.findByEmail(registrationForm.getEmail()).isPresent() || userRepository.findByGovID(registrationForm.getGovID()).isPresent()) {
            HashMap<String, Object> invalid = new HashMap<>();
            invalid.put("message", "this email or person is already registered");
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

    @PostMapping("/login")
    public ResponseEntity<HashMap<String, Object>> loginUser(HttpSession session, @RequestBody LoginForm loginForm) {
        ArrayList<String> missingFields = new ArrayList<>();
        if(loginForm.getEmail() == null || loginForm.getEmail().isBlank()) {
            missingFields.add("email");
        }
        if(loginForm.getPassword() == null || loginForm.getPassword().isBlank()) {
            missingFields.add("password");
        }
        if(!missingFields.isEmpty()) {
            HashMap<String, Object> invalid = new HashMap<>();
            invalid.put("message", "missing fields in request body");
            invalid.put("fields", missingFields);
            return new ResponseEntity<>(invalid, HttpStatus.BAD_REQUEST);
        }
        Optional<User> user = userRepository.findByEmail(loginForm.getEmail());
        Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
        if(user.isEmpty() || !argon2PasswordEncoder.matches(loginForm.getPassword(), user.get().getPasswordHash())) {
            HashMap<String, Object> invalid = new HashMap<>();
            invalid.put("message", "invalid email or password");
            return new ResponseEntity<>(invalid, HttpStatus.UNAUTHORIZED);
        }
        User u = user.get();
        session.setAttribute("id", u.getId());
        HashMap<String, Object> message = new HashMap<>();
        message.put("message", "success");
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PostMapping("/resetpassword")
    public ResponseEntity<HashMap<String, Object>> resetPassword(@RequestBody ResetForm resetForm) {
        if(resetForm.getEmail() == null || resetForm.getEmail().isBlank()) {
            HashMap<String, Object> invalid = new HashMap<>();
            invalid.put("message", "missing email in request body");
            return new ResponseEntity<>(invalid, HttpStatus.BAD_REQUEST);
        }
        Optional<User> u = userRepository.findByEmail(resetForm.getEmail());
        if(u.isPresent()) {
            try {
                JavaMailSenderImpl sender = new JavaMailSenderImpl();
                sender.setHost("127.0.0.1");
                sender.setPort(1025);

                MimeMessage message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message);
                helper.setFrom(new InternetAddress("service@medipath.com", "MediPath"));
                helper.setSubject("Hello, world!");
                helper.setTo(resetForm.getEmail());
                helper.setText("Thank you for ordering!");

                sender.send(message);
            } catch (MailException | MessagingException | UnsupportedEncodingException e) {
                HashMap<String, Object> invalid = new HashMap<>();
                invalid.put("message", "the mail service threw an error");
                invalid.put("error", e.getMessage());
                return new ResponseEntity<>(invalid, HttpStatus.SERVICE_UNAVAILABLE);
            }
        }
        HashMap<String, Object> valid = new HashMap<>();
        valid.put("message", "password reset mail has been sent");
        return new ResponseEntity<>(valid, HttpStatus.OK);

    }

    private static ArrayList<String> getMissingFields(RegistrationForm registrationForm) {
        ArrayList<String> missingFields = new ArrayList<>();
        if(registrationForm.getName() == null || registrationForm.getName().isBlank()) {
            missingFields.add("name");
        }
        if(registrationForm.getSurname() == null || registrationForm.getSurname().isBlank()) {
            missingFields.add("surname");
        }
        if(registrationForm.getEmail() == null || registrationForm.getEmail().isBlank()) {
            missingFields.add("email");
        }
        if(registrationForm.getCity() == null || registrationForm.getCity().isBlank()) {
            missingFields.add("city");
        }
        if(registrationForm.getProvince() == null || registrationForm.getProvince().isBlank()) {
            missingFields.add("province");
        }
        if(registrationForm.getStreet() == null || registrationForm.getStreet().isBlank()) {
            missingFields.add("street");
        }
        if(registrationForm.getNumber() == null || registrationForm.getNumber().isBlank()) {
            missingFields.add("number");
        }
        if(registrationForm.getPostalCode() == null || registrationForm.getPostalCode().isBlank()) {
            missingFields.add("postalCode");
        }
        if(registrationForm.getBirthDate() == null || registrationForm.getBirthDate().isBlank()) {
            missingFields.add("birthDate");
        }
        if(registrationForm.getGovID() == null || registrationForm.getGovID().isBlank()) {
            missingFields.add("govID");
        }
        if(registrationForm.getPhoneNumber() == null || registrationForm.getPhoneNumber().isBlank()) {
            missingFields.add("phoneNumber");
        }
        if(registrationForm.getPassword() == null || registrationForm.getPassword().isBlank()) {
            missingFields.add("password");
        }
        return missingFields;
    }
}
