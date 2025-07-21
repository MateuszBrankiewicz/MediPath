package com.adam.medipathbackend.models;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("City")

public class User {

    @Id
    private String Id;


}
