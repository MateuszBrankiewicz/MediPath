package com.adam.medipathbackend.models;

public class ResetForm {
    private String email;

    public ResetForm(String email, String password) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

}