package com.adam.medipathbackend.forms;

public class UpdateUserForm {
    private final String city;
    private final String name;
    private final String surname;
    private final String province;
    private final String postalCode;
    private final String number;
    private final String street;
    private final String phoneNumber;

    public UpdateUserForm(String city, String name, String surname, String province, String postalCode, String number, String street, String phoneNumber) {
        this.name = name;
        this.surname = surname;
        this.province = province;
        this.postalCode = postalCode;
        this.number = number;
        this.street = street;
        this.phoneNumber = phoneNumber;
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
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
}
