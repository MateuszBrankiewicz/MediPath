package com.adam.medipathbackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Objects;
import com.adam.medipathbackend.config.Utils;

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

    public int getNumOfRatings() {
        return numOfRatings;
    }

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

    public void setEmployees(ArrayList<StaffDigest> employees) {this.employees = employees;}

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

    public void editRating(float newRating, float oldRating) {
        this.rating = ((this.rating * this.numOfRatings) - oldRating + newRating) / this.numOfRatings;
    }

    public void subtractRating(float newRating) {
        if(numOfRatings - 1 == 0) {
            this.rating = 0;
            this.numOfRatings = 0;
        } else {
            this.rating = ((this.rating * this.numOfRatings) - newRating) / (this.numOfRatings - 1);
            this.numOfRatings -= 1;
        }
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public boolean isSimilar(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Institution other = (Institution) o;
        String currentFullName = this.name + "," + this.address.toString();
        String otherFullName = other.name + "," + other.address.toString();
        return Utils.isSimilar(currentFullName, otherFullName);
    }


}
