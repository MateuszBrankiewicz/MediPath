package com.adam.medipathbackend.models;

import java.util.ArrayList;

public class DoctorDigest {
    private String userId;
    private String doctorName;
    private String doctorSurname;
    private ArrayList<String> specialisations;

    public DoctorDigest(String userId, String doctorName, String doctorSurname, ArrayList<String> specialisations) {
        this.userId = userId;
        this.doctorName = doctorName;
        this.doctorSurname = doctorSurname;
        this.specialisations = specialisations;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isValid() {
        return this.userId != null && this.doctorName != null && this.doctorSurname != null && this.specialisations != null
                && !(this.userId.isBlank() || this.doctorSurname.isBlank() || this.doctorName.isBlank());
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSurname() {
        return doctorSurname;
    }

    public void setDoctorSurname(String doctorSurname) {
        this.doctorSurname = doctorSurname;
    }

    public ArrayList<String> getSpecialisations() {
        return specialisations;
    }

    public void setSpecialisations(ArrayList<String> specialisations) {
        this.specialisations = specialisations;
    }
}
