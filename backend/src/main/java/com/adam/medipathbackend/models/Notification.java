package com.adam.medipathbackend.models;

import java.time.LocalDateTime;

public class Notification {
    private String title;
    private String content;
    private LocalDateTime timestamp;
    private boolean isSystem;
    private boolean isRead;

    public Notification(String title, String content, LocalDateTime timestamp, boolean isSystem, boolean isRead) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.isSystem = isSystem;
        this.isRead = isRead;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
