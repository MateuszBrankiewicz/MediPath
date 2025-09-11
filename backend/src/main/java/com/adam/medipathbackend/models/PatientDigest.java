package com.adam.medipathbackend.models;

public class PatientDigest {
    private String userId;
    private String name;
    private String surname;
    private String govID;


    public PatientDigest(String userId, String name, String surname, String govID) {
        this.userId = userId;
        this.name = name;
        this.surname = surname;
        this.govID = govID;
    }
    public boolean isValid() {
        return this.userId != null && this.govID != null && this.name != null && this.surname != null
                && !(this.userId.isBlank() || this.surname.isBlank() || this.name.isBlank() || this.govID.isBlank());
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGovID() {
        return govID;
    }

    public void setGovID(String govID) {
        this.govID = govID;
    }
}
