package com.adam.medipathbackend.services;

import com.adam.medipathbackend.models.Notification;
import com.adam.medipathbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;


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
}
