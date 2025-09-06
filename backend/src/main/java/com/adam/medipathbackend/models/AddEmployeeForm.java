package com.adam.medipathbackend.models;

import java.util.ArrayList;

public class AddEmployeeForm {
    private String userID;
    private int roleCode;
    private ArrayList<String> specialisations;

    public AddEmployeeForm(String userID, int roleCode, ArrayList<String> specialisations) {
        this.userID = userID;
        this.roleCode = roleCode;
        this.specialisations = specialisations;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(int roleCode) {
        this.roleCode = roleCode;
    }

    public ArrayList<String> getSpecialisations() {
        return specialisations;
    }

    public void setSpecialisations(ArrayList<String> specialisations) {
        this.specialisations = specialisations;
    }
}
