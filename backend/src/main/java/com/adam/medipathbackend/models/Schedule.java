package com.adam.medipathbackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("Schedule")
public class Schedule {
    @Id
    private String id;

    private LocalDateTime startHour;

    private LocalDateTime endHour;

    private boolean booked;

    private DoctorDigest doctor;

    private InstitutionDigest institution;

    public Schedule(LocalDateTime startHour, LocalDateTime endHour, boolean booked, DoctorDigest doctor, InstitutionDigest institution) {
        this.startHour = startHour;
        this.endHour = endHour;
        this.booked = booked;
        this.doctor = doctor;
        this.institution = institution;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getStartHour() {
        return startHour;
    }

    public void setStartHour(LocalDateTime startHour) {
        this.startHour = startHour;
    }

    public LocalDateTime getEndHour() {
        return endHour;
    }

    public void setEndHour(LocalDateTime endHour) {
        this.endHour = endHour;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public DoctorDigest getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorDigest doctor) {
        this.doctor = doctor;
    }

    public InstitutionDigest getInstitution() {
        return institution;
    }

    public void setInstitution(InstitutionDigest institution) {
        this.institution = institution;
    }
}
