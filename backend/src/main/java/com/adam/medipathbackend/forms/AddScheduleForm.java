package com.adam.medipathbackend.forms;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class AddScheduleForm {
    private String doctorID;
    private String institutionID;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Europe/Warsaw")
    private LocalDateTime startHour;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Europe/Warsaw")
    private LocalDateTime endHour;


    public AddScheduleForm(String doctorID, String institutionID, LocalDateTime startHour, LocalDateTime endHour) {
        this.doctorID = doctorID;
        this.institutionID = institutionID;
        this.startHour = startHour;
        this.endHour = endHour;
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

}
