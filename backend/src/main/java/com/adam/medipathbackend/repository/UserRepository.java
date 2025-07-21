package com.adam.medipathbackend.repository;

import com.adam.medipathbackend.models.City;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<City, String> {


}
