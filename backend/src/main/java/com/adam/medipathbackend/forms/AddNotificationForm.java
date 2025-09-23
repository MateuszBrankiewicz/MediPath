package com.adam.medipathbackend.forms;


import java.time.LocalDate;
import java.time.LocalTime;

public class AddNotificationForm {
    private String userId;
    private String content;
    private String title;
    private LocalTime reminderTime;
    private LocalDate startDate;
    private LocalDate endDate;

    public AddNotificationForm(String userId, String content, String title, LocalTime reminderTime, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.content = content;
        this.title = title;
        this.reminderTime = reminderTime;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
