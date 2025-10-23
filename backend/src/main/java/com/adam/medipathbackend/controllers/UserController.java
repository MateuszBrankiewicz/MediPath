package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.config.Utils;
import com.adam.medipathbackend.forms.*;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.*;
import com.adam.medipathbackend.services.UserService;
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
import java.util.HashMap;
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
    VisitRepository visitRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    MedicalHistoryRepository mhRepository;

    @Autowired
    InstitutionRepository institutionRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    private JavaMailSender sender;

    @Autowired
    private UserService userService;

    @PostMapping(value = {"/register", "/register/"})
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody RegistrationForm registrationForm) {
        try {
            Map<String, Object> result = userService.registerUser(registrationForm);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @PostMapping(value = {"/login", "/login/"})
    public ResponseEntity<Map<String, Object>> loginUser(HttpSession session, @RequestBody LoginForm loginForm) {
        try {
            String userId = userService.loginUser(loginForm);
            session.setAttribute("id", userId);
            return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(value = {"/logout", "/logout/"})
    public ResponseEntity<Map<String, Object>> logoutUser(HttpSession session) {
        session.invalidate();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = {"/patients/{patientid}", "/patients/{patientid}"})
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String patientid, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");

        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> result = userService.getPatient(loggedUserID, patientid);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = {"/profile", "/profile/"})
    public ResponseEntity<Map<String, Object>> getProfile(HttpSession session) {
        String id = (String) session.getAttribute("id");

        if (id == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> result = userService.getProfile(id);
            return new ResponseEntity<>(Map.of("user", result), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = {"/resetpassword", "/resetpassword/"})
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestParam(value = "address", required = false) String address) {
        try {
            Map<String, Object> result = userService.resetPassword(address);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @GetMapping(value = {"/me/codes/{type}", "/me/codes/", "/me/codes"})
    public ResponseEntity<Map<String, Object>> getMyReferrals(@PathVariable(required = false) String type,  HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");

        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> result = userService.getMyReferrals(loggedUserID, type);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = {"/me/visits", "/me/visits/"})
    public ResponseEntity<Map<String, Object>> getMyVisits(HttpSession session, @RequestParam(value = "upcoming", defaultValue = "") String upcoming) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if(upcoming.isBlank()) {
            return new ResponseEntity<>(Map.of("visits", visitRepository.getAllVisitsForPatient(loggedUserID)), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("visits", visitRepository.getUpcomingVisits(loggedUserID)), HttpStatus.OK);
        }

    }

    @GetMapping(value = {"/me/comments", "/me/comments/"})
    public ResponseEntity<Map<String, Object>> getMyComments(HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ArrayList<Comment> comments = commentRepository.getCommentsForUser(loggedUserID);
        if(comments.isEmpty()) {
            return new ResponseEntity<>(Map.of("comments", new ArrayList<>()), HttpStatus.OK);
        }
        return new ResponseEntity<>(Map.of(
                "comments", comments.stream().map(
                        comment -> Map.of(
                                "id", comment.getId(),
                                "doctor", comment.getDoctorDigest().getDoctorName() + " " + comment.getDoctorDigest().getDoctorSurname(),
                                "institution", comment.getInstitution().getInstitutionName(),
                                "doctorRating", comment.getDoctorRating(),
                                "institutionRating", comment.getInstitutionRating(),
                                "content", comment.getContent()))
                        .toList()
                ), HttpStatus.OK);

    }


    @PostMapping(value = {"/resetpassword", "/resetpassword/"})
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

    @PostMapping(value = {"/me/resetpassword", "/me/resetpassword/"})
    public ResponseEntity<Map<String, Object>> resetMyPassword(@RequestBody ResetMyPasswordForm form, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        Argon2PasswordEncoder argon2PasswordEncoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
        if(userOpt.isEmpty() || !argon2PasswordEncoder.matches(form.getCurrentPassword(), userOpt.get().getPasswordHash())) {
            return new ResponseEntity<>(Map.of("message", "invalid password"), HttpStatus.UNAUTHORIZED);
        }

        String passwordHash = argon2PasswordEncoder.encode(form.getNewPassword());
        User user = userOpt.get();
        user.setPasswordHash(passwordHash);
        try {

            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(new InternetAddress("service@medipath.com", "MediPath"));
            helper.setSubject("Password reset");
            helper.setTo(user.getEmail());
            String content = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\" />\n" +
                    "    <title>Password Reset</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <h2>MediPath</h2>\n" +
                    "    <p>The password to your account has been successfully reset.</p>\n" +
                    "    <br>\n" +
                    "    <p>If you have not reset your password recently, please reset your password via the \"Forgot password\" form on the login screen, as your account may be at risk.</p>\n" +
                    "    <p>-MediPath development team</p>\n" +
                    "    \n" +
                    "</body>\n" +
                    "</html>";

            helper.setText(content);
            sender.send(message);

        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            return new ResponseEntity<>(Map.of("message", "the mail service threw an error", "error", e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
        }
        userRepository.save(user);
        session.invalidate();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = {"/me/defaultpanel/{value}", "/me/defaultpanel/{value}/"})
    public ResponseEntity<Map<String, Object>> setDefaultPanel(@PathVariable String value, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        int panel;
        try {
           panel = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(Map.of("message", "invalid panel ID"), HttpStatus.BAD_REQUEST);
        }
        if(panel != 1 && panel != 2 && panel != 4 && panel != 8) {
            return new ResponseEntity<>(Map.of("message", "invalid panel ID"), HttpStatus.BAD_REQUEST);
        }
        User user = userOpt.get();
        if((user.getRoleCode() & panel) == 0) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        UserSettings settings = user.getUserSettings();
        settings.setLastPanel(panel);
        user.setUserSettings(settings);
        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = {"/me/update", "/me/update/"})
    public ResponseEntity<Map<String, Object>> updateData(@RequestBody UpdateUserForm updateUserForm, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user = userOpt.get();
        boolean anyChanges = false;
        boolean addressChanged = false;
        Address userAddress = user.getAddress();
        if(updateUserForm.getCity() != null && !updateUserForm.getCity().isBlank()) {
            userAddress.setCity(updateUserForm.getCity());
            addressChanged = true;
        }
        if(updateUserForm.getStreet() != null && !updateUserForm.getStreet().isBlank()) {
            userAddress.setStreet(updateUserForm.getStreet());
            addressChanged = true;
        }
        if(updateUserForm.getNumber() != null && !updateUserForm.getNumber().isBlank()) {
            userAddress.setNumber(updateUserForm.getNumber());
            addressChanged = true;
        }
        if(updateUserForm.getPostalCode() != null && !updateUserForm.getPostalCode().isBlank()) {
            userAddress.setPostalCode(updateUserForm.getPostalCode());
            addressChanged = true;
        }
        if(updateUserForm.getProvince() != null && !updateUserForm.getProvince().isBlank()) {
            userAddress.setProvince(updateUserForm.getProvince());
            addressChanged = true;
        }
        if(addressChanged) {
            user.setAddress(userAddress);
            anyChanges = true;
        }
        if(updateUserForm.getName() != null && !updateUserForm.getName().isBlank()) {
            user.setName(updateUserForm.getName());
            anyChanges = true;
        }
        if(updateUserForm.getSurname() != null && !updateUserForm.getSurname().isBlank()) {
            user.setSurname(updateUserForm.getSurname());
            anyChanges = true;
        }
        if(updateUserForm.getPhoneNumber() != null && !updateUserForm.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(updateUserForm.getPhoneNumber());
            anyChanges = true;
        }
        if(anyChanges) userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = {"/me/settings", "/me/settings/"})
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody UserSettings userSettings, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        User user = userOpt.get();
        if(userSettings.getLanguage() == null || userSettings.getLanguage().isBlank()) {
            userSettings.setLanguage(user.getUserSettings().getLanguage());
        }

        if(!user.getUserSettings().equals(userSettings))  {
            user.setUserSettings(userSettings);
            userRepository.save(user);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping(value = {"/me/settings", "/me/settings/"})
    public ResponseEntity<Map<String, Object>> getSettings(HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(Map.of("settings", userOpt.get().getUserSettings()), HttpStatus.OK);
    }

    @GetMapping(value = {"/me/medicalhistory", "/me/medicalhistory/"})
    public ResponseEntity<Map<String, Object>> getMedicalHistory(HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(Map.of("medicalhistories", mhRepository.getEntriesForPatient(loggedUserID)), HttpStatus.OK);
    }


    @GetMapping(value = {"/{userid}/getpfp", "/{userid}/getpfp/"})
    public ResponseEntity<Map<String, Object>> getPfp(@PathVariable String userid, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(userid);
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(Map.of("pfp", ""), HttpStatus.OK);
        }
        User user = userOpt.get();
        return new ResponseEntity<>(Map.of("pfp", user.getPfpimage()), HttpStatus.OK);
    }

    @GetMapping(value = {"/me/notifications", "/me/notifications/"})
    public ResponseEntity<Map<String, Object>> getNotifications(HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(Map.of("notifications", new ArrayList<>()), HttpStatus.OK);
        }
        User user = userOpt.get();
        return new ResponseEntity<>(Map.of("notifications", user.getNotifications().stream()
                .map(notification -> Map.of("title", notification.getTitle(), "content",
                        notification.getContent(), "timestamp", notification.getTimestamp().toString(), "read",
                        notification.isRead(), "system", notification.isSystem())
        ).toList()), HttpStatus.OK);
    }

    @GetMapping(value = {"/me/institutions", "/me/institutions/"})
    public ResponseEntity<Map<String, Object>> getMyInstitutions(HttpSession session, @RequestParam(value = "role") String role) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(Map.of("institutions", new ArrayList<>()), HttpStatus.OK);
        }
        User admin = userOpt.get();
        if(role.equals("admin")) {
            if(admin.getRoleCode() < 8) {
                return new ResponseEntity<>(Map.of("institutions", new ArrayList<>()), HttpStatus.OK);
            }
            ArrayList<Institution> institutions = institutionRepository.findInstitutionsWhereAdmin(loggedUserID);
            return new ResponseEntity<>(Map.of("institutions", institutions), HttpStatus.OK);
        } else if(role.equals("staff")) {
            if(admin.getRoleCode() < 4) {
                return new ResponseEntity<>(Map.of("institutions", new ArrayList<>()), HttpStatus.OK);
            }
            ArrayList<Institution> institutions = institutionRepository.findInstitutionsWhereStaff(loggedUserID);
            return new ResponseEntity<>(Map.of("institutions", institutions), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Map.of("message", "invalid role"), HttpStatus.BAD_REQUEST);
        }


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
