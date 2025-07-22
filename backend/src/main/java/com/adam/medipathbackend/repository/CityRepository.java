package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.City;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CityRepository extends MongoRepository<City, String> {

    @Query("{'name': {$regex : ?0, $options: 'i'}}")
    List<City> findAll(String match);

    @Query("{}")
    List<City> findAll();

    long count();

}
