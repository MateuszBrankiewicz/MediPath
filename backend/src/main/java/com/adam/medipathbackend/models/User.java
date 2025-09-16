package com.adam.medipathbackend.models;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;

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

    private Address address;

    private String phoneNumber;

    private String passwordHash;

    private String licenceNumber;

    private ArrayList<String> specialisations;

    private LinkedList<MedicalHistory> latestMedicalHistory;

    private int roleCode;

    private ArrayList<Notification> notifications;

    private float rating;

    private boolean isActive;

    private ArrayList<InstitutionDigest> employers;

    private UserSettings userSettings;

    private int numOfRatings;

    private String pfpimage;

    @Override
    public String toString() {
        return email + " : " + name + " " + surname;
    }


    public User(String email, String name, String surname, String govId, LocalDate birthDate, Address address, String phoneNumber, String passwordHash, UserSettings userSettings) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.govId = govId;
        this.birthDate = birthDate;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.licenceNumber = "";
        this.specialisations = new ArrayList<>();
        this.latestMedicalHistory = new LinkedList<>();
        this.roleCode = 1;
        this.notifications = new ArrayList<>();
        this.rating = 0;
        this.isActive = true;
        this.employers = new ArrayList<>();
        this.userSettings = userSettings;
        this.numOfRatings = 0;
        this.pfpimage = "";
    }

    public int getNumOfRatings() {
        return numOfRatings;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
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

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public ArrayList<String> getSpecialisations() {
        return specialisations;
    }

    public void setSpecialisations(ArrayList<String> specialisations) {
        this.specialisations = specialisations;
    }

    public LinkedList<MedicalHistory> getLatestMedicalHistory() {
        return latestMedicalHistory;
    }

    public void setLatestMedicalHistory(LinkedList<MedicalHistory> latestMedicalHistory) {
        this.latestMedicalHistory = latestMedicalHistory;
    }

    public int getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(int roleCode) {
        this.roleCode = roleCode;
    }

    public ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }

    public float getRating() {
        return rating;
    }

    public void addRating(float newRating) {
        this.rating = ((this.rating * this.numOfRatings) + newRating) / (this.numOfRatings + 1);
        this.numOfRatings++;
    }


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public ArrayList<InstitutionDigest> getEmployers() {
        return employers;
    }

    public void addEmployer(InstitutionDigest digest) {
        this.employers.add(digest);
    }

    public String getId() {
        return id;
    }

    public String getPfpimage() {
        return pfpimage;
    }

    public void setPfpimage(String pfpimage) {
        this.pfpimage = pfpimage;
    }
}
