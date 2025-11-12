package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.forms.AddComboForm;
import com.adam.medipathbackend.forms.AddEmployeeForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.services.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.*;

@RestController
@RequestMapping("/api/institution")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private EmployeeManagementService employeeManagementService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private InstitutionQueryService queryService;

    @Autowired
    private EmailService emailService;


    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = { "/add", "/add/" })
    public ResponseEntity<Map<String, Object>> addInstitution(@RequestBody Institution institution,
            HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<User> adminOpt = userRepository.findAdminById(loggedUserID);
        if (adminOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        User admin = adminOpt.get();

        try {
            institutionService.createInstitution(institution, admin);
            return new ResponseEntity<>(Map.of("message", "Success"), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @PostMapping(value = { "/{institutionid}/employees/", "/{institutionid}/employees" })
    public ResponseEntity<Map<String, Object>> addEmployeeToInstitution(@PathVariable String institutionid,
                                                                        @RequestBody ArrayList<AddEmployeeForm> employeeIds, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            authorizationService.startAuthChain(loggedUserID, institutionid).adminOfInstitution().check();
            employeeManagementService.addEmployeesToInstitution(institutionid, employeeIds);

            return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Database error in addEmployeeToInstitution: " + e.getMessage());
            return new ResponseEntity<>(
                    Map.of("message", "Database connection error", "error", e.getMessage()),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @GetMapping(value = { "/{institutionid}/doctors", "/{institutionid}/doctors/" })
    public ResponseEntity<Map<String, Object>> getDoctors(@PathVariable String institutionid,
            @RequestParam(required = false) String specialisation) {
        if (!institutionService.institutionExists(institutionid)) {
            return new ResponseEntity<>(Map.of("message", "invalid institution id"), HttpStatus.BAD_REQUEST);
        }

        List<Map<String, Object>> doctors = queryService.getDoctors(institutionid, specialisation);
        return new ResponseEntity<>(Map.of("doctors", doctors), HttpStatus.OK);
    }

    @PostMapping(value = { "/{institutionid}/employee/register", "/{institutionid}/employee/register/" })
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody AddComboForm comboForm, HttpSession session,
            @PathVariable String institutionid) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {

            authorizationService.startAuthChain(loggedUserID, institutionid).adminOfInstitution().check();

            Institution institution = institutionService.getInstitution(institutionid)
                    .orElseThrow(() -> new IllegalArgumentException("Institution not found"));

            User newUser = employeeManagementService.registerEmployee(comboForm, institutionid);
            PasswordResetEntry passwordResetEntry = employeeManagementService
                    .createPasswordResetEntry(newUser.getEmail());

            try {
                emailService.sendEmployeeRegistrationEmail(newUser, institution, passwordResetEntry.getToken());
            } catch (MailException | MessagingException | UnsupportedEncodingException e) {
                employeeManagementService.deletePasswordResetEntry(passwordResetEntry);
                return new ResponseEntity<>(
                        Map.of("message", "The mail service threw an error", "error", e.getMessage()),
                        HttpStatus.SERVICE_UNAVAILABLE);
            }

            return new ResponseEntity<>(Map.of("message", "Success"), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PutMapping(value = { "/{institutionid}/employee", "/{institutionid}/employee/" })
    public ResponseEntity<Map<String, Object>> editEmployee(@PathVariable String institutionid,
            @RequestBody AddEmployeeForm employeeUpdate, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {

                         authorizationService.startAuthChain(loggedUserID, institutionid).adminOfInstitution().check();

            employeeManagementService.updateEmployee(institutionid, employeeUpdate, loggedUserID);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping(value = { "/{institutionid}/employee/{userId}", "/{institutionid}/employee/{userId}/" })
    public ResponseEntity<Map<String, Object>> removeEmployee(@PathVariable String institutionid,
            @PathVariable String userId, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {

            authorizationService.startAuthChain(loggedUserID, institutionid).adminOfInstitution().check();

            employeeManagementService.removeEmployee(institutionid, userId, loggedUserID);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping(value = { "/{id}", "/{id}/" })
    public ResponseEntity<Map<String, Object>> getInstitution(@PathVariable String id,
            @RequestParam(value = "fields", required = false) String[] fields, HttpSession session) {
        try {
            String loggedUserID = (String) session.getAttribute("id");
            boolean isEmployee = false;
            try {
                authorizationService.startAuthChain(loggedUserID, id).employeeOfInstitution().check();
                isEmployee = true;
            } catch (IllegalAccessException _) {

            }

            Map<String, Object> institutionFields = queryService.getInstitutionFields(id, fields, isEmployee);
            return new ResponseEntity<>(Map.of("institution", institutionFields), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = { "/{institutionid}/upcomingvisits/", "/{institutionid}/upcomingvisits" })
    public ResponseEntity<Map<String, Object>> getUpcomingVisitsForInstitution(@PathVariable String institutionid,
            HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            authorizationService.startAuthChain(loggedUserID, institutionid).employeeOfInstitution().check();
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }


        ArrayList<Visit> visits = queryService.getUpcomingVisits(institutionid);
        return new ResponseEntity<>(Map.of("visits", visits), HttpStatus.OK);
    }

    @PutMapping(value = { "/{institutionid}", "/{institutionid}/" })
    public ResponseEntity<Map<String, Object>> updateInstitution(@PathVariable String institutionid,
            @RequestBody Institution newInstitution, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            authorizationService.startAuthChain(loggedUserID, institutionid).adminOfInstitution().check();
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }



        try {
            institutionService.updateInstitution(institutionid, newInstitution);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @GetMapping(value = { "/{institutionid}/schedules/{date}", "/{institutionid}/schedules/{date}/",
            "/{institutionid}/schedules/", "/{institutionid}/schedules" })
    public ResponseEntity<Map<String, Object>> getSchedulesForInstitution(@PathVariable String institutionid,
            @PathVariable(required = false) String date, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }



        try {
            authorizationService.startAuthChain(loggedUserID, institutionid).employeeOfInstitution().check();

            ArrayList<Schedule> schedules = queryService.getSchedules(institutionid, date);
            return new ResponseEntity<>(Map.of("schedules", schedules), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping(value = { "/{institutionid}/visits/{date}", "/{institutionid}/visits/{date}/",
            "/{institutionid}/visits/", "/{institutionid}/visits" })
    public ResponseEntity<Map<String, Object>> getVisitsForInstitution(@PathVariable String institutionid,
            @PathVariable(required = false) String date, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            authorizationService.startAuthChain(loggedUserID, institutionid).employeeOfInstitution().check();

            ArrayList<Visit> visits = queryService.getVisits(institutionid, date);
            return new ResponseEntity<>(Map.of("visits", visits), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping(value = {"/{institutionid}/deactivate", "/{institutionid}/deactivate/"})
    public ResponseEntity<Map<String, Object>> deactivateInstitution(HttpSession session, @PathVariable String institutionid) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            authorizationService.startAuthChain(loggedUserID, institutionid).adminOfInstitution().check();
            institutionService.deactivateInstitution(institutionid);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
