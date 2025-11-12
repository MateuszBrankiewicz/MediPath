package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.City;
import com.adam.medipathbackend.models.Specialisation;
import com.adam.medipathbackend.repository.CityRepository;
import com.adam.medipathbackend.repository.SpecialisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api")
public class CityController {

    @Autowired
    CityRepository cityRepository;

    @Autowired
    SpecialisationRepository specialisationRepository;
    @GetMapping("/")
    public String home() {
        return "API OK";
    }

    @GetMapping(value = {"/cities","/cities/", "/cities/{name}"})
    public ResponseEntity<List<City>> getCities(@PathVariable(required = false) String name) {
        List<City> returnedCities;
        if (name == null) {
            returnedCities = cityRepository.findAll();
        } else {

            returnedCities = cityRepository.findAll(name);
        }

        return new ResponseEntity<>(returnedCities, HttpStatus.OK);
    }

    @GetMapping(value= {"/provinces", "/provinces/"})
    public ResponseEntity<List<String>> getProvinces() {
        String[] provinces = {"Dolnośląskie", "Kujawsko-Pomorskie", "Lubelskie", "Lubuskie", "Łódzkie", "Małopolskie", "Mazowieckie", "Opolskie", "Podkarpackie", "Podlaskie", "Pomorskie", "Śląskie", "Świętokrzyskie", "Warmińsko-Mazurskie", "Wielkopolskie", "Zachodniopomorskie"};
        return new ResponseEntity<>(List.of(provinces), HttpStatus.OK);
    }



    @GetMapping(value= {"/specialisations", "/specialisations/"})
    public ResponseEntity<List<Specialisation>> getSpecialisations(
            @RequestParam(required = false) Boolean isInstitutionType) {

        List<Specialisation> specialisations;
        if (isInstitutionType == null) {
            specialisations = specialisationRepository.findAll();
        } else {
            specialisations = specialisationRepository.findByIsInstitutionType(isInstitutionType);
        }
        return new ResponseEntity<>(specialisations, HttpStatus.OK);
    }
}
