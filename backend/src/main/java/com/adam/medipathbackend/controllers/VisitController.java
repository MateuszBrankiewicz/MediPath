package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.forms.AddVisitForm;
import com.adam.medipathbackend.forms.CompleteVisitForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.services.CodeService;
import com.adam.medipathbackend.services.VisitService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.zone.ZoneRulesProvider;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    @Autowired
    private VisitService visitService;

    @Autowired
    private CodeService codeService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = {"/add", "/add/"})
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddVisitForm visit, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> optionalUser = userRepository.findById(loggedUserID);
        if(optionalUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            visitService.addVisit(visit, optionalUser.get());
            return new ResponseEntity<>(Map.of("message", "Success"), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }

    @DeleteMapping(value = {"/{visitid}", "/{visitid}/"})
    public ResponseEntity<Map<String, Object>> cancelVisit(@PathVariable String visitid, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        try {
            visitService.cancelVisit(visitid, loggedUserID);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(IllegalStateException ise) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException iae) {
            return new ResponseEntity<>(Map.of("message", iae.getMessage()), HttpStatus.BAD_REQUEST);
        } catch(IllegalAccessException iae) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping(value = {"/{visitid}/reschedule/", "/{visitid}/reschedule"})
    public ResponseEntity<Map<String, Object>> rescheduleVisit(@PathVariable String visitid, @RequestParam(value = "newschedule", defaultValue = "") String newScheduleId, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        try {
            visitService.rescheduleVisit(visitid, newScheduleId, loggedUserID);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(IllegalAccessException ise) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException iae) {
            return new ResponseEntity<>(Map.of("message", iae.getMessage()), HttpStatus.BAD_REQUEST);
        } catch(IllegalComponentStateException iae) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @PutMapping(value = {"/{visitid}/complete/", "/{visitid}/complete"})
    public ResponseEntity<Map<String, String>> completeVisit(@PathVariable String visitid, @RequestBody CompleteVisitForm completionForm, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        try {
            visitService.completeVisit(visitid, completionForm, loggedUserID);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(IllegalAccessException ise) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException iae) {
            return new ResponseEntity<>(Map.of("message", iae.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = {"/{visitid}", "/{visitid}/"})
    public ResponseEntity<Map<String, Object>> getVisitDetails(@PathVariable String visitid, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            Visit visit = visitService.getVisitDetails(visitid, loggedUserID);
            return new ResponseEntity<>(Map.of("visit", visit), HttpStatus.OK);
        } catch(IllegalAccessException ise) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    @PutMapping(value = {"/code", "/code/"})
    public ResponseEntity<Map<String, Object>> updateCode(@RequestBody Code code, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new org.springframework.http.ResponseEntity<>(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        try {
            boolean found = codeService.updateCode(code, loggedUserID);
            return new ResponseEntity<>(found ? org.springframework.http.HttpStatus.OK : org.springframework.http.HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException iae) {
            return new ResponseEntity<>(Map.of("message", iae.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = {"/code", "/code/"})
    public ResponseEntity<Map<String, Object>> deleteeCode(@RequestBody Code code, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new org.springframework.http.ResponseEntity<>(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        try {
            boolean found = codeService.deleteCode(code, loggedUserID);
            return new ResponseEntity<>(found ? org.springframework.http.HttpStatus.OK
                    : org.springframework.http.HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException iae) {
            return new ResponseEntity<>(Map.of("message", iae.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
