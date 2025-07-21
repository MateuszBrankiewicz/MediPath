package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.City;
import com.adam.medipathbackend.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CityController {

    @Autowired
    CityRepository cityRepository;

    @GetMapping(value = {"/cities", "/cities/{name}"})
    public ResponseEntity<List<City>> getCities(@PathVariable(required = false) String name) {
        List<City> returnedCities;
        if (name == null) {
            returnedCities = cityRepository.findAll();
        } else {

            returnedCities = cityRepository.findAll(name);
        }

        return new ResponseEntity<>(returnedCities, HttpStatus.OK);
    }

}
