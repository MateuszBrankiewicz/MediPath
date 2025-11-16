package com.adam.medipathbackend.controllers;


import com.adam.medipathbackend.forms.AddScheduleForm;
import com.adam.medipathbackend.forms.ManySchedulesUpdateForm;
import com.adam.medipathbackend.models.*;
import com.adam.medipathbackend.services.ScheduleService;
import com.adam.medipathbackend.services.AuthorizationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    ScheduleService scheduleService;
    @Autowired
    AuthorizationService authorizationService;

    @PostMapping(value = {"/add", "/add/"})
    public ResponseEntity<Map<String, Object>> add(@RequestBody AddScheduleForm schedule, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            scheduleService.addSchedule(schedule, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @PostMapping(value = {"/addmany", "/addmany/"})
    public ResponseEntity<Map<String, Object>> addMany(@RequestBody AddScheduleForm schedule, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
                    System.out.println("DEBUG: Starting addManySchedules for user: " + loggedUserID);

            scheduleService.addManySchedules(schedule, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
                    System.out.println("DEBUG: IllegalArgumentException - " + e.getMessage());

            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
                    System.out.println("DEBUG: IllegalAccessException - " + e.getMessage());

            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
                    System.out.println("DEBUG: IllegalStateException - " + e.getMessage());

            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @PutMapping(value = {"/updatemany", "/updatemany/"})
    public ResponseEntity<Map<String, Object>> updateManySchedules(@RequestBody ManySchedulesUpdateForm newSchedule, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            scheduleService.updateManySchedules(newSchedule, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @PutMapping(value = {"/{scheduleid}", "/{scheduleid}/"})
    public ResponseEntity<Map<String, Object>> updateSchedule(@PathVariable String scheduleid, @RequestBody AddScheduleForm newSchedule, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            scheduleService.updateSchedule(scheduleid, newSchedule, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        }
    }



    @DeleteMapping(value = {"/{scheduleid}", "/{scheduleid}/"})
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable String scheduleid, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            scheduleService.deleteSchedule(scheduleid, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "success"), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.CONFLICT);
        }
    }

}
