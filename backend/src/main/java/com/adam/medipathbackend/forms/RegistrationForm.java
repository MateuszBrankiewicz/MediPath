package com.adam.medipathbackend.forms;

public class RegistrationForm {
    private final String city;
    private final String email;
    private final String name;
    private final String surname;
    private final String govID;
    private final String birthDate;
    private final String province;
    private final String postalCode;
    private final String number;
    private final String street;
    private final String phoneNumber;
    private final String password;

    public String getCity() {
        return city;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getGovID() {
        return govID;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getNumber() {
        return number;
    }

    public String getStreet() {
        return street;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "RegistrationForm{" +
                "city='" + city + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", govId='" + govID + '\'' +
                ", birthDate=" + birthDate +
                ", province='" + province + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", number='" + number + '\'' +
                ", street='" + street + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public RegistrationForm(String email, String name, String surname, String govID, String birthDate, String province, String city, String postalCode, String number, String street, String phoneNumber, String password) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.govID = govID;
        this.birthDate = birthDate;
        this.province = province;
        this.city = city;
        this.postalCode = postalCode;
        this.number = number;
        this.street = street;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }
}
