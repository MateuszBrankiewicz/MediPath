package com.adam.medipathbackend.models;

import java.util.Objects;

public class Address {
    private String province;
    private String city;
    private String street;
    private String number;
    private String postalCode;

    public Address(String province, String city, String street, String number, String postalCode) {
        this.province = province;
        this.city = city;
        this.street = street;
        this.number = number;
        this.postalCode = postalCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(province, address.province) && Objects.equals(city, address.city) && Objects.equals(street, address.street) && Objects.equals(number, address.number) && Objects.equals(postalCode, address.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(province, city, street, number, postalCode);
    }

    public boolean isValid() {
        return !(postalCode == null || postalCode.isBlank() || number == null || number.isBlank() || city == null || city.isBlank() || province == null || province.isBlank());
    }
}
