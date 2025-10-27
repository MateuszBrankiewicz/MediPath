package com.adam.medipathbackend.services;

import com.adam.medipathbackend.forms.AddNotificationForm;
import com.adam.medipathbackend.models.Notification;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.models.Visit;
import com.adam.medipathbackend.repository.UserRepository;
import com.adam.medipathbackend.repository.VisitRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;


    @Autowired
    UserRepository userRepository;

    @Autowired
    VisitRepository visitRepository;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotificationToUser(String userId, Notification notification) {
        messagingTemplate.convertAndSendToUser(userId, "/notifications", notification);
    }

    public void broadcastNotification(Notification notification) {
        messagingTemplate.convertAndSend("/systemnotifications", notification);
    }

    public void addNotification(AddNotificationForm notificationForm, String loggedUserID) throws IllegalArgumentException, IllegalAccessException {

        ArrayList<String> missingFields = getMissingFields(notificationForm);
        if (!missingFields.isEmpty()) throw new IllegalArgumentException("Missing fields: " + missingFields);

        Optional<User> optUser;
        if (notificationForm.getUserId() == null || notificationForm.getUserId().isBlank()) {

            optUser = userRepository.findById(loggedUserID);
            if (optUser.isEmpty()) throw new IllegalAccessException("User not found");

        } else {

            optUser = userRepository.findById(notificationForm.getUserId());
            if (optUser.isEmpty()) throw new IllegalAccessException("Patient not found");

            ArrayList<Visit> patientsvisits = visitRepository.getAllVisitsForPatient(notificationForm.getUserId());
            if (patientsvisits.isEmpty()) throw new IllegalAccessException("No visits found for patient");

            Set<String> doctors = patientsvisits.stream().map(visit -> visit.getDoctor().getUserId()).collect(java.util.stream.Collectors.toSet());
            if (!doctors.contains(loggedUserID)) throw new IllegalAccessException("Doctor not authorized");

        }

        User user = optUser.get();
        LocalDate startDate = notificationForm.getStartDate();
        LocalDate endDate = notificationForm.getEndDate() == null ? startDate : notificationForm.getEndDate();
        String content = notificationForm.getContent() == null ? "" : notificationForm.getContent();

        if (startDate.isBefore(java.time.LocalDate.now())) throw new IllegalArgumentException("Start date is not future");
        if (endDate.isBefore(startDate)) throw new IllegalArgumentException("End date is before start date");

        while (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
            user.addNotification(new Notification(notificationForm.getTitle(), content, startDate.atTime(notificationForm.getReminderTime()), false, false));
            startDate = startDate.plusDays(1);
        }

        userRepository.save(user);
    }

    public void readNotification(Notification notifToChange, String loggedUserID) throws IllegalArgumentException, IllegalAccessException {

        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if (userOpt.isEmpty()) throw new IllegalAccessException("User not found");

        User user = userOpt.get();
        ArrayList<Notification> notifications = user.getNotifications();

        boolean found = false;
        for (int i = 0; i < notifications.size(); i++) {

            Notification notification = notifications.get(i);
            if (notification.getTimestamp().isEqual(notifToChange.getTimestamp()) && notification.getTitle().equals(notifToChange.getTitle())) {

                if (notification.getTimestamp().isAfter(java.time.LocalDateTime.now())) throw new IllegalArgumentException("Notification set in future");
                notification.setRead(true);
                notifications.set(i, notification);
                found = true;
                break;

            }
        }

        if (!found) throw new IllegalArgumentException("Notification not found");
        user.setNotifications(notifications);
        userRepository.save(user);

    }

    public void readAllNotifications(String loggedUserID) throws IllegalArgumentException, IllegalAccessException {

        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if (userOpt.isEmpty()) throw new IllegalAccessException("User not found");

        User user = userOpt.get();
        ArrayList<Notification> notifications = user.getNotifications();

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);

            if (!notification.getTimestamp().isAfter(java.time.LocalDateTime.now())) {
                notification.setRead(true);
                notifications.set(i, notification);
            }
        }

        user.setNotifications(notifications);
        userRepository.save(user);
    }

    private static ArrayList<String> getMissingFields(AddNotificationForm notificationForm) {

        ArrayList<String> missingFields = new ArrayList<>();

        if (notificationForm.getStartDate() == null) missingFields.add("startDate");
        if (notificationForm.getReminderTime() == null) missingFields.add("reminderTime");
        if (notificationForm.getTitle() == null || notificationForm.getTitle().isBlank()) missingFields.add("title");

        return missingFields;
    }

    private boolean isTimeBetween(LocalDateTime timeStamp, LocalDateTime lower, LocalDateTime upper) {
        return !(timeStamp.isBefore(lower) || timeStamp.isAfter(upper));
    }

    private boolean notificationMatches(Notification notification, AddNotificationForm notificationForm) {
        return isTimeBetween(notification.getTimestamp(), notificationForm.getStartDate().atStartOfDay(), notificationForm.getEndDate().plusDays(1).atStartOfDay())
                && notification.getTitle().equals(notificationForm.getTitle())
                && notification.getTimestamp().toLocalTime().equals(notificationForm.getReminderTime());
    }


    public void removeNotifications(AddNotificationForm notificationForm, String loggedUserID) throws IllegalAccessException {
        Optional<User> userOpt = userRepository.findById(loggedUserID);
        if (userOpt.isEmpty()) throw new IllegalAccessException("User not found");

        if(notificationForm.getTitle() == null || notificationForm.getTitle().isBlank()) {
            throw new IllegalArgumentException("missing title");
        }
        if(notificationForm.getReminderTime() == null) {
            throw new IllegalArgumentException("missing reminderTime");
        }
        if(notificationForm.getStartDate() == null) {
            throw new IllegalArgumentException("missing startDate");
        }
        if(notificationForm.getEndDate() == null) {
            throw new IllegalArgumentException("missing endDate");
        }


        User user = userOpt.get();
        ArrayList<Notification> notifications = user.getNotifications();

        if (!notifications.removeIf(notification -> notificationMatches(notification, notificationForm)))
            throw new IllegalArgumentException("No notification matches criteria");

        user.setNotifications(notifications);
        userRepository.save(user);
    }
}
