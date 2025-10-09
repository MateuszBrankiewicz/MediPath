package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.forms.AddNotificationForm;
import com.adam.medipathbackend.models.Notification;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.models.Visit;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.repository.VisitRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    VisitRepository visitRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping(value = {"/add", "/add/"})
    public ResponseEntity<Map<String, Object>> addNotification(@RequestBody AddNotificationForm notificationForm, HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ArrayList<String> missingFields = getMissingFields(notificationForm);
        if(!missingFields.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "missing fields in request body", "fields", missingFields), HttpStatus.BAD_REQUEST);
        }
        Optional<User> optUser;
        if(notificationForm.getUserId() == null || notificationForm.getUserId().isBlank()) {
            optUser = userRepository.findById(loggedUserID);
            if(optUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else {
            optUser = userRepository.findById(notificationForm.getUserId());
            if(optUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            ArrayList<Visit> patientsvisits = visitRepository.getAllVisitsForPatient(notificationForm.getUserId());
            if(patientsvisits.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            Set<String> doctors = patientsvisits.stream().map(visit -> visit.getDoctor().getUserId()).collect(Collectors.toSet());
            if(!doctors.contains(loggedUserID)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        User user = optUser.get();

        LocalDate startDate = notificationForm.getStartDate();

        LocalDate endDate = notificationForm.getEndDate() == null ? startDate : notificationForm.getEndDate();

        String content = notificationForm.getContent() == null ? "" : notificationForm.getContent();


        if(startDate.isBefore(LocalDate.now())) {
            return new ResponseEntity<>(Map.of("message", "start date is not future"), HttpStatus.BAD_REQUEST);
        }
        if(endDate.isBefore(startDate)) {
            return new ResponseEntity<>(Map.of("message", "end date is before start date"), HttpStatus.BAD_REQUEST);
        }
        while(startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
            user.addNotification(new Notification(notificationForm.getTitle(), content, startDate.atTime(notificationForm.getReminderTime()), false, false));
            startDate = startDate.plusDays(1);
        }

        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.CREATED);


    }

    @PostMapping(value = {"/read/", "/read"})
    public ResponseEntity<Map<String, Object>> readNotification(@RequestBody Notification notifToChange,  HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        User user =  userOpt.get();
        ArrayList<Notification> notifications = user.getNotifications();
        for(int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            if(notification.getTimestamp().isEqual(notifToChange.getTimestamp()) && notification.getTitle().equals(notifToChange.getTitle())) {
                notification.setRead(true);
                notifications.set(i, notification);
                break;
            }
        }
        user.setNotifications(notifications);
        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);

    }
    @PostMapping(value = {"/readall/", "/readall"})
    public ResponseEntity<Map<String, Object>> readNotification(HttpSession session) {
        String loggedUserID = (String) session.getAttribute("id");
        if(loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if(userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        User user =  userOpt.get();
        ArrayList<Notification> notifications = user.getNotifications();
        for(int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notification.setRead(true);
            notifications.set(i, notification);
        }
        user.setNotifications(notifications);
        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    private static ArrayList<String> getMissingFields(AddNotificationForm notificationForm) {
        ArrayList<String> missingFields = new ArrayList<>();
        if(notificationForm.getStartDate() == null) {
            missingFields.add("startDate");
        }
        if(notificationForm.getReminderTime() == null) {
            missingFields.add("reminderTime");
        }
        if(notificationForm.getTitle() == null || notificationForm.getTitle().isBlank()) {
            missingFields.add("title");
        }
        return missingFields;
    }
}
