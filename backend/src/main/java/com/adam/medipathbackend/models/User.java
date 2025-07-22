package com.adam.medipathbackend.models;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document("User")

public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String name;

    private String surname;

    @Indexed(unique = true)
    private String govId;

    private LocalDate birthDate;

    private String province;

    private String postalCode;

    private String city;

    private String number;

    private String street;

    private String phoneNumber;

    private String passwordHash;

    @Override
    public String toString() {
        return email + " : " + name + " " + surname;
    }

    public User(String email, String name, String surname, String govId, LocalDate birthDate, String province, String city, String postalCode, String number, String street, String phoneNumber, String passwordHash) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.govId = govId;
        this.birthDate = birthDate;
        this.province = province;
        this.city = city;
        this.postalCode = postalCode;
        this.number = number;
        this.street = street;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getGovId() {
        return govId;
    }

    public void setGovId(String govId) {
        this.govId = govId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
