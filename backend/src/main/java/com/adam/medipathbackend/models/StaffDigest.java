package com.adam.medipathbackend.models;

import java.util.ArrayList;

public class StaffDigest {
    private String userId;
    private String name;
    private String surname;
    private ArrayList<String> specialisations;
    private int roleCode;
    private String pfpimage;

    public StaffDigest(String userId, String name, String surname, ArrayList<String> specialisations, int roleCode, String pfpimage) {
        this.userId = userId;
        this.name = name;
        this.surname = surname;
        this.specialisations = specialisations;
        this.roleCode = roleCode;
        this.pfpimage = pfpimage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public ArrayList<String> getSpecialisations() {
        return specialisations;
    }

    public void setSpecialisations(ArrayList<String> specialisations) {
        this.specialisations = specialisations;
    }

    public int getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(int roleCode) {
        this.roleCode = roleCode;
    }

    public String getPfpimage() {
        return pfpimage;
    }

    public void setPfpimage(String pfpimage) {
        this.pfpimage = pfpimage;
    }
}
