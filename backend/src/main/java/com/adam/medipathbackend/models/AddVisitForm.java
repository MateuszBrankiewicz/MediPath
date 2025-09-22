package com.adam.medipathbackend.models;

public class AddVisitForm {
    private String scheduleID;
    private String patientID;

    public String getPatientRemarks() {
        return patientRemarks;
    }

    public void setPatientRemarks(String patientRemarks) {
        this.patientRemarks = patientRemarks;
    }

    private String patientRemarks;

    public AddVisitForm(String scheduleID, String patientID, String patientRemarks) {
        this.scheduleID = scheduleID;
        this.patientID = patientID;
        this.patientRemarks = patientRemarks;
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
