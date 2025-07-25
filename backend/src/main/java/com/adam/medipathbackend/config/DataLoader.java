package com.adam.medipathbackend.config;

import com.adam.medipathbackend.models.City;
import com.adam.medipathbackend.repository.CityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CityRepository cityRepository;


    @Override
    public void run(String... args) throws Exception {
        if(cityRepository.count() == 0) {
            cityRepository.save(new City("Lublin"));
            cityRepository.save(new City("Lubin"));
            cityRepository.save(new City("Warszawa"));
            cityRepository.save(new City("Gdańsk"));
            cityRepository.save(new City("Gdynia"));
        }
    }
}
