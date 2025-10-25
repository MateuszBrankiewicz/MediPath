package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.forms.AddNotificationForm;
import com.adam.medipathbackend.models.Notification;
// ...existing code...
// ...existing code...
import com.adam.medipathbackend.services.NotificationService;
import jakarta.servlet.http.HttpSession;
// ...existing code...
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ...existing code...
// ...existing code...
// ...existing code...
import java.util.Map;
// ...existing code...
// ...existing code...
// ...existing code...

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @PostMapping(value = {"/add", "/add/"})
    public ResponseEntity<Map<String, Object>> addNotification(@RequestBody AddNotificationForm notificationForm, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {

            notificationService.addNotification(notificationForm, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "Notification added successfully"), HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping(value = {"/read/", "/read"})
    public ResponseEntity<Map<String, Object>> readNotification(@RequestBody Notification notifToChange, HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {

            notificationService.readNotification(notifToChange, loggedUserID);
            return new ResponseEntity<>(Map.of("message", "Notification marked as read"), HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }
    @PostMapping(value = {"/readall/", "/readall"})
    public ResponseEntity<Map<String, Object>> readAllNotifications(HttpSession session) {

        String loggedUserID = (String) session.getAttribute("id");
        if (loggedUserID == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {

            notificationService.readAllNotifications(loggedUserID);
            return new ResponseEntity<>(Map.of("message", "All notifications marked as read"), HttpStatus.OK);

        }
        catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalAccessException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }
}
