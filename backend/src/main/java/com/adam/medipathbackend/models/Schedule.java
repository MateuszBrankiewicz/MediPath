package com.adam.medipathbackend.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("Schedule")
public class Schedule {
    @Id
    private String id;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Europe/Warsaw")
    private LocalDateTime startHour;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Europe/Warsaw")
    private LocalDateTime endHour;

    private boolean booked;

    private DoctorDigest doctor;

    private InstitutionDigest institution;

    public Schedule(LocalDateTime startHour, LocalDateTime endHour, DoctorDigest doctor, InstitutionDigest institution) {
        this.startHour = startHour;
        this.endHour = endHour;
        this.booked = false;
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
