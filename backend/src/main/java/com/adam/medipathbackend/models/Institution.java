package com.adam.medipathbackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document("Institution")
public class Institution {
    @Id
    private String id;

    private String name;

    private ArrayList<String> types;

    private boolean isPublic;

    private Address address;

    private ArrayList<StaffDigest> employees;

    private float rating;

    private String image;


    public Institution(String name, boolean isPublic, Address address, float rating, String image) {
        this.name = name;
        this.isPublic = isPublic;
        this.address = address;
        this.rating = rating;
        this.image = image;
        this.types = new ArrayList<>();
        this.employees = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public ArrayList<StaffDigest> getEmployees() {
        return employees;
    }

    public void setEmployees(ArrayList<StaffDigest> employees) {
        this.employees = employees;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
