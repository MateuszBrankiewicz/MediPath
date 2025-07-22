package com.adam.medipathbackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("City")
public class City {



    @Id
    private String id;

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    @Indexed(unique = true)
    private final String name;

    public City(String name) {
        super();
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
