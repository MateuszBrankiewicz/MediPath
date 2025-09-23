package com.adam.medipathbackend.services;

import com.adam.medipathbackend.models.Notification;
import com.adam.medipathbackend.models.User;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationSender {


    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    public void pushNotifications() {
        List<User> users = userRepository.findAll();
        for(User user: users) {
            boolean gotModified = false;
            for(Notification notification: user.getNotifications()) {
                if(notification.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(1)) && notification.getTimestamp().isBefore(LocalDateTime.now().plusMinutes(1))) {
                    notificationService.sendNotificationToUser(user.getId(), notification);
                }
                if(notification.getTimestamp().isBefore(LocalDateTime.now().minusMonths(1))) {
                    user.removeNotification(notification);
                    gotModified = true;
                }
            }
            if(gotModified) {
                userRepository.save(user);
            }
        }
    }

}
