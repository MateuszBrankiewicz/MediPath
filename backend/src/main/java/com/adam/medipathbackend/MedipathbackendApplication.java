package com.adam.medipathbackend;

import com.adam.medipathbackend.models.City;
import com.adam.medipathbackend.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
@EnableMongoRepositories
public class MedipathbackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(MedipathbackendApplication.class, args);
	}

}
