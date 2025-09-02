package com.adam.medipathbackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Objects;

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

    private int numOfRatings;

    public Institution(String name, boolean isPublic, Address address, String image) {
        this.name = name;
        this.isPublic = isPublic;
        this.address = address;
        this.rating = 0;
        this.image = image;
        this.types = new ArrayList<>();
        this.employees = new ArrayList<>();
        this.numOfRatings = 0;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void addEmployee(StaffDigest staff) {
        employees.add(staff);
    }

    public float getRating() {
        return rating;
    }

    public void addRating(float newRating) {
        this.rating = ((this.rating * this.numOfRatings) + newRating) / (this.numOfRatings + 1);
        this.numOfRatings += 1;
    }



    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Institution that = (Institution) o;
        return isPublic == that.isPublic && Objects.equals(name, that.name) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isPublic, address);
    }
}
