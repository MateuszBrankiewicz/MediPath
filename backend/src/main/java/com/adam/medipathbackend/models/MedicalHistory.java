package com.adam.medipathbackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document("MedicalHistory")
public class MedicalHistory {
    @Id
    private String id;

    private String userId;

    private String title;

    private String note;

    private LocalDate date;

    private DoctorDigest doctor;

    public MedicalHistory(String userId, String title, String note, LocalDate date, DoctorDigest doctor) {
        this.userId = userId;
        this.title = title;
        this.note = note;
        this.date = date;
        this.doctor = doctor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public DoctorDigest getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorDigest doctor) {
        this.doctor = doctor;
    }
}
