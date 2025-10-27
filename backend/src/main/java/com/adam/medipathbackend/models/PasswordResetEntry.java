package com.adam.medipathbackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Document("PasswordReset")
public class PasswordResetEntry {

    @Id
    private String id;

    private final String email;

    private final String token;
    private final LocalDateTime dateIssued;
    private LocalDateTime dateExpiry;

    private boolean gotUsed;

    public PasswordResetEntry(String email, String token) {
        this.email = email;
        this.token = token;
        this.dateIssued = LocalDateTime.now();
        this.dateExpiry = dateIssued.plusMinutes(10);
        this.gotUsed = false;
    }

    public void setDateExpiry(LocalDateTime dateExpiry) {
        this.dateExpiry = dateExpiry;
    }

    public String getToken() {
        return token;
    }

    public void setGotUsed(boolean gotUsed) {
        this.gotUsed = gotUsed;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getDateIssued() {
        return dateIssued;
    }

    public LocalDateTime getDateExpiry() {
        return dateExpiry;
    }

    public boolean isGotUsed() {
        return gotUsed;
    }
}
