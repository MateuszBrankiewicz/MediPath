package com.adam.medipathbackend.forms;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ManySchedulesUpdateForm {
    private String doctorID;
    private String institutionID;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Europe/Warsaw")
    private LocalDateTime startHour;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Europe/Warsaw")
    private LocalDateTime endHour;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Europe/Warsaw")
    private LocalDateTime newStartHour;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Europe/Warsaw")
    private LocalDateTime newEndHour;
    @JsonFormat(pattern="HH:mm:ss", timezone="Europe/Warsaw")
    private LocalTime newInterval;

    public ManySchedulesUpdateForm(String doctorID, String institutionID, LocalDateTime startHour, LocalDateTime endHour, LocalDateTime newStartHour, LocalDateTime newEndHour, LocalTime newInterval) {
        this.doctorID = doctorID;
        this.institutionID = institutionID;
        this.startHour = startHour;
        this.endHour = endHour;
        this.newStartHour = newStartHour;
        this.newEndHour = newEndHour;
        this.newInterval = newInterval;
    }

    public String getDoctorID() {
        return doctorID;
    }

    public void setDoctorID(String doctorID) {
        this.doctorID = doctorID;
    }

    public String getInstitutionID() {
        return institutionID;
    }

    public void setInstitutionID(String institutionID) {
        this.institutionID = institutionID;
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

    public LocalDateTime getNewStartHour() {
        return newStartHour;
    }

    public void setNewStartHour(LocalDateTime newStartHour) {
        this.newStartHour = newStartHour;
    }

    public LocalDateTime getNewEndHour() {
        return newEndHour;
    }

    public void setNewEndHour(LocalDateTime newEndHour) {
        this.newEndHour = newEndHour;
    }

    public LocalTime getNewInterval() {
        return newInterval;
    }

    public void setNewInterval(LocalTime newInterval) {
        this.newInterval = newInterval;
    }
}
