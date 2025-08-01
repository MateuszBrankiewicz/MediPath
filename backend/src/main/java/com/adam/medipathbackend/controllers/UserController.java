package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.PasswordResetEntryRepository;
import com.adam.medipathbackend.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.security.SecureRandom;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetEntryRepository preRepository;

    @Autowired
    private JavaMailSender sender;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody RegistrationForm registrationForm) {

        ArrayList<String> missingFields = getMissingFields(registrationForm);
        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }
        if(userRepository.findByEmail(registrationForm.getEmail()).isPresent() || userRepository.findByGovID(registrationForm.getGovID()).isPresent()) {
            return new ResponseEntity<>(Map.of("message", "this email or person is already registered"), HttpStatus.CONFLICT);
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
        return new ResponseEntity<>(Map.of("message", "Success"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(HttpSession session, @RequestBody LoginForm loginForm) {
        ArrayList<String> missingFields = new ArrayList<>();
        if(loginForm.getEmail() == null || loginForm.getEmail().isBlank()) {
            missingFields.add("email");
        }
        if(loginForm.getPassword() == null || loginForm.getPassword().isBlank()) {
            missingFields.add("password");
        }
        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }
        Optional<User> user = userRepository.findByEmail(loginForm.getEmail());
        Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
        if(user.isEmpty() || !argon2PasswordEncoder.matches(loginForm.getPassword(), user.get().getPasswordHash())) {
            return new ResponseEntity<>(Map.of("message", "invalid email or password"), HttpStatus.UNAUTHORIZED);
        }
        User u = user.get();
        session.setAttribute("id", u.getId());
        return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.OK);
    }
    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logoutUser(HttpSession session) {
        session.invalidate();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/resetpassword")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestParam(value = "address", required = false) String address) {
        if(address == null || address.isBlank()) {
            return new ResponseEntity<>(Map.of("message", "missing email in request parameters"), HttpStatus.BAD_REQUEST);
        }
        Optional<User> u = userRepository.findByEmail(address);
        if(u.isPresent()) {
            PasswordResetEntry passwordResetEntry = null;
            try {

                MimeMessage message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message);
                helper.setFrom(new InternetAddress("service@medipath.com", "MediPath"));
                helper.setSubject("Password reset request");
                helper.setTo(address);
                SecureRandom secureRandom = new SecureRandom();
                String token = Long.toHexString(secureRandom.nextLong());
                String content = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\" />\n" +
                        "    <title>Password Reset</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <h2>MediPath</h2>\n" +
                        "    <p>We have received a password reset request for your MediPath account.<br>To reset your password click the link below:</p>\n" +
                        "    <a href=\"http://localhost:4200/resetpassword?prid="+token+"\">http://localhost:4200/resetpassword?prid="+token+"</a>\n" +
                        "    <br>\n" +
                        "    <p>The link will expire within 10 minutes</p>\n" +
                        "    <p>If you have not sent a password reset request, ignore this email.</p>\n" +
                        "    <p>-MediPath development team</p>\n" +
                        "    \n" +
                        "</body>\n" +
                        "</html>";
                passwordResetEntry = preRepository.save(new PasswordResetEntry(address, token));

                helper.setText(content);
                sender.send(message);

            } catch (MailException | MessagingException | UnsupportedEncodingException e) {
                if(passwordResetEntry != null) {
                    preRepository.delete(passwordResetEntry);
                }
                return new ResponseEntity<>(Map.of("message", "the mail service threw an error", "error", e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
            }
        }
        return new ResponseEntity<>(Map.of("message", "password reset mail has been sent, if the account exists"), HttpStatus.OK);

    }

    @PostMapping("/resetpassword")
    public ResponseEntity<Map<String, Object>> resetPasswordWithToken(@RequestBody ResetForm resetForm) {
        ArrayList<String> missingFields = new ArrayList<>();
        if(resetForm.getToken() == null || resetForm.getToken().isBlank()) {
            missingFields.add("token");
        }
        if(resetForm.getPassword() == null || resetForm.getPassword().isBlank()) {
            missingFields.add("password");
        }
        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }
        Optional<PasswordResetEntry> p = preRepository.findValidToken(resetForm.getToken());
        if(p.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "token invalid or expired"), HttpStatus.GONE);
        }

        Optional<User> u = userRepository.findByEmail(p.get().getEmail());
        if(u.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "invalid user referenced by token"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        User user = u.get();
        Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
        String passwordHash = argon2PasswordEncoder.encode(resetForm.getPassword());
        user.setPasswordHash(passwordHash);
        userRepository.save(user);
        return new ResponseEntity<>(Map.of("message", "password reset successfully"), HttpStatus.OK);
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
