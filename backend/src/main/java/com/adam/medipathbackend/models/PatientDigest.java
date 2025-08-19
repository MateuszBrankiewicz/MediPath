package com.adam.medipathbackend.models;

public class PatientDigest {
    private String patientId;
    private String name;
    private String surname;
    private String govID;


    public PatientDigest(String userId, String name, String surname, String govID) {
        this.patientId = userId;
        this.name = name;
        this.surname = surname;
        this.govID = govID;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String id) {
        this.patientId = id;
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
