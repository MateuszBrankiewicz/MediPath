package com.adam.medipathbackend.models;

public class ResetForm {
    private final String token;
    private final String password;

    public ResetForm(String token, String password) {
        this.token = token;
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public String getPassword() {
        return password;
    }
}