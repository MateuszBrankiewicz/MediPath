package com.adam.medipathbackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Specialisation")
public class Specialisation {
    @Id
    private String id;
    private String name;
    private boolean isInstitutionType;

    public Specialisation(String name, boolean isInstitutionType) {
        this.name = name;
        this.isInstitutionType = isInstitutionType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInstitutionType() {
        return isInstitutionType;
    }

    public void setInstitutionType(boolean institutionType) {
        isInstitutionType = institutionType;
    }
}
