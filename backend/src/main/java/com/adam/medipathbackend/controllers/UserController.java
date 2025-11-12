package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.forms.*;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.*;
import com.adam.medipathbackend.services.UserService;
import jakarta.mail.IllegalWriteException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.Map;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/users")
public class UserController {


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
    public ResponseEntity<Map<String, Object>> loginUser(HttpSession session, 
                                                        @RequestBody LoginForm loginForm) {
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

    @GetMapping(value = "/find/{govid}")
    public ResponseEntity<Map<String, Object>> findByGovID(@PathVariable String govid, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");

        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            return new ResponseEntity<>(userService.findEmployeeByGovId(govid, loggedUserID), HttpStatus.OK);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
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
        return new ResponseEntity<>(Map.of("visits", userService.getMyVisits(loggedUserID, upcoming)), HttpStatus.OK);

    }

    @GetMapping(value = {"/me/comments", "/me/comments/"})
    public ResponseEntity<Map<String, Object>> getMyComments(HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(userService.getMyComments(loggedUserID), HttpStatus.OK);
    }


    @PostMapping(value = {"/resetpassword", "/resetpassword/"})
    public ResponseEntity<Map<String, Object>> resetPasswordWithToken(@RequestBody ResetForm resetForm) {
        try {
            userService.postResetPassword(resetForm);
            return new ResponseEntity<>(Map.of("message", "password reset successfully"), HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch(IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.GONE);
        } catch(IllegalWriteException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = {"/me/resetpassword", "/me/resetpassword/"})
    public ResponseEntity<Map<String, Object>> resetMyPassword(@RequestBody ResetMyPasswordForm form, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            userService.resetMyPassword(loggedUserID, form);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalWriteException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
        } catch(IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.UNAUTHORIZED);
        }


    }

    @PutMapping(value = {"/me/defaultpanel/{value}", "/me/defaultpanel/{value}/"})
    public ResponseEntity<Map<String, Object>> setDefaultPanel(@PathVariable String value, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            userService.updatePanel(value, loggedUserID);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = {"/me/update", "/me/update/"})
    public ResponseEntity<Map<String, Object>> updateData(@RequestBody UpdateUserForm updateUserForm, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            userService.updateMe(updateUserForm, loggedUserID);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping(value = {"/me/settings", "/me/settings/"})
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody UserSettings userSettings, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            userService.updateSettings(userSettings, loggedUserID);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    @GetMapping(value = {"/me/settings", "/me/settings/"})
    public ResponseEntity<Map<String, Object>> getSettings(HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            UserSettings us = userService.getUserSettings(loggedUserID);
            return new ResponseEntity<>(Map.of("settings", us), HttpStatus.OK);
        } catch(IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(value = {"/me/medicalhistory", "/me/medicalhistory/"})
    public ResponseEntity<Map<String, Object>> getMedicalHistory(HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(Map.of("medicalhistories", userService.getMyMedicalHistories(loggedUserID)), HttpStatus.OK);
    }

    @GetMapping(value = {"/{id}/medicalhistory", "/{id}/medicalhistory/"})
    public ResponseEntity<Map<String, Object>> getPatientsMedicalHistory(HttpSession session, @PathVariable String id) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            return new ResponseEntity<>(Map.of("medicalhistories", userService.getMedicalHistoriesForPatient(loggedUserID, id)), HttpStatus.OK);
        } catch(IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }


    @GetMapping(value = {"/me/notifications", "/me/notifications/"})
    public ResponseEntity<Map<String, Object>> getNotifications(HttpSession session, @RequestParam(value = "filter", required = false) String filter) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            return new ResponseEntity<>(Map.of("notifications",
                    userService.getMyNotifications(loggedUserID, filter)), HttpStatus.OK);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = {"/me/institutions", "/me/institutions/"})
    public ResponseEntity<Map<String, Object>> getMyInstitutions(HttpSession session, @RequestParam(value = "role") String role) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            return new ResponseEntity<>(Map.of("institutions",
                    userService.getMyInstitutions(loggedUserID, role)), HttpStatus.OK);

        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = {"/me/deactivate", "/me/deactivate/"})
    public ResponseEntity<Map<String, Object>> deactivateUser(HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {

            userService.deactivateMe(loggedUserID);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

}
