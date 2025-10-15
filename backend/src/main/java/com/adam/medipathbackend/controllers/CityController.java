package com.adam.medipathbackend.controllers;

import com.adam.medipathbackend.models.City;
import com.adam.medipathbackend.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api")
public class CityController {

    @Autowired
    CityRepository cityRepository;

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
    public ResponseEntity<List<String>> getSpecialisations() {
        String[] specialisations = {
                "Allergy and immunology",
                "Anesthesiology",
                "Dermatology",
                "Diagnostic radiology" ,
                "Emergency medicine",
                "Family medicine",
                "Internal medicine",
                "Medical genetics",
                "Neurology",
                "Nuclear medicine",
                "Obstetrics and gynecology",
                "Ophthalmology",
                "Pathology",
                "Pediatrics",
                "Physical medicine and rehabilitation",
                "Preventive medicine",
                "Psychiatry",
                "Radiation oncology",
                "Surgery",
                "Urology"
                };
        return new ResponseEntity<>(List.of(specialisations), HttpStatus.OK);
    }
}
