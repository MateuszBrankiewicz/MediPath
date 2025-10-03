package com.adam.medipathbackend.forms;

public class AddVisitForm {
    private String scheduleID;
    private String patientRemarks;


    public String getPatientRemarks() {
        return patientRemarks;
    }

    public void setPatientRemarks(String patientRemarks) {
        this.patientRemarks = patientRemarks;
    }



    public AddVisitForm(String scheduleID, String patientID, String patientRemarks) {
        this.scheduleID = scheduleID;
        this.patientRemarks = patientRemarks;
    }

    public String getScheduleID() {
        return scheduleID;
    }

    public void setScheduleID(String scheduleID) {
        this.scheduleID = scheduleID;
    }

}
