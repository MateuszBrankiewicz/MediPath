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

    @Scheduled(fixedRate = 5*60000)
    public void pushNotifications() {

        List<User> users = userRepository.getUserNotificationsNow(LocalDateTime.now().minusMinutes(2).minusSeconds(30),
                LocalDateTime.now().plusMinutes(2).plusSeconds(30));
        for(User user: users) {
            for(Notification notification: user.getNotifications()) {
                notificationService.sendNotificationToUser(user.getId(), notification);

            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void deleteOldNotifications() {
        userRepository.deleteOldNotifications(LocalDateTime.now().minusMonths(1));
    }

}
