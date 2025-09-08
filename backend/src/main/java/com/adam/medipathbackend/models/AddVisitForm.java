package com.adam.medipathbackend.models;

public class AddVisitForm {
    private String scheduleID;
    private String patientID;

    public AddVisitForm(String scheduleID, String patientID) {
        this.scheduleID = scheduleID;
        this.patientID = patientID;
    }

    public String getScheduleID() {
        return scheduleID;
    }

    public void setScheduleID(String scheduleID) {
        this.scheduleID = scheduleID;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }
}
